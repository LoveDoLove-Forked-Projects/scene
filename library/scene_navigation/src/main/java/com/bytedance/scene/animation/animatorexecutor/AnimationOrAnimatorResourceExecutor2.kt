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
package com.bytedance.scene.animation.animatorexecutor

import android.app.Activity
import android.provider.Settings
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.core.view.ViewCompat
import com.bytedance.scene.Scene
import com.bytedance.scene.State
import com.bytedance.scene.animation.AnimationInfo
import com.bytedance.scene.animation.AnimationOrAnimator
import com.bytedance.scene.animation.NavigationAnimationExecutor
import com.bytedance.scene.utlity.AnimatorUtility
import com.bytedance.scene.utlity.CancellationSignal
import com.bytedance.scene.utlity.syncMaxDuration

/**
 * Created by JiangQi on 10/27/25.
 *
 *
 * A -> B
 * A Exit, B Enter
 *
 *
 * B -> A
 * B Return, A Reenter
 *
 * animation duration will be affected by device Settings.Global.TRANSITION_ANIMATION_SCALE
 */
class AnimationOrAnimatorResourceExecutor2 : NavigationAnimationExecutor {
    private var enterAnimator: AnimationOrAnimator? = null
    private var exitAnimator: AnimationOrAnimator? = null
    private var returnAnimator: AnimationOrAnimator? = null
    private var reenterAnimator: AnimationOrAnimator? = null

    constructor(
        activity: Activity,
        @AnimatorRes @AnimRes enterResId: Int,
        @AnimatorRes @AnimRes exitResId: Int,
        syncAnimationsDuration: Boolean
    ) {
        if (enterResId != 0) {
            this.enterAnimator = AnimationOrAnimator.loadAnimation(activity, enterResId)
            this.returnAnimator = AnimationOrAnimator.loadAnimation(activity, enterResId).also {
                it.reverse()
            }
        }
        if (exitResId != 0) {
            this.exitAnimator = AnimationOrAnimator.loadAnimation(activity, exitResId)
            this.reenterAnimator = AnimationOrAnimator.loadAnimation(activity, exitResId).also {
                it.reverse()
            }
        }

        if (syncAnimationsDuration) {
            this.syncAnimationDurationToMaxDuration()
        }
    }

    constructor(
        activity: Activity,
        @AnimatorRes @AnimRes enterResId: Int,
        @AnimatorRes @AnimRes exitResId: Int,
        @AnimatorRes @AnimRes returnResId: Int,
        @AnimatorRes @AnimRes reenterResId: Int,
        syncAnimationsDuration: Boolean
    ) {
        if (enterResId != 0) {
            this.enterAnimator = AnimationOrAnimator.loadAnimation(activity, enterResId)
        }
        if (exitResId != 0) {
            this.exitAnimator = AnimationOrAnimator.loadAnimation(activity, exitResId)
        }
        if (returnResId != 0) {
            this.returnAnimator = AnimationOrAnimator.loadAnimation(activity, returnResId)
        }
        if (reenterResId != 0) {
            this.reenterAnimator = AnimationOrAnimator.loadAnimation(activity, reenterResId)
        }

        if (syncAnimationsDuration) {
            this.syncAnimationDurationToMaxDuration()
        }
    }

    private fun syncAnimationDurationToMaxDuration() {
        if (this.enterAnimator != null && this.exitAnimator != null) {
            syncMaxDuration(enterAnimator, exitAnimator)
        }
        if (this.returnAnimator != null && this.reenterAnimator != null) {
            syncMaxDuration(returnAnimator, reenterAnimator)
        }
    }

    override fun isSupport(from: Class<out Scene?>, to: Class<out Scene?>): Boolean {
        return true
    }

