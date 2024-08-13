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
package com.bytedance.scene.ktx

import androidx.annotation.MainThread
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior
import com.bytedance.scene.navigation.ActivityCompatibleInfoCollector
import com.bytedance.scene.navigation.NavigationScene
import com.bytedance.scene.navigation.NavigationSceneGetter

val Scene.navigationScene: NavigationScene?
    get() {
        return NavigationSceneGetter.getNavigationScene(this)
    }

fun Scene.requireNavigationScene(): NavigationScene {
    return NavigationSceneGetter.requireNavigationScene(this)
}

@MainThread
fun <T> T.activityAttributes(
    init: ActivityAttributes.() -> Unit
) where T : Scene, T : ActivityCompatibleBehavior {
    val activityAttributes = ActivityAttributes()
    init.invoke(activityAttributes)
    if (activityAttributes.configChanges == -1) {
        return
    }
    val data = ActivityCompatibleInfoCollector.getOrCreateHolder(this)
    if (data.configChanges != null) {
        throw IllegalArgumentException("activityAttributes can't invoke more than once")
    }
    val configChanges = activityAttributes.configChanges
    if (configChanges != -1) {
        data.configChanges = configChanges
    }
}