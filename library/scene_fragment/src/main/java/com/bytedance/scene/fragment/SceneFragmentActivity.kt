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

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bytedance.scene.NavigationSceneUtility
import com.bytedance.scene.SceneDelegate

abstract class SceneFragmentActivity : AppCompatActivity() {
    private var mDelegate: SceneDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                (window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
        val arguments = getHomeFragmentArguments(intent)
        this.mDelegate = NavigationSceneUtility.setupWithActivity(
            this, FragmentScene::class.java
        ).rootSceneArguments(arguments).supportRestore(supportRestore())
            .onlyRestoreVisibleScene(true)
            .rootSceneComponentFactory { cl, className, bundle ->
                FragmentScene.newInstance(
                    homeFragmentClass, bundle
                )
            }.build()
    }

    override fun onBackPressed() {
        if (!mDelegate!!.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val navigationScene = mDelegate!!.navigationScene
        navigationScene?.onConfigurationChanged(newConfig)
    }

    protected abstract val homeFragmentClass: Class<out Fragment>

    protected abstract fun supportRestore(): Boolean

    protected fun getHomeFragmentArguments(intent: Intent?): Bundle? {
        return null
    }
}