    override fun executePushChangeCancelable(
        fromInfo: AnimationInfo,
        toInfo: AnimationInfo,
        endAction: Runnable,
        cancellationSignal: CancellationSignal
    ) {
        if (enterAnimator == null && exitAnimator == null) {
            endAction.run()
            return
        }

        // Cannot be placed in onAnimationStart, as there it a post interval, it will splash
        val fromView = fromInfo.mSceneView
        val toView = toInfo.mSceneView

        AnimatorUtility.resetViewStatus(fromView)
        AnimatorUtility.resetViewStatus(toView)
        fromView.visibility = View.VISIBLE

        val fromViewElevation = ViewCompat.getElevation(fromView)
        if (fromViewElevation > 0) {
            ViewCompat.setElevation(fromView, 0f)
        }

        // In the case of pushAndClear, it is possible that the Scene come from has been destroyed.
        if (!mDisableRemoveView) {
            if (fromInfo.mSceneState.value < State.VIEW_CREATED.value) {
                mAnimationViewGroup.getOverlay().add(fromView)
            }
        }

        val animationEndAction: Runnable = CountRunnable(2) {
            if (!toInfo.mIsTranslucent) {
                fromView.visibility = View.GONE
            }

            if (fromViewElevation > 0) {
                ViewCompat.setElevation(fromView, fromViewElevation)
            }

            AnimatorUtility.resetViewStatus(fromView)
            AnimatorUtility.resetViewStatus(toView)

            if (!mDisableRemoveView) {
                if (fromInfo.mSceneState.value < State.VIEW_CREATED.value) {
                    mAnimationViewGroup.getOverlay().remove(fromView)
                }
            }
            endAction.run()
        }

        if (enterAnimator != null) {
            enterAnimator!!.addEndAction(animationEndAction)
            enterAnimator!!.applySystemDurationScale(
                toView, Settings.Global.TRANSITION_ANIMATION_SCALE
            )
            enterAnimator!!.start(toView)
        } else {
            animationEndAction.run()
        }
        if (exitAnimator != null) {
            exitAnimator?.addEndAction(animationEndAction)
            exitAnimator?.applySystemDurationScale(
                fromView, Settings.Global.TRANSITION_ANIMATION_SCALE
            )
            exitAnimator?.start(fromView)
        } else {
            animationEndAction.run()
        }

        cancellationSignal.setOnCancelListener {
            enterAnimator?.end()
            exitAnimator?.end()
        }
    }

    override fun executePopChangeCancelable(
        fromInfo: AnimationInfo,
        toInfo: AnimationInfo,
        endAction: Runnable,
        cancellationSignal: CancellationSignal
    ) {
        if (returnAnimator == null && reenterAnimator == null) {
            endAction.run()
            return
        }

        val fromView = fromInfo.mSceneView
        val toView = toInfo.mSceneView

        AnimatorUtility.resetViewStatus(fromView)
        AnimatorUtility.resetViewStatus(toView)

        fromView.visibility = View.VISIBLE

        if (!mDisableRemoveView) {
            mAnimationViewGroup.getOverlay().add(fromView)
        }

        val animationEndAction: Runnable = CountRunnable(2) { // Todo: children also has to reset
            AnimatorUtility.resetViewStatus(fromView)
            AnimatorUtility.resetViewStatus(toView)

            if (!mDisableRemoveView) {
                mAnimationViewGroup.getOverlay().remove(fromView)
            }
            endAction.run()
        }

        if (returnAnimator != null) {
            returnAnimator?.addEndAction(animationEndAction)
            returnAnimator?.applySystemDurationScale(
                fromView, Settings.Global.TRANSITION_ANIMATION_SCALE
            )
            returnAnimator?.start(fromView)
        } else {
            animationEndAction.run()
        }

        if (reenterAnimator != null) {
            reenterAnimator?.addEndAction(animationEndAction)
            reenterAnimator?.applySystemDurationScale(
                toView, Settings.Global.TRANSITION_ANIMATION_SCALE
            )
            reenterAnimator?.start(toView)
        } else {
            animationEndAction.run()
        }
        cancellationSignal.setOnCancelListener {
            returnAnimator?.end()
            reenterAnimator?.end()
        }
    }

    private class CountRunnable(var count: Int, var runnable: Runnable) : Runnable {
        override fun run() {
            count--
            if (count == 0) {
                runnable.run()
            }
        }
    }
}
