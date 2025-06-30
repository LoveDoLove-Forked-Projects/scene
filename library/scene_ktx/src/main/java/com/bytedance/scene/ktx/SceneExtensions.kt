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

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bytedance.scene.ActivityCompatibilityUtility
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.ActivityResultCallback
import com.bytedance.scene.interfaces.PermissionResultCallback
import com.bytedance.scene.navigation.ConfigurationChangedListener

@Suppress("all")
private val HANDLER by lazy { Handler(Looper.getMainLooper()) }

fun Scene.post(runnable: Runnable) {
    if (this.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return
    }

    val wrappedRunnable = wrapRunnable(runnable)
    HANDLER.post(wrappedRunnable)
}

fun Scene.postDelayed(runnable: Runnable, delayMillis: Long) {
    if (this.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return
    }

    val wrappedRunnable = wrapRunnable(runnable)
    HANDLER.postDelayed(wrappedRunnable, delayMillis)
}

private fun Scene.wrapRunnable(runnable: Runnable): Runnable {
    var removeCallback: (() -> Unit)? = null

    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                this@wrapRunnable.lifecycle.removeObserver(this)
                removeCallback?.invoke()
            }
        }
    }
    this.lifecycle.addObserver(observer)

    val wrappedRunnable = Runnable {
        runnable.run()
        this.lifecycle.removeObserver(observer)
        removeCallback = null
    }
    removeCallback = {
        HANDLER.removeCallbacks(wrappedRunnable)
    }
    return wrappedRunnable
}

/**
 * Actually it is equal to [Scene.isViewDestroyed]
 */
@Deprecated("It is better to use Scene.isViewDestroyed()")
fun Scene.isDestroyed(): Boolean {
    return this.lifecycle.currentState == Lifecycle.State.DESTROYED
}

fun Scene.fragmentActivity(): FragmentActivity? {
    return activity as FragmentActivity?
}

fun Scene.requireFragmentActivity(): FragmentActivity {
    return requireActivity() as FragmentActivity
}

@Deprecated("use Scene.startActivityForResult(Intent, Int, (Int, Intent?) -> Unit)) instead")
fun Scene.startActivityForResult(intent: Intent, requestCode: Int, resultCallback: ActivityResultCallback) {
    activity?.let {
        ActivityCompatibilityUtility.startActivityForResult(it, this, intent, requestCode, resultCallback)
    }
}

@Deprecated("use Scene.requestPermissions(Array<String>, Int, (IntArray?) -> Unit)) instead")
@RequiresApi(Build.VERSION_CODES.M)
fun Scene.requestPermissions(permissions: Array<String>, requestCode: Int, resultCallback: PermissionResultCallback) {
    activity?.let {
        ActivityCompatibilityUtility.requestPermissions(it, this, permissions, requestCode, resultCallback)
    }
}

@Deprecated("use Scene.addConfigurationChangedListener((Configuration) -> Unit)) instead")
fun Scene.addConfigurationChangedListener(configurationChangedListener: ConfigurationChangedListener) {
    activity?.let {
        ActivityCompatibilityUtility.addConfigurationChangedListener(it, this, configurationChangedListener)
    }
}

fun Scene.startActivityForResult(intent: Intent, requestCode: Int, resultCallback: (Int, Intent?) -> Unit) {
    activity?.let {
        ActivityCompatibilityUtility.startActivityForResult(it, this, intent, requestCode,
                ActivityResultCallback { resultCode: Int, result: Intent? ->
                    resultCallback(resultCode, result)
                })
    }
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun Scene.startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?, resultCallback: (Int, Intent?) -> Unit) {
    activity?.let {
        ActivityCompatibilityUtility.startActivityForResult(it, this, intent, requestCode, options,
                ActivityResultCallback { resultCode: Int, result: Intent? ->
                    resultCallback(resultCode, result)
                })
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun Scene.requestPermissions(permissions: Array<String>, requestCode: Int, resultCallback: (IntArray?) -> Unit) {
    activity?.let {
        ActivityCompatibilityUtility.requestPermissions(it, this, permissions, requestCode,
                PermissionResultCallback { grantResults ->
                    resultCallback(grantResults)
                })
    }
}

fun Scene.addConfigurationChangedListener(configurationChangedListener: (Configuration) -> Unit) {
    activity?.let {
        ActivityCompatibilityUtility.addConfigurationChangedListener(it, this,
                ConfigurationChangedListener { newConfig ->
                    configurationChangedListener(newConfig)
                })
    }
}