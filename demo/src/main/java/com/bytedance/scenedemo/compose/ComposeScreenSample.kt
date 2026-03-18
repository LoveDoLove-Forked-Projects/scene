package com.bytedance.scenedemo.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleEventObserver
import com.bytedance.scene.navigation.compose.ComposeScreen
import com.bytedance.scene.navigation.compose.LocalNavigationScene
import com.bytedance.scene.navigation.compose.LocalResultReceiver
import com.bytedance.scene.navigation.compose.LocalScreenArguments
import com.bytedance.scene.navigation.compose.pushCompose
import com.google.accompanist.appcompattheme.AppCompatTheme

class ComposeScreenSample : ComposeScreen() {
    @Composable
    override fun Content() {
        val arguments = LocalScreenArguments.current
        val resultReceiver = LocalResultReceiver.current
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->

            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        AppCompatTheme {
            Greeting()
        }
    }

    @Composable
    private fun Greeting() {
        val navigationScene = LocalNavigationScene.current

        Text(
            text = "Test",
            modifier = Modifier.Companion.padding(horizontal = 10.dp, vertical = 5.dp)
        )

        Button(onClick = {
            navigationScene.pushCompose(ComposeScreenSample().apply {
                setArguments(null)
            })
        }) {
            Text("Click")
        }
    }
}