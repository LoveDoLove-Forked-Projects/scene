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

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.navigation.NavigationScene

fun NavigationScene.push(fragment: Fragment, pushOptions: PushOptions? = null) {
    val scene = FragmentScene.newInstance(fragment.javaClass, fragment.arguments)
    scene.rootFragment = fragment
    push(scene, pushOptions)
}

fun NavigationScene.push(
    fragmentClass: Class<out Fragment>, arguments: Bundle? = null, pushOptions: PushOptions? = null
) {
    val scene = FragmentScene.newInstance(fragmentClass, arguments)
    push(scene, pushOptions)
}