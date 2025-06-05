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
package com.bytedance.scene.navigation;

import android.app.Activity;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;

/**
 * Created by JiangQi on 7/30/18.
 */

class ActivityStatusRecord implements Parcelable {
    private int mStatusBarColor;
    private int mNavigationBarColor;
    private int mSystemUiVisibility;
    private int mSoftInputMode;
    private int mWindowFlags;
    private int mRequestedOrientation;
    private int mSystemBarsAppearance;

    private ActivityStatusRecord() {

    }

    protected ActivityStatusRecord(Parcel in) {
        mStatusBarColor = in.readInt();
        mNavigationBarColor = in.readInt();
        mSystemUiVisibility = in.readInt();
        mSoftInputMode = in.readInt();
        mWindowFlags = in.readInt();
        mRequestedOrientation = in.readInt();
        mSystemBarsAppearance = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mStatusBarColor);
        dest.writeInt(mNavigationBarColor);
        dest.writeInt(mSystemUiVisibility);
        dest.writeInt(mSoftInputMode);
        dest.writeInt(mWindowFlags);
        dest.writeInt(mRequestedOrientation);
        dest.writeInt(mSystemBarsAppearance);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ActivityStatusRecord> CREATOR = new Parcelable.Creator<ActivityStatusRecord>() {
        @Override
        public ActivityStatusRecord createFromParcel(Parcel in) {
            return new ActivityStatusRecord(in);
        }

        @Override
        public ActivityStatusRecord[] newArray(int size) {
            return new ActivityStatusRecord[size];
        }
    };

    public static ActivityStatusRecord newInstance(Activity activity) {
        ActivityStatusRecord record = new ActivityStatusRecord();
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            record.mStatusBarColor = window.getStatusBarColor();
            record.mNavigationBarColor = window.getNavigationBarColor();
        }
        record.mSystemUiVisibility = decorView.getSystemUiVisibility();
        record.mSoftInputMode = window.getAttributes().softInputMode;
        record.mWindowFlags = window.getAttributes().flags;
        record.mRequestedOrientation = activity.getRequestedOrientation();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController windowInsetsController = window.getInsetsController();
            if (windowInsetsController != null) {
                record.mSystemBarsAppearance = windowInsetsController.getSystemBarsAppearance();
            } else {
                record.mSystemBarsAppearance = -1;
            }
        }
        return record;
    }

    public void restore(Activity activity) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(mStatusBarColor);
            window.setNavigationBarColor(mNavigationBarColor);
        }
        window.getDecorView().setSystemUiVisibility(mSystemUiVisibility);
        window.setSoftInputMode(mSoftInputMode);
        int currentFlags = window.getAttributes().flags;
        int previousFlags = mWindowFlags;

        int commonFlags = currentFlags & previousFlags;
        int clearFlags = previousFlags & ~commonFlags;
        window.addFlags(clearFlags);

        int addFlags = currentFlags & ~commonFlags;
        window.clearFlags(addFlags);

        activity.setRequestedOrientation(mRequestedOrientation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int restoredSystemBarsAppearance = mSystemBarsAppearance;
            if (restoredSystemBarsAppearance != -1) {
                WindowInsetsController windowInsetsController = window.getInsetsController();
                if (windowInsetsController != null) {
                    int currentSystemBarsAppearance = windowInsetsController.getSystemBarsAppearance();
                    if (currentSystemBarsAppearance != restoredSystemBarsAppearance) {
                        if ((currentSystemBarsAppearance & WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS) != (restoredSystemBarsAppearance & WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)) {
                            if ((restoredSystemBarsAppearance & WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS) != 0) {
                                windowInsetsController.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                            } else {
                                windowInsetsController.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                            }
                        }
                        if ((currentSystemBarsAppearance & WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS) != (restoredSystemBarsAppearance & WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)) {
                            if ((restoredSystemBarsAppearance & WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS) != 0) {
                                windowInsetsController.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
                            } else {
                                windowInsetsController.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
                            }
                        }
                    }
                }
            }
        }
    }
}
