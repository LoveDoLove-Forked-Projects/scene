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
package com.bytedance.scene.navigation.pop;

import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignalList;
import com.bytedance.scene.utlity.SceneInternalException;

import java.util.List;

/**
 * Created by jiangqi on 2023/11/19
 *
 * @author jiangqi@bytedance.com
 */
public class PopDestroyOperation implements Operation {
    private static final String TAG = "PopDestroyOperation";
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationAnimationExecutor mAnimationFactory;
    private final NavigationScene mNavigationScene;
    private final List<Record> mDestroyRecordList;
    private final Record mCurrentRecord;
    private final Record mReturnRecord;
    private final Scene mCurrentScene;

    public PopDestroyOperation(NavigationManagerAbility navigationManagerAbility, NavigationAnimationExecutor animationFactory,
                               List<Record> destroyRecordList, Record currentRecord, Record returnRecord, Scene currentScene) {
        this.mManagerAbility = navigationManagerAbility;
        this.mAnimationFactory = animationFactory;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.mDestroyRecordList = destroyRecordList;
        this.mCurrentRecord = currentRecord;
        this.mReturnRecord = returnRecord;
        this.mCurrentScene = currentScene;
    }

    @Override
    public void execute(final Runnable operationEndAction) {
        LoggerManager.getInstance().i(TAG, "invoke execute operation");
        //cache view before destroy it
        View currentSceneView = this.mCurrentScene.getView();

        for (final Record record : this.mDestroyRecordList) {
            this.mManagerAbility.destroyByRecord(record, mCurrentRecord);
        }

        // Ensure that the requesting Scene is correct
        if (mCurrentRecord.mPushResultCallback != null && mNavigationScene.isFixOnResultTiming()) {
            this.mManagerAbility.obtainNavigationResultActionHandler().deliverResultLegacy(mCurrentRecord);
        }

        this.mManagerAbility.obtainNavigationResultActionHandler().deliverResult(mReturnRecord);

        this.mManagerAbility.restoreActivityStatus(mReturnRecord.mActivityStatusRecord);
        this.mManagerAbility.getNavigationListener().navigationChange(mCurrentRecord.mScene, mReturnRecord.mScene, false);

        NavigationAnimationExecutor navigationAnimationExecutor = null;
        // If Pop has a specified animation, the animation specified by Pop is preferred.
        if (this.mAnimationFactory != null && this.mAnimationFactory.isSupport(mCurrentRecord.mScene.getClass(), mReturnRecord.mScene.getClass())) {
            navigationAnimationExecutor = this.mAnimationFactory;
        }

        if (navigationAnimationExecutor == null && mCurrentRecord.mNavigationAnimationExecutor != null && mCurrentRecord.mNavigationAnimationExecutor.isSupport(mCurrentRecord.mScene.getClass(), mReturnRecord.mScene.getClass())) {
            navigationAnimationExecutor = mCurrentRecord.mNavigationAnimationExecutor;
        }

        if (navigationAnimationExecutor == null) {
            navigationAnimationExecutor = mNavigationScene.getDefaultNavigationAnimationExecutor();
        }

        final boolean isNavigationSceneInAnimationState = mNavigationScene.getState().value >= State.STARTED.value;

        if (!this.mManagerAbility.isDisableNavigationAnimation() && isNavigationSceneInAnimationState && navigationAnimationExecutor != null && navigationAnimationExecutor.isSupport(mCurrentRecord.mScene.getClass(), mReturnRecord.mScene.getClass())) {
            if (currentSceneView == null) {
                throw new SceneInternalException("Current Scene view can't be null");
            }

            ViewGroup animationContainer = mNavigationScene.getAnimationContainer();
            // Ensure that the Z-axis is correct
            AnimatorUtility.bringAnimationViewToFrontIfNeeded(mNavigationScene);
            navigationAnimationExecutor.setAnimationViewGroup(animationContainer);

            final CancellationSignalList cancellationSignalList = new CancellationSignalList();
            final Runnable endAction = new Runnable() {
                @Override
                public void run() {
                    mManagerAbility.getCancellationSignalManager().remove(cancellationSignalList);
                    mNavigationScene.addToReuseCache(mCurrentRecord.mScene);
                    mManagerAbility.notifyNavigationAnimationEnd(mCurrentScene, mReturnRecord.mScene, false);
                    operationEndAction.run();
                }
            };

            final AnimationInfo fromInfo = new AnimationInfo(mCurrentScene, currentSceneView, mCurrentScene.getState(), mCurrentRecord.mIsTranslucent);
            final AnimationInfo toInfo = new AnimationInfo(mReturnRecord.mScene, mReturnRecord.mScene.getView(), mReturnRecord.mScene.getState(), mReturnRecord.mIsTranslucent);

            this.mManagerAbility.getCancellationSignalManager().add(cancellationSignalList);
            /*
             * In the extreme case of Pop immediately after Push,
             * We are likely to executed pop() before the layout() of the View being pushing.
             * At this time, both height and width are 0, and it has no parent.
             * As the animation cannot be executed, so we need to correct this case.
             */
            navigationAnimationExecutor.executePopChange(mNavigationScene,
                    mNavigationScene.getView().getRootView(),
                    fromInfo, toInfo, cancellationSignalList, mManagerAbility::suppressRecycle, endAction);
        } else {
            mNavigationScene.addToReuseCache(mCurrentRecord.mScene);
            operationEndAction.run();
        }
    }
}
