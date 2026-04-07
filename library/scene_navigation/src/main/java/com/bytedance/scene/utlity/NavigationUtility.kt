package com.bytedance.scene.utlity

import com.bytedance.scene.animation.AnimationOrAnimator


internal fun syncMaxDuration(vararg animations: AnimationOrAnimator?): Unit {
    val nonNullArray = animations.filterNotNull()
    val maxDuration = nonNullArray.maxOfOrNull { it.duration }
    if (maxDuration == null) {
        return
    }
    nonNullArray.forEach {
        if (it.duration != maxDuration) {
            it.duration = maxDuration
        }
    }
}