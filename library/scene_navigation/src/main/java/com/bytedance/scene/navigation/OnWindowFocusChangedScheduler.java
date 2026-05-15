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

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.content.res.Configuration;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.utlity.Action1;
import com.bytedance.scene.utlity.ConfigurationUtility;
import com.bytedance.scene.utlity.SceneInternalException;

/**
 * @hide
 */
@RestrictTo(LIBRARY)
class OnWindowFocusChangedScheduler {
    private static final String TAG = "OnWindowFocusChangedScheduler";

    //make sure Scene has invoked onCreateView, then dispatch onConfigurationChanged
    static boolean dispatchOnConfigurationChangedToRecordInternal(@NonNull Record record, @NonNull Scene scene, @NonNull Configuration newConfig, int configurationChangesAllowList, @NonNull Action1<Scene> recreateAction) {
        if (scene.getView() == null) {
            return false;
        }
        ActivityCompatibleInfoCollector.Holder holder = ActivityCompatibleInfoCollector.getHolder(scene);
        if (holder == null) {
            //skip, default behavior, nothing will happen
            return false;
        }
        Integer configChanges = holder.configChanges;
        if (holder.configChanges == null) {
            //skip, default behavior, nothing will happen
            return false;
        }
        if (newConfig.equals(record.mConfiguration)) {
            return false;
        }
        if (record.mConfiguration != null) {
            Configuration sceneConfiguration = record.mConfiguration;
            int diff = sceneConfiguration.diff(newConfig);
            LoggerManager.getInstance().i(TAG, "Configuration has been changed, raw diff " + diff);

            //remove private diff properties
            diff = ConfigurationUtility.removePrivateDiff(diff);
            if (configurationChangesAllowList != 0) {
                diff = (diff & configurationChangesAllowList);
                LoggerManager.getInstance().i(TAG, "clean diff not include in configurationChangesAllowList, result diff " + diff);
            }

            String diffString = ConfigurationUtility.configurationDiffToString(diff);
            LoggerManager.getInstance().i(TAG, "Configuration has been changed, diff " + diffString);

            if ((diff & configChanges) != 0) {
                LoggerManager.getInstance().i(TAG, "Configuration has been changed, Scene has suitable configChanges, so dispatch onConfigurationChanged to " + scene.toString());
                if (scene instanceof ActivityCompatibleBehavior) {
                    ((ActivityCompatibleBehavior) scene).onConfigurationChanged(newConfig);
                    record.saveActivityCompatibleInfo(newConfig);
                    return false;
                } else {
                    throw new SceneInternalException("Impossible, Scene don't implement ActivityCompatibleBehavior but have configChanges " + scene.toString());
                }
            } else {
                if (TextUtils.isEmpty(diffString)) {
                    LoggerManager.getInstance().i(TAG, "Configuration has been changed, skip because unknown diff " + diff);
                    if (scene instanceof ActivityCompatibleBehavior) {
                        ((ActivityCompatibleBehavior) scene).onConfigurationChanged(newConfig);
                        record.saveActivityCompatibleInfo(newConfig);
                        return false;
                    } else {
                        throw new SceneInternalException("Impossible, Scene don't implement ActivityCompatibleBehavior but have configChanges " + scene.toString());
                    }
                } else {
                    LoggerManager.getInstance().i(TAG, "Configuration has been changed, recreate " + scene.toString());
                }
            }
        } else {
            LoggerManager.getInstance().i(TAG, "Scene previous Configuration not found, recreate " + scene.toString());
        }
        recreateAction.execute(scene);
        return true;
    }

}
