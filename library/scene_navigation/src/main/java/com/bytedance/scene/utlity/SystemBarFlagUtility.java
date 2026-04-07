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
package com.bytedance.scene.utlity;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.RestrictTo;

@RestrictTo(LIBRARY_GROUP)
public class SystemBarFlagUtility {
    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public static int restoreTargetFlagFromRightToLeft(int leftSystemUiVisibility, int rightSystemUiVisibility, int targetFlag) {
        if ((leftSystemUiVisibility & targetFlag) != (rightSystemUiVisibility & targetFlag)) {
            //rightSystemUiVisibility has this targetFlag but leftSystemUiVisibility don't have
            if ((rightSystemUiVisibility & targetFlag) == targetFlag) {
                //leftSystemUiVisibility add targetFlag
                leftSystemUiVisibility = leftSystemUiVisibility | targetFlag;
            } else {
                //leftSystemUiVisibility remove targetFlag
                leftSystemUiVisibility = leftSystemUiVisibility & ~targetFlag;
            }
        }
        return leftSystemUiVisibility;
    }
}
