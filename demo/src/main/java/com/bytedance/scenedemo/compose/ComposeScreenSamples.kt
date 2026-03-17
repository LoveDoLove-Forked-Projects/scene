package com.bytedance.scenedemo.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle
import com.bytedance.scene.navigation.compose.pushCompose

class ComposeScreenSamples : UserVisibleHintGroupScene() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?
    ): ViewGroup {
        val scrollView = ScrollView(requireSceneContext())

        val layout = LinearLayout(requireSceneContext())
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.main_scene_title_compose), View.OnClickListener {
            requireNavigationScene().pushCompose(ComposeScreenSample().apply {
                setArguments(null)
            })
        })

        addSpace(layout, 100)

        return scrollView
    }

}
