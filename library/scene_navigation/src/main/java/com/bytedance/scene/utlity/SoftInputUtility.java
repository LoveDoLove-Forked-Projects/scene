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
package com.bytedance.scene.utlity;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;

import com.bytedance.scene.SceneGlobalConfig;

/**
 * Created by JiangQi on 8/19/18.
 */
public class SoftInputUtility {
    public static boolean hideSoftInputFromWindow(View view) {
        if (view == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SceneGlobalConfig.forceUseWindowInsetsToDetectIMEStatus) {
            View rootView = view.getRootView();
            if (rootView != null) {
                WindowInsets windowInsets = rootView.getRootWindowInsets();
                if (windowInsets != null) {
                    boolean isImeVisible = windowInsets.isVisible(WindowInsets.Type.ime());
                    if (!isImeVisible) {
                        return false;
                    }
                }
            }
        }
        return ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean hideSoftInputFromWindow(Window window) {
        return hideSoftInputFromWindow(window != null ? window.getDecorView() : null);
    }
}
