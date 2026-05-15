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

import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.navigation.SystemBarRestoreFlag;

/**
 * Created by jiangqi on 2023/11/19
 *
 * @author jiangqi@bytedance.com
 */
public class PopDestroyOperationV2 implements Operation {
    private static final String TAG = "PopDestroyOperationV2";
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationScene mNavigationScene;
    private final Record mCurrentRecord;
    private final Record mReturnRecord;
    private final SystemBarRestoreFlag mSystemBarRestoreFlag;

    public PopDestroyOperationV2(NavigationManagerAbility navigationManagerAbility, Record currentRecord, Record returnRecord,
                                 SystemBarRestoreFlag systemBarRestoreFlag) {
        this.mManagerAbility = navigationManagerAbility;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.mCurrentRecord = currentRecord;
        this.mReturnRecord = returnRecord;
        this.mSystemBarRestoreFlag = systemBarRestoreFlag;
    }

    @Override
    public void execute(final Runnable operationEndAction) {
        LoggerManager.getInstance().i(TAG, "invoke execute operation");
        mManagerAbility.destroyByRecord(mCurrentRecord, mCurrentRecord);

        // Ensure that the requesting Scene is correct
        if (mCurrentRecord.mPushResultCallback != null && mNavigationScene.isFixOnResultTiming()) {
            this.mManagerAbility.obtainNavigationResultActionHandler().deliverResultLegacy(mCurrentRecord);
        }

        this.mManagerAbility.obtainNavigationResultActionHandler().deliverResult(mReturnRecord);

        if (SceneGlobalConfig.onlyRestoreNonSystemBarAfterAnimation && this.mSystemBarRestoreFlag != null && this.mSystemBarRestoreFlag.hasAlreadyRestoreSystemBar()) {
            this.mManagerAbility.restoreNonSystemBarStatus(mReturnRecord.mActivityStatusRecord);
        } else {
            this.mManagerAbility.restoreActivityStatus(mReturnRecord.mActivityStatusRecord);
        }
        this.mManagerAbility.getNavigationListener().navigationChange(mCurrentRecord.mScene, mReturnRecord.mScene, false);
        mNavigationScene.addToReuseCache(mCurrentRecord.mScene);
    }
}
