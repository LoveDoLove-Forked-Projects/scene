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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class CancellationSignalManager {
    private final List<CancellationSignalList> cancelableList = new ArrayList<>();

    public void cancelAllRunningAnimationExecutor() {
        if (cancelableList.size() == 0) {
            return;
        }

        List<CancellationSignalList> copy = new ArrayList<>(cancelableList);
        Iterator<CancellationSignalList> iterator = copy.iterator();
        while (iterator.hasNext()) {
            CancellationSignalList cancellationSignal = iterator.next();
            iterator.remove();
            cancellationSignal.cancel();
        }
        cancelableList.removeAll(copy);
    }

    public void add(CancellationSignalList cancellationSignalList) {
        this.cancelableList.add(cancellationSignalList);
    }

    public void remove(CancellationSignalList cancellationSignalList) {
        this.cancelableList.remove(cancellationSignalList);
    }
}