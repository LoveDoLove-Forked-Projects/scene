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
package com.bytedance.scene

//Describes the reason why a Scene's state is being saved.
object SceneStateSaveReason {
    const val KEY_SCENE_SAVE_STATE_REASON = "bd-scene:scene_save_instance_state_reason";

    const val UNKNOWN = 0

    /**
     * The state is saved because the parent container (e.g., an Activity or Fragment)
     * is going through its own state-saving process.
     */
    const val PARENT_SAVED = 1

    /**
     * The state is saved as part of a process to recycle invisible Scenes
     * to optimize memory and performance.
     */
    const val RECYCLING = 2

    /**
     * The state is saved because the Scene's own configuration has changed,
     * such as a theme or orientation change.
     */
    const val CONFIGURATION_CHANGED = 3

    /**
     * The state is saved because NavigationScene.recreate() was explicitly called
     * by user code, typically to force a fresh instance of the Scene.
     */
    const val MANUAL_RECREATE = 4

    /**
     * Returns a human-readable name for the given reason code.
     */
    fun getReasonName(reason: Int): String {
        return when (reason) {
            UNKNOWN -> "UNKNOWN"
            PARENT_SAVED -> "PARENT_SAVED"
            RECYCLING -> "RECYCLING"
            CONFIGURATION_CHANGED -> "CONFIGURATION_CHANGED"
            MANUAL_RECREATE -> "MANUAL_RECREATE"
            else -> "INVALID($reason)"
        }
    }

    /**
     * Checks if the reason is a user-initiated reason.
     */
    fun isUserInitiated(reason: Int): Boolean {
        return reason == MANUAL_RECREATE
    }

    /**
     * Checks if the reason is related to system events.
     */
    fun isSystemEvent(reason: Int): Boolean {
        return reason == PARENT_SAVED || reason == RECYCLING || reason == CONFIGURATION_CHANGED
    }
}

