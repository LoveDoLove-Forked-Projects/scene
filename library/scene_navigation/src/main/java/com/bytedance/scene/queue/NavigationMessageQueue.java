/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene.queue;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.SceneInternalException;
import com.bytedance.scene.utlity.TaskStartSignal;
import com.bytedance.scene.utlity.ThreadUtility;

import java.util.LinkedList;

/**
 * Created by jiangqi on 2023/11/3
 *
 * @author jiangqi@bytedance.com
 * <p>
 * An Android Handler MessageQueue wrapper that ensures navigation operations are scheduled and executed as expected.
 * For example, a single navigation operation may involve multiple Handler messages.
 * While one navigation operation is still in progress, additional navigation requests may be made.
 * However, each new operation will be queued and executed only after the current operation has completed,
 * ensuring that navigation actions are processed sequentially and without conflict.
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY)
public class NavigationMessageQueue {
    private static final String TAG = "NavigationMessageQueue";
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final LinkedList<NavigationRunnable> mPendingTasks = new LinkedList<>();
    private boolean mIsRunningPostSync = false;
    private IdleRunnable mIdleRunnable = null;

    private int mDelayMessageCount = 0;

    /**
     * post task at the end of queue
     *
     * @param runnable
     */
    public void postAsync(@NonNull final NavigationRunnable runnable) {
        LoggerManager.getInstance().i(TAG, "postAsync submit " + runnable.toString());
        ThreadUtility.checkUIThread();
        this.forceExecuteIdleTask();
        this.mPendingTasks.add(runnable);
        this.mHandler.post(this.mSceneNavigationTask);
    }

    /**
     * post task at the front of system queue and framework queue
     *
     * @param runnable
     */
    public void postUrgentAtHead(@NonNull final NavigationRunnable runnable) {
        LoggerManager.getInstance().i(TAG, "postUrgentAtHead submit " + runnable.toString());
        this.forceExecuteIdleTask();
        this.mPendingTasks.add(0, runnable);
        this.mHandler.postAtFrontOfQueue(this.mSceneNavigationTask);
    }

    /**
     * post task at the front of queue
     *
     * @param runnable
     */
    public void postAsyncAtHead(@NonNull final NavigationRunnable runnable) {
        LoggerManager.getInstance().i(TAG, "postAsyncAtHead submit " + runnable.toString());
        this.forceExecuteIdleTask();
        this.mPendingTasks.add(0, runnable);
        this.mHandler.post(this.mSceneNavigationTask);
    }

    public boolean remove(@NonNull final NavigationRunnable runnable) {
        LoggerManager.getInstance().i(TAG, "remove " + runnable.toString());
        return this.mPendingTasks.remove(runnable);
    }

    @VisibleForTesting
    public void forceExecuteIdleTask() {
        while (true) {
            IdleRunnable tmp = this.mIdleRunnable;
            this.mIdleRunnable = null;

            if (tmp != null && tmp.hasStarted()) {
                tmp.forceExecute();
            } else {
                break;
            }
        }
    }

    /**
     * post a delayed(idle and timeout) task at the front of queue, it will delay all task too
     * @param runnable
     * @param timeOutMillis
     */
    public void postAsyncAtHeadIdleDelayed(@NonNull final NavigationRunnable runnable, long timeOutMillis) {
        this.postAsyncAtHeadIdleDelayed(runnable, null, null, timeOutMillis);
    }

    public void postAsyncAtHeadIdleDelayed(@NonNull final NavigationRunnable runnable, @Nullable TaskStartSignal taskStartSignal, @Nullable CancellationSignal cancellationSignal, long timeOutMillis) {
        LoggerManager.getInstance().i(TAG, "postAsyncAtHeadIdleDelayed submit " + runnable.toString());
        this.forceExecuteIdleTask();

        if (this.mIdleRunnable != null) {
            throw new SceneInternalException("mIdleRunnable should be null");
        }

        if (!this.mPendingTasks.isEmpty()) {
            this.mDelayMessageCount = this.mPendingTasks.size();
            this.mHandler.removeCallbacks(this.mSceneNavigationTask);
        }

        this.mPendingTasks.add(0, runnable);
        this.mIdleRunnable = new IdleRunnable(this.mHandler, this.mSceneNavigationTask, taskStartSignal, cancellationSignal, timeOutMillis);
        this.mIdleRunnable.start();
    }

    private final SceneMainRunnable mSceneNavigationTask = new SceneMainRunnable() {
        @Override
        public void run() {
            while (mDelayMessageCount > 0) {
                mHandler.post(this);
                mDelayMessageCount--;
            }

            NavigationRunnable currentTask = NavigationMessageQueue.this.mPendingTasks.poll();
            if (currentTask == null) {
                LoggerManager.getInstance().i(TAG, "empty return");
                return;
            }

            LoggerManager.getInstance().i(TAG, "mSceneNavigationTask run task start " + currentTask.toString());
            currentTask.run();
            LoggerManager.getInstance().i(TAG, "mSceneNavigationTask run task finish " + currentTask.toString());
        }
    };

    /**
     * force execute all previous tasks and then execute target task
     *
     * @param runnable
     */
    public void postSync(@NonNull Runnable runnable) {
        LoggerManager.getInstance().i(TAG, "postSync start");
        ThreadUtility.checkUIThread();

        if (this.mIsRunningPostSync) {
            throw new SceneInternalException("Can't execute postSync nested.");
        }

        this.mIsRunningPostSync = true;

        try {
            forceExecuteIdleTask();

            //execute all previous navigation tasks
            while (true) {
                NavigationRunnable currentTask = this.mPendingTasks.poll();
                if (currentTask == null) {
                    break;
                }

                LoggerManager.getInstance().i(TAG, "postSync run previous pending task start " + currentTask.toString());
                currentTask.run();
                LoggerManager.getInstance().i(TAG, "postSync run previous pending task finish " + currentTask.toString());
            }

            this.mHandler.removeCallbacks(this.mSceneNavigationTask);

            //then execute this task
            LoggerManager.getInstance().i(TAG, "postSync run current task start " + runnable.toString());
            runnable.run();
            LoggerManager.getInstance().i(TAG, "postSync run current task finish " + runnable.toString());
        } finally {
            this.mIsRunningPostSync = false;
        }
        LoggerManager.getInstance().i(TAG, "postSync finish");
    }

    public boolean hasPendingTasks() {
        return mPendingTasks.size() > 0;
    }

    @VisibleForTesting
    public int getDelayMessageCount() {
        return this.mDelayMessageCount;
    }
}
