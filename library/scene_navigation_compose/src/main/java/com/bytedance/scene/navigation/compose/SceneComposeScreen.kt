package com.bytedance.scene.navigation.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.ComposeView
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.CoordinateScheduleScene
import com.bytedance.scene.navigation.NavigationScene

val LocalNavigationScene = staticCompositionLocalOf<NavigationScene?> { null }
val LocalScreenArguments = staticCompositionLocalOf<Bundle?> { null }

//TODO performance，merge ComposeView?
class ComposeInnerScene : Scene(), CoordinateScheduleScene {
    private val SCENE_COMPOSE_SCREEN_CLASS_NAME = "bd-scene-nav:scene_compose_name"
    private val SCENE_COMPOSE_SCREEN_ARGUMENTS = "bd-scene-nav:scene_compose_name_arguments"
    internal var sceneComposeScreen: SceneComposeScreen? = null
    private var composeView: ComposeView? = null
    override fun onCreateView(p0: LayoutInflater, p1: ViewGroup, p2: Bundle?): View {
        return ComposeView(requireSceneContext()).apply {
            composeView = this
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (this.sceneComposeScreen == null && savedInstanceState != null) {
            val targetCompose = Class.forName(
                requireNotNull(
                    savedInstanceState.getString(
                        SCENE_COMPOSE_SCREEN_CLASS_NAME
                    )
                )
            ).newInstance() as SceneComposeScreen
            targetCompose.setArguments(savedInstanceState.getBundle(SCENE_COMPOSE_SCREEN_ARGUMENTS))
            this.sceneComposeScreen = targetCompose
        }

        val navigationScene = requireParentScene() as NavigationScene
        composeView?.setParentCompositionContext(createLifecycleAwareViewTreeRecomposer(requireView()))
        composeView?.setContent {
            CompositionLocalProvider(
                LocalNavigationScene provides navigationScene,
                LocalScreenArguments provides this.sceneComposeScreen?.arguments
            ) {
                this.sceneComposeScreen?.Content()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            SCENE_COMPOSE_SCREEN_CLASS_NAME, requireNotNull(this.sceneComposeScreen).javaClass.name
        )
        outState.putBundle(
            SCENE_COMPOSE_SCREEN_ARGUMENTS, requireNotNull(this.sceneComposeScreen).arguments
        )
    }
}

abstract class SceneComposeScreen() {
    internal var arguments: Bundle? = null

    @Composable
    abstract fun Content()

    fun setArguments(arguments: Bundle?) {
        this.arguments = arguments
    }
}

//TODO support other PushOptions abilities, like PushResultCallback
fun NavigationScene.pushCompose(screen: SceneComposeScreen) {
    val scene = ComposeInnerScene()
    scene.sceneComposeScreen = screen
    this.push(scene, null)
}