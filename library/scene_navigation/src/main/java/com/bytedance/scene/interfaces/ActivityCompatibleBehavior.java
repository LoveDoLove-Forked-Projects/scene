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
package com.bytedance.scene.interfaces;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;

/**
 * Created by jiangqi on 2024/5/21
 *
 * @author jiangqi@bytedance.com
 */
public interface ActivityCompatibleBehavior {
    void onConfigurationChanged(@NonNull Configuration newConfig);

    /**
     * <p>An scene can never receive a new intent in the resumed state. You can count on
     * {@link Scene#onResume} being called after this method, though not necessarily immediately after
     * the completion this callback. If the scene was resumed, it will be paused and new arguments
     * will be delivered, followed by {@link Scene#onResume}. If the scene wasn't in the resumed
     * state, then new arguments can be delivered immediately, with {@link Scene#onResume()} called
     * sometime later when scene becomes active again.
     *
     * <p>Note that {@link Scene#getArguments} still returns the original arguments.  You
     * can use {@link Scene#setArguments} to update it to this new arguments.
     *
     * @param arguments The new arguments that was started for the scene.
     * @see Scene#getArguments()
     * @see Scene#setArguments
     * @see Scene#onResume
     */
    void onNewIntent(@Nullable Bundle arguments);

    void onWindowFocusChanged(boolean hasFocus);
}
