package com.bytedance.scene.navigation.pop.idle;

import com.bytedance.scene.SceneGlobalConfig;
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
