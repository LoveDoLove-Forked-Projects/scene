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

import android.animation.TimeInterpolator
import android.animation.TypeEvaluator
import android.view.View
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation
import com.bytedance.scene.animation.interaction.scenetransition.SceneTransition

interface AnimationValueCaptor<VIEW_TYPE, ANIMATION_VALUE_TYPE> where VIEW_TYPE : View {
    abstract fun capture(
        animateView: VIEW_TYPE, targetView: VIEW_TYPE
    ): ANIMATION_VALUE_TYPE?
}

interface AnimationValueApplier<VIEW_TYPE, ANIMATION_VALUE_TYPE> where VIEW_TYPE : View {
    abstract fun applyValue(view: VIEW_TYPE, value: ANIMATION_VALUE_TYPE)

    open fun onClear(view: VIEW_TYPE) {}
}

typealias AnimationValueCalculator<ANIMATION_VALUE_TYPE> = TypeEvaluator<ANIMATION_VALUE_TYPE>
typealias AnimationValueInterpolator = TimeInterpolator

open class TypedSceneTransition<VIEW_TYPE, ANIMATION_VALUE_TYPE>(
    private val valueCaptor: AnimationValueCaptor<VIEW_TYPE, ANIMATION_VALUE_TYPE>,
    private val valueEvaluator: AnimationValueCalculator<ANIMATION_VALUE_TYPE>,
    private val valueApplier: AnimationValueApplier<VIEW_TYPE, ANIMATION_VALUE_TYPE>,
    private val valueInterpolator: AnimationValueInterpolator? = null
) : SceneTransition() where VIEW_TYPE : View {

    private var capturedValues: Pair<ANIMATION_VALUE_TYPE, ANIMATION_VALUE_TYPE>? = null

    override fun captureValue(
        fromView: View, toView: View, animationView: View
    ) {
        super.captureValue(fromView, toView, animationView)
        val fromValue = this.valueCaptor.capture(animationView as VIEW_TYPE, fromView as VIEW_TYPE)
        val toValue = this.valueCaptor.capture(animationView as VIEW_TYPE, toView as VIEW_TYPE)
        if (fromValue != null && toValue != null) {
            this.capturedValues = Pair(fromValue, toValue)
        } else {
            this.capturedValues = null
        }
    }

    override fun getAnimation(push: Boolean): InteractionAnimation? {
        val localValue = this.capturedValues
        if (localValue == null) {
            return InteractionAnimation.EMPTY
        }

        val startValue = localValue.first
        val endValue = localValue.second

        return object : InteractionAnimation(1.0f) {
            override fun onProgress(progress: Float) {
                onApplyProgress(valueInterpolator, startValue, endValue, progress)
            }
        }
    }

    private fun onApplyProgress(
        timeInterpolator: TimeInterpolator?,
        startValue: ANIMATION_VALUE_TYPE,
        endValue: ANIMATION_VALUE_TYPE,
        progress: Float
    ) {
        val currentProgress = timeInterpolator?.getInterpolation(progress) ?: progress
        val currentValue = valueEvaluator.evaluate(
            currentProgress, startValue, endValue
        )
        valueApplier.applyValue(mAnimationView as VIEW_TYPE, currentValue)
    }

    override fun finish(push: Boolean) {
        val localValue = this.capturedValues
        if (localValue == null) {
            return
        }

        val startValue = localValue.first
        val endValue = localValue.second
        this.onApplyProgress(null, startValue, endValue, 1.0f)
        this.valueApplier.onClear(mAnimationView as VIEW_TYPE)
    }
}