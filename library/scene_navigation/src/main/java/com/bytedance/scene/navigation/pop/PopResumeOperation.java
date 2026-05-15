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

import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.interfaces.Function;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneManager;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;

import java.util.List;

/**
 * Created by jiangqi on 2023/11/19
 *
 * @author jiangqi@bytedance.com
 */
public class PopResumeOperation implements Operation {
    private static final String TAG = "PopResumeOperation";
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationScene mNavigationScene;
    private final Record currentRecord;
    private final Record returnRecord;
    @Nullable
    private final Function<Scene, Void> afterOnActivityCreatedAction;

    public PopResumeOperation(NavigationManagerAbility navigationManagerAbility, Record currentRecord, Record returnRecord, Function<Scene, Void> afterOnActivityCreatedAction) {
        this.mManagerAbility = navigationManagerAbility;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.currentRecord = currentRecord;
        this.returnRecord = returnRecord;
        this.afterOnActivityCreatedAction = afterOnActivityCreatedAction;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        LoggerManager.getInstance().i(TAG, "invoke execute operation");
        Scene dstScene = returnRecord.mScene;
        //When Scene has created View, compare its cached Configuration to the latest Configuration, if it is changed, recreate it
        boolean recreated = mManagerAbility.dispatchOnConfigurationChangedToRecord(returnRecord, dstScene);
        if (recreated) {
            //new scene instance is created
            dstScene = returnRecord.mScene;
        }
        final State dstState = mNavigationScene.getState();

        if (afterOnActivityCreatedAction != null) {
            if (mManagerAbility.isOnlyRestoreVisibleScene() && dstScene.getState().value < State.ACTIVITY_CREATED.value) {
                Bundle dstScenePreviousDstSavedState = returnRecord.mPreviousSavedState;
                returnRecord.mPreviousSavedState = null;
                //Scene is destroyed, so schedule onNewIntent after onActivityCreated
                mManagerAbility.moveState(mNavigationScene, dstScene, dstState, dstScenePreviousDstSavedState, false, afterOnActivityCreatedAction, null);
            } else {
                afterOnActivityCreatedAction.apply(dstScene);
                mManagerAbility.moveState(mNavigationScene, dstScene, dstState, null, false, null);
            }
        } else {
            if (mManagerAbility.isOnlyRestoreVisibleScene()) {
                Bundle dstScenePreviousDstSavedState = returnRecord.mPreviousSavedState;
                returnRecord.mPreviousSavedState = null;
                mManagerAbility.moveState(mNavigationScene, dstScene, dstState, dstScenePreviousDstSavedState, false, null);
            } else {
                mManagerAbility.moveState(mNavigationScene, dstScene, dstState, null, false, null);
            }
        }

        // Ensure that the requesting Scene is correct
        if (currentRecord.mPushResultCallback != null && !mNavigationScene.isFixOnResultTiming()) {
            this.mManagerAbility.obtainNavigationResultActionHandler().deliverResultLegacy(currentRecord);
        }

        /*
         * In case of multiple translucent overlays of an opaque Scene,
         * after returning, it is necessary to set the previous translucent Scene to STARTED
         */
        //TODO other visible scenes should also deal with onConfigurationChanged
        if (returnRecord.mIsTranslucent) {
            final List<Record> currentRecordList = mManagerAbility.getCurrentRecordList();
            if (currentRecordList.size() > 1) {
                int index = currentRecordList.indexOf(returnRecord);
                if (index > 0) {
                    for (int i = index - 1; i >= 0; i--) {
                        Record record = currentRecordList.get(i);
                        if (mManagerAbility.isOnlyRestoreVisibleScene()) {
                            mManagerAbility.moveState(mNavigationScene, record.mScene, NavigationSceneManager.findMinState(mNavigationScene.getState(), State.STARTED), record.mPreviousSavedState, false, null);
                            record.mPreviousSavedState = null;
                        } else {
                            mManagerAbility.moveState(mNavigationScene, record.mScene, NavigationSceneManager.findMinState(mNavigationScene.getState(), State.STARTED), null, false, null);
                        }
                        if (!record.mIsTranslucent) {
                            break;
                        }
                    }
                }
            }
        }

        operationEndAction.run();
    }
}
