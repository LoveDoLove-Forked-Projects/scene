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

import android.os.Bundle;
import android.text.TextUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by JiangQi on 9/11/18.
 */
public class Scope {
    public interface Scoped {
        void onUnRegister();
    }

    public interface RootScopeFactory {
        @NonNull
        Scope getRootScope();
    }

    public static final RootScopeFactory DEFAULT_ROOT_SCOPE_FACTORY = new RootScopeFactory() {
        @Override
        public Scope getRootScope() {
            return new Scope(null, generateScopeKey(null));
        }
    };

    private final Scope mParentScope;
    private final String mScopeKey;
    private final Map<String, Scope> mChildrenScopes = new HashMap<>();
    private final Map<Object, Object> mServices = new HashMap<>();

    @NonNull
    Scope buildScope(@NonNull Scene scene, @Nullable Bundle bundle) {
        String scopeKey = null;
        if (bundle != null) {
            scopeKey = getScopeKeyFromBundle(bundle);
        }
        if (TextUtils.isEmpty(scopeKey)) {
            scopeKey = generateScopeKey(scene);
        }
        Scope scope = this.mChildrenScopes.get(scopeKey);
        if (scope == null) {
            scope = new Scope(this, scopeKey);
            this.mChildrenScopes.put(scopeKey, scope);
        }
        return scope;
    }

    private void removeChildScope(String scopeKey) {
        this.mChildrenScopes.remove(scopeKey);
    }

    private Scope(Scope parentScope, String scopeKey) {
        this.mParentScope = parentScope;
        this.mScopeKey = scopeKey;
    }

    public void register(@NonNull Object key, @NonNull Object service) {
        this.mServices.put(key, service);
    }

    public void unRegister(@NonNull Object key) {
        Object value = this.mServices.get(key);
        if (value != null) {
            if (value instanceof Scoped) {
                ((Scoped) value).onUnRegister();
            }
            this.mServices.remove(key);
        }
    }

    public boolean hasServiceInMyScope(@NonNull Object key) {
        return mServices.containsKey(key);
    }

    @Nullable
    public <T> T getService(@NonNull Object key) {
        Object value = mServices.get(key);
        if (value != null) {
            return (T) value;
        } else if (mParentScope != null) {
            return mParentScope.getService(key);
        } else {
            return null;
        }
    }

    private static final AtomicInteger SCENE_COUNT = new AtomicInteger(0);
    private static final String TAG_SCENE_SCOPE_KEY = "scope_key";

    private static String generateScopeKey(@Nullable Scene scene) {
        switch (SceneGlobalConfig.INSTANCE.getGenScopeStrategy()) {
            case 1: {
                if (scene == null) {
                    return "Scene_" + UUID.randomUUID();
                } else {
                    return scene.getClass().getName() + "_" + UUID.randomUUID();
                }
            }
            default: {
                return "Scene #" + SCENE_COUNT.getAndIncrement();
            }
        }
    }

    private static String getScopeKeyFromBundle(@NonNull Bundle bundle) {
        return bundle.getString(TAG_SCENE_SCOPE_KEY);
    }

    public void saveInstance(@NonNull Bundle bundle) {
        bundle.putString(TAG_SCENE_SCOPE_KEY, mScopeKey);
    }

    void destroy() {
        if (mParentScope != null) {
            mParentScope.removeChildScope(this.mScopeKey);
        }

        Collection<Object> values = mServices.values();
        for (Object value : values) {
            if (value instanceof Scoped) {
                ((Scoped) value).onUnRegister();
            }
        }
        mServices.clear();
        mChildrenScopes.clear();
    }
}
