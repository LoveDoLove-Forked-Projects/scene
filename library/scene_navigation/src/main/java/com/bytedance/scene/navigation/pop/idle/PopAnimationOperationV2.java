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
package com.bytedance.scene.navigation.pop.idle;

import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.navigation.SystemBarRestoreFlag;
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
public class PopAnimationOperationV2 implements Operation {
    private static final String TAG = "PopAnimationOperationV2";
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationAnimationExecutor mAnimationFactory;
    private final NavigationScene mNavigationScene;
    private final Record mCurrentRecord;
    private final Record mReturnRecord;
    private final Scene mCurrentScene;
    private final boolean mNotRemoveView;
    private final SystemBarRestoreFlag mSystemBarRestoreFlag;

    public final CancellationSignalList cancellationSignalList = new CancellationSignalList();

    public PopAnimationOperationV2(NavigationManagerAbility navigationManagerAbility, NavigationAnimationExecutor animationFactory, List<Record> destroyRecordList, Record currentRecord, Record returnRecord, Scene currentScene, boolean notRemoveView, SystemBarRestoreFlag systemBarRestoreFlag) {
        this.mManagerAbility = navigationManagerAbility;
        this.mAnimationFactory = animationFactory;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.mCurrentRecord = currentRecord;
        this.mReturnRecord = returnRecord;
        this.mCurrentScene = currentScene;
        this.mNotRemoveView = notRemoveView;
        this.mSystemBarRestoreFlag = systemBarRestoreFlag;
    }

    @Override
    public void execute(final Runnable operationEndAction) {
        LoggerManager.getInstance().i(TAG, "invoke execute operation");
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
            this.mManagerAbility.restoreActivityStatusBarNavigationBarStatus(mReturnRecord.mActivityStatusRecord);
            if (SceneGlobalConfig.onlyRestoreNonSystemBarAfterAnimation) {
                this.mSystemBarRestoreFlag.markHasRestored();
            }

            ViewGroup animationContainer = mNavigationScene.getAnimationContainer();
            // Ensure that the Z-axis is correct
            AnimatorUtility.bringAnimationViewToFrontIfNeeded(mNavigationScene);
            navigationAnimationExecutor.setDisableRemoveView(mNotRemoveView);
            navigationAnimationExecutor.setAnimationViewGroup(animationContainer);
            final Runnable endAction = new Runnable() {
                @Override
                public void run() {
                    mManagerAbility.getCancellationSignalManager().remove(cancellationSignalList);
                    mManagerAbility.notifyNavigationAnimationEnd(mCurrentScene, mReturnRecord.mScene, false);
                    operationEndAction.run();
                }
            };

            View currentSceneView = this.mCurrentScene.getView();
            if (currentSceneView == null) {
                throw new SceneInternalException("Current Scene view can't be null");
            }

            final AnimationInfo fromInfo = new AnimationInfo(mCurrentScene, currentSceneView, mCurrentScene.getState(), mCurrentRecord.mIsTranslucent);
            final AnimationInfo toInfo = new AnimationInfo(mReturnRecord.mScene, mReturnRecord.mScene.getView(), mReturnRecord.mScene.getState(), mReturnRecord.mIsTranslucent);

            this.mManagerAbility.getCancellationSignalManager().add(cancellationSignalList);
            /*
             * In the extreme case of Pop immediately after Push,
             * We are likely to executed pop() before the layout() of the View being pushing.
             * At this time, both height and width are 0, and it has no parent.
             * As the animation cannot be executed, so we need to correct this case.
             */
            navigationAnimationExecutor.executePopChange(mNavigationScene, mNavigationScene.getView().getRootView(), fromInfo, toInfo, cancellationSignalList, mManagerAbility::suppressRecycle, endAction);
        } else {
            operationEndAction.run();
        }
    }
}
