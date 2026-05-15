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
package com.bytedance.scene.launchmode;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior;

import java.util.List;

/**
 * Created by jiangqi on 2024/5/20
 *
 * @author jiangqi@bytedance.com
 * <p>
 * If you use SingleTaskLaunchModeBehavior, the Scene instance by yourself maybe be dropped
 */
public final class SingleTaskLaunchModeBehavior implements LaunchModeBehavior {
    private final TargetSceneFinder mTargetSceneFinder;
    private int mRemoveSceneCount = 0;

    public SingleTaskLaunchModeBehavior(@NonNull final Class targetClass) {
        this.mTargetSceneFinder = new TargetSceneFinder() {
            @Override
            public boolean isTargetScene(@NonNull Scene scene, @Nullable Bundle argument) {
                return scene.getClass() == targetClass;
            }
        };
    }

    public SingleTaskLaunchModeBehavior(@NonNull TargetSceneFinder targetSceneFinder) {
        this.mTargetSceneFinder = targetSceneFinder;
    }

    @Override
    public boolean onInterceptPushOperation(@NonNull List<Pair<Scene,Bundle>> sceneList) {
        if (sceneList.size() <= 0) {
            return false;
        }

        int targetIndex = -1;

        for (int i = sceneList.size() - 1; i >= 0; i--) {
            Pair<Scene, Bundle> pair = sceneList.get(i);
            Scene scene = pair.first;
            Bundle arguments = pair.second;
            if (this.mTargetSceneFinder.isTargetScene(scene, arguments)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex != -1) {
            this.mRemoveSceneCount = sceneList.size() - targetIndex - 1;
        }

        return targetIndex != -1;
    }

    @Override
    public int getPopSceneCount() {
        return this.mRemoveSceneCount;
    }

    @Override
    public void sceneOnNewIntent(@NonNull Scene scene, @Nullable Bundle arguments) {
        if (scene instanceof ActivityCompatibleBehavior) {
            ((ActivityCompatibleBehavior) scene).onNewIntent(arguments);
        }
    }
}
