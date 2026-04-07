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
package com.bytedance.scene.animation.interaction.scenetransition.utils;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

/**
 * Created by JiangQi on 11/12/25.
 */
@TargetApi(29)
class SceneViewCompatUtilsApi29 extends SceneViewCompatUtilsApi22 {
    @Override
    public void suppressLayout(@NonNull ViewGroup group, boolean suppress) {
        group.suppressLayout(suppress);
    }

    @Override
    public void setAnimationMatrix(@NonNull View view, Matrix matrix) {
        view.setAnimationMatrix(matrix);
    }

    @Override
    public void transformMatrixToGlobal(@NonNull View view, @NonNull Matrix matrix) {
        view.transformMatrixToGlobal(matrix);
    }

    @Override
    public void transformMatrixToLocal(@NonNull View view, @NonNull Matrix matrix) {
        view.transformMatrixToLocal(matrix);
    }

    @Override
    public void animateTransform(ImageView view, Matrix matrix) {
        view.animateTransform(matrix);
    }

    @Override
    public void setLeftTopRightBottom(View v, int left, int top, int right, int bottom) {
        v.setLeftTopRightBottom(left, top, right, bottom);
    }

    @Override
    public void setTransitionAlpha(@NonNull View view, float alpha) {
        view.setTransitionAlpha(alpha);
    }

    @Override
    public float getTransitionAlpha(@NonNull View view) {
        return view.getTransitionAlpha();
    }
}
