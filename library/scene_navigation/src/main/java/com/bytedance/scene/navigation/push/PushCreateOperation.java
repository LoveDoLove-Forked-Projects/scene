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
package com.bytedance.scene.navigation.push;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.navigation.SceneTranslucent;

public class PushCreateOperation implements Operation {
    private static final String TAG = "PushCreateOperation";
    private final NavigationManagerAbility mManagerAbility;
    private final Scene mScene;
    private final PushOptions mPushOptions;
    private final boolean mIsSceneTranslucent;
    private final NavigationScene mNavigationScene;

    public PushCreateOperation(NavigationManagerAbility managerAbility, Scene scene, PushOptions pushOptions) {
        this.mManagerAbility = managerAbility;
        this.mNavigationScene = managerAbility.getNavigationScene();
        this.mScene = scene;
        this.mPushOptions = pushOptions;
        this.mIsSceneTranslucent = pushOptions.isIsTranslucent() || scene instanceof SceneTranslucent;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        LoggerManager.getInstance().i(TAG, "invoke execute operation");
        //add new scene to record list
        NavigationAnimationExecutor animationFactory = this.mPushOptions.getNavigationAnimationFactory();
        Record record = Record.newInstance(this.mScene, this.mIsSceneTranslucent, animationFactory);
        this.mManagerAbility.obtainNavigationResultActionHandler().saveCallback(record, this.mPushOptions);

        this.mManagerAbility.pushRecord(record);

        /*
         * TODO: In fact, it is need to support that moveState to the specified state.
         *       Because of the destruction restore, it is impossible to go directly to RESUMED
         */

        mManagerAbility.moveState(mNavigationScene, mScene, mNavigationScene.getState(), null, false, null);
        operationEndAction.run();
    }
}