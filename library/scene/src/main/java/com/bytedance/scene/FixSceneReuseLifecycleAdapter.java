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
package com.bytedance.scene;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;

import androidx.lifecycle.LifecycleRegistry;

import java.util.ArrayList;
import java.util.List;

class FixSceneReuseLifecycleAdapter extends Lifecycle {
    private final LifecycleRegistry lifecycleRegistry;
    private final List<LifecycleObserver> lifecycleObservers = new ArrayList<>();

    FixSceneReuseLifecycleAdapter(LifecycleRegistry lifecycleRegistry) {
        this.lifecycleRegistry = lifecycleRegistry;
    }

    @Override
    public void addObserver(@NonNull LifecycleObserver observer) {
        if (observer == null) {
            return;
        }

        this.lifecycleObservers.add(observer);
        this.lifecycleRegistry.addObserver(observer);
    }

    @Override
    public void removeObserver(@NonNull LifecycleObserver observer) {
        if (observer == null) {
            return;
        }

        this.lifecycleObservers.remove(observer);
        this.lifecycleRegistry.removeObserver(observer);
    }

    @NonNull
    @Override
    public State getCurrentState() {
        return this.lifecycleRegistry.getCurrentState();
    }

    void handleLifecycleEvent(@NonNull Lifecycle.Event event) {
        this.lifecycleRegistry.handleLifecycleEvent(event);
    }

    int getObserverCount() {
        return lifecycleObservers.size();
    }

    void rest() {
        // Otherwise it will loop endless
        for (LifecycleObserver lifecycleObserver : lifecycleObservers) {
            this.lifecycleRegistry.removeObserver(lifecycleObserver);
        }
        this.lifecycleRegistry.markState(Lifecycle.State.INITIALIZED);
        for (LifecycleObserver lifecycleObserver : lifecycleObservers) {
            this.lifecycleRegistry.addObserver(lifecycleObserver);
        }
    }
}