package com.bytedance.scenedemo.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import com.bytedance.scene.navigation.compose.ComposeScreen
import com.bytedance.scene.navigation.compose.LocalNavigationScene
import com.bytedance.scene.navigation.compose.LocalResultReceiver
import com.bytedance.scene.navigation.compose.LocalScreenArguments
import com.bytedance.scene.navigation.compose.pushCompose

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
        AppTheme {
            SimpleScaffold()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SimpleScaffold() {
        val navigationScene = LocalNavigationScene.current
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Sample Compose Page")
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            navigationScene.pop()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Menu")
                        }
                    },
                )
            }) { it ->
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Button(onClick = {
                    navigationScene.pushCompose(ComposeScreenSample().apply {
                        setArguments(null)
                    })
                }) {
                    Text("Click Me")
                }
            }
        }
    }

    @Composable
    fun AppTheme(content: @Composable () -> Unit) {
        val dark = isSystemInDarkTheme()
        MaterialTheme(
            colorScheme = if (dark) darkColorScheme() else lightColorScheme(), content = content
        )
    }
}