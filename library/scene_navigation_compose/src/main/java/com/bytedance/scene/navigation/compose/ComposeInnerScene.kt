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
package com.bytedance.scene.navigation.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.CoordinateScheduleScene
import com.bytedance.scene.navigation.NavigationScene


//TODO performance，merge ComposeView?
class ComposeInnerScene : Scene(), CoordinateScheduleScene {
    private val SCENE_COMPOSE_SCREEN_CLASS_NAME = "bd-scene-nav:scene_compose_name"
    private val SCENE_COMPOSE_SCREEN_ARGUMENTS = "bd-scene-nav:scene_compose_name_arguments"
    internal var composeScreen: ComposeScreen? = null
    private var composeView: ComposeView? = null
    override fun onCreateView(p0: LayoutInflater, p1: ViewGroup, p2: Bundle?): View {
        return ComposeView(requireSceneContext()).apply {
            composeView = this
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (this.composeScreen == null && savedInstanceState != null) {
            val targetCompose = Class.forName(
                requireNotNull(
                    savedInstanceState.getString(
                        SCENE_COMPOSE_SCREEN_CLASS_NAME
                    )
                )
            ).newInstance() as ComposeScreen
            targetCompose.setArguments(savedInstanceState.getBundle(SCENE_COMPOSE_SCREEN_ARGUMENTS))
            this.composeScreen = targetCompose
        }

        val navigationScene = requireParentScene() as NavigationScene
        composeView?.setParentCompositionContext(createLifecycleAwareViewTreeRecomposer(requireView()))
        composeView?.setContent {
            CompositionLocalProvider(
                LocalNavigationScene provides navigationScene,
                LocalScreenArguments provides this.composeScreen?.arguments,
                LocalResultReceiver provides ComposeResultReceiver(navigationScene, this)
            ) {
                this.composeScreen?.Content()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            SCENE_COMPOSE_SCREEN_CLASS_NAME, requireNotNull(this.composeScreen).javaClass.name
        )
        outState.putBundle(
            SCENE_COMPOSE_SCREEN_ARGUMENTS, requireNotNull(this.composeScreen).arguments
        )
    }
}