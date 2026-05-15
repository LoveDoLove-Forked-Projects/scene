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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.ThreadUtility;

import java.util.WeakHashMap;

/**
 * Created by jiangqi on 2024/7/4
 *
 * @author jiangqi@bytedance.com
 * @hide
 */
@RestrictTo(LIBRARY)
public class ActivityCompatibleInfoCollector {

    public static class Holder {
        public Integer configChanges;
    }

    private static final WeakHashMap<Scene, Holder> sSceneHolderWeakHashMap = new WeakHashMap<>();

    @Nullable
    public static Holder getHolder(@NonNull Scene scene) {
        if (scene == null) {
            throw new NullPointerException("Scene can't be null");
        }
        if (!isTargetSceneType(scene)) {
            return null;
        }
        if (sSceneHolderWeakHashMap.size() == 0) {
            return null;
        }
        return sSceneHolderWeakHashMap.get(scene);
    }

    @NonNull
    public static Holder getOrCreateHolder(@NonNull Scene scene) {
        if (scene == null) {
            throw new NullPointerException("Scene can't be null");
        }
        if (!isTargetSceneType(scene)) {
            throw new NullPointerException("Scene must implement ActivityCompatibleBehavior");
        }

        if (!SceneInstanceUtility.isConstructorMethodSupportRestore(scene)) {
            throw new IllegalArgumentException("Scene " + scene.getClass().getName() + " must be a public class or public static class, " + "and have only one parameterless constructor to be properly recreated to support Configuration changed.");
        }

        ThreadUtility.checkUIThread();
        Holder holder = sSceneHolderWeakHashMap.get(scene);
        if (holder == null) {
            holder = new Holder();
            sSceneHolderWeakHashMap.put(scene, holder);
        }
        return holder;
    }

    public static void clearHolder(@NonNull Scene scene) {
        if (!isTargetSceneType(scene)) {
            return;
        }
        ThreadUtility.checkUIThread();
        if (sSceneHolderWeakHashMap.size() > 0) {
            sSceneHolderWeakHashMap.remove(scene);
        }
    }

    public static boolean containsConfigChanges(@NonNull Scene scene) {
        if (scene == null) {
            throw new NullPointerException("Scene can't be null");
        }
        if (!isTargetSceneType(scene)) {
            return false;
        }
        if (sSceneHolderWeakHashMap.size() == 0) {
            return false;
        }
        Holder holder = sSceneHolderWeakHashMap.get(scene);
        return holder != null && holder.configChanges != null;
    }

    public static boolean isTargetSceneType(@NonNull Scene scene){
        return scene instanceof ActivityCompatibleBehavior;
    }
}
