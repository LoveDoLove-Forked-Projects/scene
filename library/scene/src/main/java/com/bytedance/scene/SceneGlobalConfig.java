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

/**
 * Created by fengminchao on 2023/10/18
 *
 * @author fengminchao@bytedance.com
 */
public class SceneGlobalConfig {
    public static volatile boolean validateScopeAndViewModelStoreSceneClassStrategy = false;
    public static volatile boolean sceneLifecycleCallbackObjectCreationOpt = false;
    public static volatile boolean cancelAnimationWhenForceExecutePendingNavigationOperation = false;
    public static volatile boolean useStrictPublishResultCallbackEnabled = false;
    public static volatile boolean onlyRestoreNonSystemBarAfterAnimation = false;
    public static volatile boolean forceUseWindowInsetsToDetectIMEStatus = false;
}