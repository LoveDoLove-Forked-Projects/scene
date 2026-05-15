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
package com.bytedance.scene.fragment

import androidx.fragment.app.Fragment
import com.bytedance.scene.ktx.getNavigationScene
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scene.navigation.NavigationScene


/**
 * return the target NavigationScene which bind to current Fragment
 */
fun Fragment.requireNavigationScene(): NavigationScene {
    val view = this.view ?: throw IllegalStateException("Fragment is not created")
    return view.requireNavigationScene()
}

fun Fragment.getNavigationScene(): NavigationScene? {
    val view = this.view
    return view?.getNavigationScene()
}

