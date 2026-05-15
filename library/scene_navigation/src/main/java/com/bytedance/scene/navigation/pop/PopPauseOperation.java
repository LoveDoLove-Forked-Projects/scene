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

import android.os.Bundle;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.State;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.utlity.SceneInternalException;

/**
 * Created by jiangqi on 2023/11/19
 *
 * @author jiangqi@bytedance.com
 */
public class PopPauseOperation implements Operation {
    private static final String TAG = "PopPauseOperation";
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationScene mNavigationScene;
    private final Scene mCurrentScene;
    private final Record mCurrentRecord;

    public PopPauseOperation(NavigationManagerAbility navigationManagerAbility, Record currentRecord, Scene currentScene) {
        this.mManagerAbility = navigationManagerAbility;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.mCurrentRecord = currentRecord;
        this.mCurrentScene = currentScene;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        LoggerManager.getInstance().i(TAG, "invoke execute operation");
        /*
         * The practice here should be to remove those Scenes in the middle,
         * then animate the two Scenes.
         */
        if (mCurrentScene != null) {
            Bundle previousSavedState = null;
            previousSavedState = this.mCurrentRecord.consumeSavedInstanceState();
            if (previousSavedState != null && this.mCurrentScene.getState() != State.NONE) {
                throw new SceneInternalException("Scene' previous saved state still exists when its state is " + this.mCurrentScene.getState().name);
            }
            this.mManagerAbility.moveState(this.mNavigationScene, this.mCurrentScene, State.STARTED, previousSavedState, false, null);
        }

        operationEndAction.run();
    }
}
