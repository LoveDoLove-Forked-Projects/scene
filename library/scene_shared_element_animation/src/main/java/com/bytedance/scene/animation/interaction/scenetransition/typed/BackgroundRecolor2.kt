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
package com.bytedance.scene.animation.interaction.scenetransition.typed

import android.animation.IntEvaluator
import android.animation.TimeInterpolator
import android.graphics.drawable.ColorDrawable
import android.view.View

class BackgroundColorValueCaptor : AnimationValueCaptor<View, Int> {
    override fun capture(
        animateView: View, targetView: View
    ): Int? {
        if (animateView.background is ColorDrawable && targetView.background is ColorDrawable) {
            val endColor = (animateView.background as ColorDrawable).color
            return endColor
        } else {
            return null
        }
    }
}

class BackgroundColorValueApplier : AnimationValueApplier<View, Int> {
    private var init: Boolean = false
    override fun applyValue(view: View, value: Int) {
        if (!this.init) {
            val endColorDrawable = view.background.mutate() as ColorDrawable
            view.setBackgroundDrawable(endColorDrawable)
            this.init = true
        }
        (view.background as ColorDrawable).color = value
    }
}

class BackgroundRecolor2(timeInterpolator: TimeInterpolator?) : TypedSceneTransition<View, Int>(
    BackgroundColorValueCaptor(), IntEvaluator(), BackgroundColorValueApplier(), timeInterpolator
)