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

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.queue.NavigationMessageQueue;
import com.bytedance.scene.utlity.Predicate;

import java.util.List;

/**
 * Created by jiangqi on 2023/11/15
 *
 * @author jiangqi@bytedance.com
 */
public class CoordinatePopOptionOperation implements Operation {
    private static final String TAG = "CoordinatePopOptionOperation";
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationMessageQueue mMessageQueue;
    private final PopOptions mPopOptions;

    public CoordinatePopOptionOperation(NavigationManagerAbility navigationManagerAbility, NavigationMessageQueue messageQueue,
                                        PopOptions popOptions) {
        this.mManagerAbility = navigationManagerAbility;
        this.mMessageQueue = messageQueue;
        this.mPopOptions = popOptions;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        LoggerManager.getInstance().i(TAG, "invoke execute operation");
        List<Record> recordList = mManagerAbility.getCurrentRecordList();

        Predicate<Scene> popUtilPredicate = this.mPopOptions.getPopUtilPredicate();
        int count = 0;
        if (popUtilPredicate != null) {
            for (int i = recordList.size() - 1; i >= 0; i--) {
                Record record = recordList.get(i);
                if (popUtilPredicate.apply(record.mScene)) {
                    break;
                }
                count++;
            }
            new CoordinatePopCountOperation(mManagerAbility, mMessageQueue, mPopOptions.getNavigationAnimationExecutor(), count, mPopOptions).execute(operationEndAction);
        } else {
            new CoordinatePopOperation(mManagerAbility, mMessageQueue, mPopOptions.getNavigationAnimationExecutor(), mPopOptions).execute(operationEndAction);
        }
    }
}
