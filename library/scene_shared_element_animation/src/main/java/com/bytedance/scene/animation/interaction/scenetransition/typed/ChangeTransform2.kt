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
import android.graphics.Matrix
import android.graphics.RectF
import android.view.View
import com.bytedance.scene.animation.interaction.evaluator.MatrixEvaluator
import com.bytedance.scene.animation.interaction.scenetransition.utils.SceneViewCompatUtils

class ChangeTransformValueCaptor(private val scaleToFit: Matrix.ScaleToFit = Matrix.ScaleToFit.FILL) :
    AnimationValueCaptor<View, Matrix> {
    override fun capture(
        animateView: View, targetView: View
    ): Matrix? {
        if (animateView == targetView) {
            return Matrix()
        }

        val animateViewRect = getRectByView(animateView)
        val simulateTargetViewRect = getRectByView(targetView)

        val matrix = Matrix()
        matrix.setRectToRect(animateViewRect, simulateTargetViewRect, scaleToFit)

        return matrix
    }

    private fun getRectByView(view: View): RectF {
        val loc = IntArray(2)
        view.getLocationOnScreen(loc)
        val animateViewRect = RectF(
            loc[0].toFloat(),
            loc[1].toFloat(),
            loc[0].toFloat() + view.width,
            loc[1].toFloat() + view.height
        )
        return animateViewRect
    }
}

class ChangeTransformValueApplier : AnimationValueApplier<View, Matrix> {
    override fun applyValue(view: View, value: Matrix) {
        SceneViewCompatUtils.setAnimationMatrix(view, value)
    }

    override fun onClear(view: View) {
        super.onClear(view)
        SceneViewCompatUtils.setAnimationMatrix(view, null)
    }
}

class ChangeTransform2(timeInterpolator: TimeInterpolator?) : TypedSceneTransition<View, Matrix>(
    ChangeTransformValueCaptor(), MatrixEvaluator(), ChangeTransformValueApplier(), timeInterpolator
)