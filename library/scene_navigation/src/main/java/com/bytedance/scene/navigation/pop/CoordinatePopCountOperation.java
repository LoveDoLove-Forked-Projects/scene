package com.bytedance.scene.navigation.pop;

import android.view.View;

import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneTrace;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.interfaces.Function;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneManager;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.navigation.pop.idle.PopAnimationOperationV2;
import com.bytedance.scene.navigation.pop.idle.PopDestroyMiddlePageOperationV2;
import com.bytedance.scene.navigation.pop.idle.PopDestroyOperationV2;
import com.bytedance.scene.queue.NavigationMessageQueue;
import com.bytedance.scene.queue.NavigationRunnable;
import com.bytedance.scene.utlity.Constants;
import com.bytedance.scene.utlity.TaskStartSignal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangqi on 2023/11/15
 *
 * @author jiangqi@bytedance.com
 * <p>
 * 封装成三个 Operation，再封装进三个 NavigationRunnable
 * <p>
 * PopPauseOperation -> PopResumeOperation -> PopDestroyOperation
 */
public class CoordinatePopCountOperation implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationMessageQueue mMessageQueue;
    private final NavigationAnimationExecutor mAnimationFactory;
    private final PopOptions mPopOptions;
    private final int mPopCount;
    private final NavigationScene mNavigationScene;
    @Nullable
    private final Function<Scene, Void> mAfterOnActivityCreatedAction;

    public CoordinatePopCountOperation(NavigationManagerAbility navigationManagerAbility,
                                       NavigationMessageQueue messageQueue,
                                       NavigationAnimationExecutor animationFactory, int popCount, PopOptions popOptions) {
        this(navigationManagerAbility, messageQueue, animationFactory, popCount, popOptions, null);
    }

    public CoordinatePopCountOperation(NavigationManagerAbility navigationManagerAbility, NavigationMessageQueue messageQueue, NavigationAnimationExecutor animationFactory, int popCount, PopOptions popOptions, Function<Scene, Void> afterOnActivityCreatedAction) {
        this.mManagerAbility = navigationManagerAbility;
        this.mMessageQueue = messageQueue;
        this.mAnimationFactory = animationFactory;
        this.mPopCount = popCount;
        this.mPopOptions = popOptions;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.mAfterOnActivityCreatedAction = afterOnActivityCreatedAction;
    }

    @Override
    public void execute(final Runnable operationEndAction) {
        this.mManagerAbility.cancelCurrentRunningAnimation();

        if (!this.mManagerAbility.canExecuteNavigationStackOperation()) {
            throw new IllegalArgumentException("Can't pop, current NavigationScene state " + mNavigationScene.getState().name);
        }

        List<Record> recordList = this.mManagerAbility.getCurrentRecordList();
        if (this.mPopCount <= 0) {
            throw new IllegalArgumentException("popCount can not be " + this.mPopCount + " stackSize is " + recordList.size());
        }
        if (this.mPopCount >= recordList.size()) {
            /*
             * Need to pop all that can pop.
             * Extreme case: there are 2 Scenes, pop two times and push one new,
             * the new one will push failed because the Activity has been destroyed.
             */
            if (recordList.size() > 1) {
                new CoordinatePopCountOperation(this.mManagerAbility, mMessageQueue, mAnimationFactory, recordList.size() - 1, mPopOptions).execute(new Runnable() {
                    @Override
                    public void run() {
                        mManagerAbility.getNavigationScene().finishCurrentActivity();
                        operationEndAction.run();
                    }
                });
            }
            return;
        }

        final List<Record> destroyRecordList = new ArrayList<>();
        for (int i = 0; i <= this.mPopCount - 1; i++) {
            Record record = recordList.get(recordList.size() - 1 - i);
            destroyRecordList.add(record);
        }

        final Record returnRecord = recordList.get(recordList.size() - this.mPopCount - 1);
        final Record currentRecord = this.mManagerAbility.getCurrentRecord();
        final Scene currentScene = currentRecord.mScene;
        final View currentSceneView = currentScene.getView();

        //pause current scene
        PopPauseOperation popPauseOperation = new PopPauseOperation(this.mManagerAbility, currentScene);

        //resume previous scene
        final PopResumeOperation popResumeOperation = new PopResumeOperation(this.mManagerAbility, currentRecord, returnRecord, this.mAfterOnActivityCreatedAction);

        //destroy top scene
        NavigationRunnable popDestroyTask = null;

        if (mPopOptions == null || !mPopOptions.isUseIdleWhenStop()) {
            final PopDestroyOperation popDestroyOperation = new PopDestroyOperation(this.mManagerAbility, mAnimationFactory, destroyRecordList, currentRecord, returnRecord, currentScene, currentSceneView);
            final NavigationRunnable tmpPopDestroyTask = new NavigationRunnable() {
                @Override
                public void run() {
                    SceneTrace.beginSection(NavigationSceneManager.TRACE_EXECUTE_OPERATION_TAG);
                    String suppressTag = mManagerAbility.beginSuppressStackOperation("NavigationManager execute operation directly");
                    mManagerAbility.executeOperationSafely(popDestroyOperation, operationEndAction);
                    mManagerAbility.endSuppressStackOperation(suppressTag);
                    mManagerAbility.notifySceneStateChanged();
                    SceneTrace.endSection();
                }
            };
            popDestroyTask = tmpPopDestroyTask;
        } else {
            final PopDestroyMiddlePageOperationV2 clearMiddlePageOperationV2 = new PopDestroyMiddlePageOperationV2(this.mManagerAbility, mAnimationFactory, destroyRecordList, currentRecord, returnRecord, currentScene, currentSceneView);
            final PopAnimationOperationV2 animationOperationV2 = new PopAnimationOperationV2(this.mManagerAbility, mAnimationFactory, destroyRecordList, currentRecord, returnRecord, currentScene, currentSceneView);
            final PopDestroyOperationV2 popDestroyOperationV2 = new PopDestroyOperationV2(this.mManagerAbility, mAnimationFactory, destroyRecordList, currentRecord, returnRecord, currentScene, currentSceneView);

            final NavigationRunnable popDestroyOperationV2Task = new NavigationRunnable() {
                @Override
                public void run() {
                    SceneTrace.beginSection(NavigationSceneManager.TRACE_EXECUTE_OPERATION_TAG);
                    String suppressTag = mManagerAbility.beginSuppressStackOperation("NavigationManager execute popDestroyOperationV2Task operation directly");
                    mManagerAbility.executeOperationSafely(popDestroyOperationV2, operationEndAction);
                    mManagerAbility.endSuppressStackOperation(suppressTag);
                    mManagerAbility.notifySceneStateChanged();
                    SceneTrace.endSection();
                }
            };

            popDestroyTask = new NavigationRunnable() {
                @Override
                public void run() {
                    SceneTrace.beginSection(NavigationSceneManager.TRACE_EXECUTE_OPERATION_TAG);
                    String suppressTag = mManagerAbility.beginSuppressStackOperation("NavigationManager execute operation directly");
                    mManagerAbility.executeOperationSafely(clearMiddlePageOperationV2, null);
                    TaskStartSignal taskStartSignal = new TaskStartSignal();
                    mManagerAbility.executeOperationSafely(animationOperationV2, new Runnable() {
                        @Override
                        public void run() {
                            taskStartSignal.start();
                        }
                    });

                    mMessageQueue.executeWhenIdleOrTimeLimit(popDestroyOperationV2Task, taskStartSignal, animationOperationV2.cancellationSignalList, TimeUnit.SECONDS.toMillis(Constants.SCENE_DESTROY_MAX_TIMEOUT_SECONDS));
                    mManagerAbility.endSuppressStackOperation(suppressTag);
                    SceneTrace.endSection();
                }
            };
        }

        NavigationRunnable finalPopDestroyTask = popDestroyTask;
        final NavigationRunnable popResumeTask = new NavigationRunnable() {
            @Override
            public void run() {
                SceneTrace.beginSection(NavigationSceneManager.TRACE_EXECUTE_OPERATION_TAG);
                String suppressTag = mManagerAbility.beginSuppressStackOperation("NavigationManager execute operation directly");
                mManagerAbility.executeOperationSafely(popResumeOperation, new Runnable() {
                    @Override
                    public void run() {
                        mMessageQueue.postAsyncAtHead(finalPopDestroyTask);
                    }
                });
                mManagerAbility.endSuppressStackOperation(suppressTag);
                SceneTrace.endSection();
            }
        };

        popPauseOperation.execute(new Runnable() {
            @Override
            public void run() {
                mMessageQueue.postAsyncAtHead(popResumeTask);
            }
        });
    }
}
