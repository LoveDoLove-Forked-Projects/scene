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

import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.bytedance.scene.animation.AnimationInfo
import com.bytedance.scene.animation.NavigationAnimationExecutor
import com.bytedance.scene.animation.NavigationAnimationListener
import com.bytedance.scene.logger.LoggerManager
import com.bytedance.scene.utlity.CancellationSignal

/**
 * Mutable animation executor which can modifies executor before start an animation.
 *
 * Created by junyu on 2025/5/8
 * @author yuejunyu.0@tiktok.com
 */
class MutableAnimationExecutor(
    // note: setter functions e.g. setAnimationViewGroup
    // will not be called again when the delegated being replaced.
    private var _delegated: NavigationAnimationExecutor,
) : NavigationAnimationExecutor() {
    private val TAG = "MutableAnimationExecutor"

    var delegated: NavigationAnimationExecutor
        get() {
            return this._delegated
        }
        set(value) {
            LoggerManager.getInstance()
                .i(TAG, "change delegated NavigationAnimationExecutor $_delegated -> $value")
            //todo reset previous NavigationAnimationExecutor?
            //make sure the latest NavigationAnimationExecutor properties is correct
            value.setDisableRemoveView(this.mDisableRemoveView)
            value.setAnimationEndAction(this.mCustomAnimationEndAction)
            value.setAnimationViewGroup(this.mAnimationViewGroup)
            value.replaceAnimationListenerList(this.mAnimationListenerList)
            this._delegated = value
        }

    override fun setDisableRemoveView(disableRemoveView: Boolean) {
        super.setDisableRemoveView(disableRemoveView)
        LoggerManager.getInstance().i(TAG, "setDisableRemoveView $_delegated")
        _delegated.setDisableRemoveView(disableRemoveView)
    }

    @Deprecated("use addAnimationEndAction/removeAnimationEndAction instead")
    override fun setAnimationEndAction(endAction: Runnable?) {
        super.setAnimationEndAction(endAction)
        LoggerManager.getInstance().i(TAG, "setAnimationEndAction $_delegated")
        _delegated.setAnimationEndAction(endAction)
    }

    override fun setAnimationViewGroup(viewGroup: ViewGroup) {
        super.setAnimationViewGroup(viewGroup)
        LoggerManager.getInstance().i(TAG, "setAnimationViewGroup $_delegated")
        _delegated.setAnimationViewGroup(viewGroup)
    }

    override fun addAnimationListener(listener: NavigationAnimationListener) {
        super.addAnimationListener(listener)
        _delegated.addAnimationListener(listener)
    }

    override fun removeAnimationListener(listener: NavigationAnimationListener) {
        super.removeAnimationListener(listener)
        _delegated.removeAnimationListener(listener)
    }

    override fun replaceAnimationListenerList(listenerList: List<NavigationAnimationListener?>?) {
        super.replaceAnimationListenerList(listenerList)
        _delegated.replaceAnimationListenerList(listenerList)
    }

    override fun executePopChangeCancelable(
        fromInfo: AnimationInfo,
        toInfo: AnimationInfo,
        endAction: Runnable,
        cancellationSignal: CancellationSignal
    ) {
        LoggerManager.getInstance().i(TAG, "executePopChangeCancelable $_delegated")
        _delegated.executePopChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal)
    }

    override fun executePushChangeCancelable(
        fromInfo: AnimationInfo,
        toInfo: AnimationInfo,
        endAction: Runnable,
        cancellationSignal: CancellationSignal
    ) {
        LoggerManager.getInstance().i(TAG, "executePushChangeCancelable $_delegated")
        _delegated.executePushChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal)
    }

    override fun isSupport(from: Class<out Scene?>, to: Class<out Scene?>): Boolean {
        return _delegated.isSupport(from, to)
    }
}