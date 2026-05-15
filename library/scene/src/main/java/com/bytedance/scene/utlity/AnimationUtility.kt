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
package com.bytedance.scene.utlity

import android.provider.Settings
import android.view.View
import androidx.annotation.StringDef
import kotlin.math.max

/**
 * Created by jiangqi on 2024/9/27
 * @author jiangqi@bytedance.com
 */
/**
 * if system > Build.VERSION_CODES.TIRAMISU, can use ValueAnimator.getDurationScale() instead
 */
internal fun getDurationScale(view: View): Float {
    return getDurationScale(view, Settings.Global.ANIMATOR_DURATION_SCALE)
}

@StringDef(
    value = [Settings.Global.ANIMATOR_DURATION_SCALE, Settings.Global.TRANSITION_ANIMATION_SCALE, Settings.Global.WINDOW_ANIMATION_SCALE]
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class DurationScaleType

internal fun getDurationScale(view: View, @DurationScaleType durationScaleType: String): Float {
    val durationScale = Settings.Global.getFloat(
        view.context.contentResolver, durationScaleType, 1.0f
    )
    return max(0.0f, durationScale)
}