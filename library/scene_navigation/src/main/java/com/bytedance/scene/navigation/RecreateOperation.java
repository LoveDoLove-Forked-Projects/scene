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
package com.bytedance.scene.navigation;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneStateSaveReason;
import com.bytedance.scene.State;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.utlity.SceneInstanceUtility;

class RecreateOperation implements Operation {
    private static final String TAG = "RecreateOperation";
    private final NavigationScene mNavigationScene;
    private final NavigationManagerAbility mNavigationManager;
    private final RecordStack mBackStackList;
    private final Scene mScene;
    private final int mReason;

    RecreateOperation(NavigationScene navigationScene, NavigationManagerAbility navigationManager, RecordStack backStackList, @NonNull Scene scene, int reason) {
        this.mNavigationScene = navigationScene;
        this.mNavigationManager = navigationManager;
        this.mBackStackList = backStackList;
        this.mScene = scene;
        this.mReason = reason;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        if (this.mScene.getState() == State.NONE) {
            //Target scene is destroyed, skip
            if (operationEndAction != null) {
                operationEndAction.run();
            }
            return;
        }

        if (!mScene.isSceneRestoreEnabled()) {
            throw new IllegalArgumentException("Scene " + mScene.getClass().getName() + " don't support restore, so it can't use recreate");
        }
        if (!SceneInstanceUtility.isConstructorMethodSupportRestore(mScene)) {
            throw new IllegalArgumentException("Scene " + mScene.getClass().getName() + " must be a public class or public static class, " +
                    "and have only one parameterless constructor to be properly recreated.");
        }

        Record record = mBackStackList.getRecordByScene(this.mScene);
        State targetState = this.mScene.getState();

        LoggerManager.getInstance().i(TAG, "RecreateOperation current Scene save latest data, current Scene instance " + mScene.toString());

        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putInt(SceneStateSaveReason.KEY_SCENE_SAVE_STATE_REASON, this.mReason);
        this.mScene.dispatchSaveInstanceState(savedInstanceState);

        LoggerManager.getInstance().i(TAG, "RecreateOperation current Scene destroy itself, current Scene instance " + mScene.toString());
        this.mNavigationManager.moveState(mNavigationScene, this.mScene, State.NONE, null, false, null);

        Scene newSceneInstance = null;
        if (mBackStackList.isRootScene(this.mScene) && mNavigationScene.mRootSceneComponentFactory != null) {
            Scene sceneInstance = mNavigationScene.mRootSceneComponentFactory.instantiateScene(mNavigationScene.requireActivity().getClassLoader(), record.mSceneClassName, null);
            if (sceneInstance != null && sceneInstance.getParentScene() != null) {
                throw new IllegalArgumentException("SceneComponentFactory instantiateScene return Scene already has a parent");
            }
            if (sceneInstance != null) {
                LoggerManager.getInstance().i(TAG, "RecreateOperation create new Scene by SceneComponentFactory");
            }
            newSceneInstance = sceneInstance;
        }
        if (newSceneInstance == null) {
            LoggerManager.getInstance().i(TAG, "RecreateOperation create new Scene directly");
            Class<?> sceneClass = mScene.getClass();
            newSceneInstance = SceneInstanceUtility.getInstanceFromClass(sceneClass, null);
        }
        record.mScene = newSceneInstance;

        LoggerManager.getInstance().i(TAG, "RecreateOperation new created Scene restore from previous data, new Scene instance " + newSceneInstance.toString());
        this.mNavigationManager.moveState(mNavigationScene, newSceneInstance, targetState, savedInstanceState, false, null);

        if (operationEndAction != null) {
            operationEndAction.run();
        }
    }
}