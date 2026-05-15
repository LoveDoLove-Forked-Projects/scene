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

import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;

import java.util.List;

/**
 * Created by jiangqi on 2023/11/19
 *
 * @author jiangqi@bytedance.com
 */
public class PopDestroyMiddlePageOperationV2 implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final List<Record> mDestroyRecordList;
    private final Record mCurrentRecord;

    public PopDestroyMiddlePageOperationV2(NavigationManagerAbility navigationManagerAbility, List<Record> destroyRecordList, Record currentRecord) {
        this.mManagerAbility = navigationManagerAbility;
        this.mDestroyRecordList = destroyRecordList;
        this.mCurrentRecord = currentRecord;
    }

    @Override
    public void execute(final Runnable operationEndAction) {
        for (final Record record : this.mDestroyRecordList) {
            if (record == mCurrentRecord) {
                continue;
            }
            this.mManagerAbility.destroyByRecord(record, mCurrentRecord);
        }
        if (operationEndAction != null) {
            operationEndAction.run();
        }
    }
}
