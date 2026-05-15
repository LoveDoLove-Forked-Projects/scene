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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/** @noinspection unused, FieldCanBeLocal */
class GcWatchers {
    private static WeakReference<GcWatcher> sGcWatcher = null;
    private static final ArrayList<Runnable> sGcWatchers = new ArrayList<>();
    private static Runnable[] sTmpWatchers = new Runnable[1];

    static final class GcWatcher {
        @Override
        protected void finalize() throws Throwable {
            synchronized (sGcWatchers) {
                sTmpWatchers = sGcWatchers.toArray(sTmpWatchers);
            }
            for (Runnable sTmpWatcher : sTmpWatchers) {
                if (sTmpWatcher != null) {
                    sTmpWatcher.run();
                }
            }
            if (!sGcWatchers.isEmpty()) {
                sGcWatcher = new WeakReference<>(new GcWatcher());
            }
        }
    }

    static void addGcWatcher(Runnable watcher) {
        synchronized (sGcWatchers) {
            sGcWatchers.add(watcher);
            if (sGcWatcher == null) {
                sGcWatcher = new WeakReference<>(new GcWatcher());
            }
        }
    }

    static void removeGcWatcher(Runnable watcher) {
        synchronized (sGcWatchers) {
            sGcWatchers.remove(watcher);
            if (sGcWatchers.isEmpty()) {
                sGcWatcher = null;
            }
        }
    }
}