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

import android.view.View
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.PausableMonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bytedance.scene.Scene
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

fun Scene.createLifecycleAwareViewTreeRecomposer(view: View): Recomposer {
    val currentThreadContext = AndroidUiDispatcher.CurrentThread
    val pausableClock = currentThreadContext[MonotonicFrameClock]?.let {
        PausableMonotonicFrameClock(it).apply { pause() }
    }
    val contextWithClock = currentThreadContext + (pausableClock ?: EmptyCoroutineContext)
    val recomposer = Recomposer(contextWithClock)
    val runRecomposeScope = CoroutineScope(contextWithClock)
    val viewTreeLifecycleOwner = this
    // Removing the view holding the ViewTreeRecomposer means we may never be reattached again.
    // Since this factory function is used to create a new recomposer for each invocation and
    // doesn't reuse a single instance like other factories might, shut it down whenever it
    // becomes detached. This can easily happen as part of setting a new content view.
    view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {}
        override fun onViewDetachedFromWindow(v: View) {
            view.removeOnAttachStateChangeListener(
                this
            )
            recomposer.cancel()
        }
    })
    viewTreeLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event) {
            val self = this
            when (event) {
                Lifecycle.Event.ON_CREATE ->
                    // Undispatched launch since we've configured this scope
                    // to be on the UI thread
                    runRecomposeScope.launch(start = CoroutineStart.UNDISPATCHED) {
                        try {
                            recomposer.runRecomposeAndApplyChanges()
                        } finally {
                            // If runRecomposeAndApplyChanges returns or this coroutine is
                            // cancelled it means we no longer care about this lifecycle.
                            // Clean up the dangling references tied to this observer.
                            lifecycleOwner.lifecycle.removeObserver(self)
                        }
                    }

                Lifecycle.Event.ON_START -> pausableClock?.resume()
                Lifecycle.Event.ON_STOP -> pausableClock?.pause()
                Lifecycle.Event.ON_DESTROY -> {
                    recomposer.cancel()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    // Nothing
                }

                Lifecycle.Event.ON_RESUME -> {
                    // Nothing
                }

                Lifecycle.Event.ON_ANY -> {
                    // Nothing
                }
            }
        }
    })
    return recomposer
}
