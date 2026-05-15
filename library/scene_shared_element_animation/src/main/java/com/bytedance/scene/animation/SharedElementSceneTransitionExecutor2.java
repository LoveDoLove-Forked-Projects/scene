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
package com.bytedance.scene.animation;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.animatorexecutor.Android8DefaultSceneAnimatorExecutor;
import com.bytedance.scene.animation.interaction.scenetransition.SceneTransition;
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.SceneVisibilityTransition;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.CancellationSignalList;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by JiangQi on 9/2/18.
 */
public class SharedElementSceneTransitionExecutor2 extends NavigationAnimationExecutor {
    public static enum TranslucentPolicy {
        THROW_ERROR, USE_FALLBACK, CONTINUE
    }

    private static final String TAG = "SharedElementSceneTransitionExecutor";
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private final Map<String, SceneTransition> mSharedElementTransition;
    @Nullable
    private final SceneVisibilityTransition mOtherTransition;
    @NonNull
    private SharedElementNotFoundPolicy mSharedElementNotFoundPolicy;
    @NonNull
    private final NavigationAnimationExecutor mFallbackAnimationExecutor;
    private boolean mDelayEnterTransitionExecute = false;
    private Runnable mEnterTransitionRunnable = null;
    protected int mAnimationDuration = -1;
    private final TranslucentPolicy mTranslucentPolicy;

    public SharedElementSceneTransitionExecutor2(@NonNull Map<String, SceneTransition> sharedElementTransition,
                                                 @Nullable SceneVisibilityTransition otherTransition,
                                                 @NonNull NavigationAnimationExecutor fallbackAnimationExecutor,
                                                 @NonNull SharedElementNotFoundPolicy sharedElementNotFoundPolicy,
                                                 TranslucentPolicy translucentPolicy) {
        this.mSharedElementTransition = sharedElementTransition;
        this.mOtherTransition = otherTransition;
        this.mSharedElementNotFoundPolicy = sharedElementNotFoundPolicy;
        this.mFallbackAnimationExecutor = fallbackAnimationExecutor;
        this.mTranslucentPolicy = translucentPolicy;
    }

    public SharedElementSceneTransitionExecutor2(@NonNull Map<String, SceneTransition> sharedElementTransition,
                                                 @Nullable SceneVisibilityTransition otherTransition,
                                                 @NonNull NavigationAnimationExecutor fallbackAnimationExecutor,
                                                 @NonNull SharedElementNotFoundPolicy sharedElementNotFoundPolicy) {
        this(sharedElementTransition, otherTransition, fallbackAnimationExecutor, sharedElementNotFoundPolicy, TranslucentPolicy.THROW_ERROR);
    }

    public SharedElementSceneTransitionExecutor2(@NonNull Map<String, SceneTransition> sharedElementTransition,
                                                 @Nullable SceneVisibilityTransition otherTransition,
                                                 @NonNull NavigationAnimationExecutor fallbackAnimationExecutor) {
        this(sharedElementTransition, otherTransition, fallbackAnimationExecutor, SharedElementNotFoundPolicy.FALLBACK);
    }

    public SharedElementSceneTransitionExecutor2(Map<String, SceneTransition> sharedElementTransition, SceneVisibilityTransition otherTransition) {
        this(sharedElementTransition, otherTransition, new Android8DefaultSceneAnimatorExecutor());
    }

    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    public void setSharedElementNotFoundPolicy(@NonNull SharedElementNotFoundPolicy sharedElementNotFoundPolicy) {
        this.mSharedElementNotFoundPolicy = sharedElementNotFoundPolicy;
    }

    public void postponeEnterTransition() {
        this.postponeEnterTransition(TimeUnit.SECONDS.toMillis(5));
    }

    public void postponeEnterTransition(long timeOutMillis) {
        if (this.mDelayEnterTransitionExecute) {
            return;
        }
        LoggerManager.getInstance().i(TAG, "invoke postponeEnterTransition to delay animation");
        this.mDelayEnterTransitionExecute = true;
        sHandler.postDelayed(this.TIME_OUT_CALLBACK, timeOutMillis);
    }

    public void startPostponedEnterTransition() {
        this.startPostponedEnterTransitionInternal(true);
    }

    private void startPostponedEnterTransitionInternal(boolean removeTimeOutCallback) {
        LoggerManager.getInstance().i(TAG, "invoke startPostponedEnterTransition to start pending animation");
        if (this.mEnterTransitionRunnable != null) {
            this.mEnterTransitionRunnable.run();
            this.mEnterTransitionRunnable = null;
        }
        this.mDelayEnterTransitionExecute = false;
        if (removeTimeOutCallback) {
            sHandler.removeCallbacks(this.TIME_OUT_CALLBACK);
        }
    }

    public void setAnimationDuration(int animationDuration) {
        this.mAnimationDuration = animationDuration;
    }

    private final Runnable TIME_OUT_CALLBACK = () -> {
        if (mEnterTransitionRunnable != null) {
            LoggerManager.getInstance().i(TAG, "postponeEnterTransition reach time out");
            startPostponedEnterTransitionInternal(false);
        }
    };

    @Override
    public final void executePushChangeCancelable(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull final CancellationSignal cancellationSignal) {
        boolean useFallbackAnimation = false;
        if (fromInfo.mIsTranslucent) {
            String exceptionMessage = "SharedElement push animation don't support translucent source scene: " + fromInfo.mSceneClass + ", destination scene: " + toInfo.mSceneClass;
            switch (this.mTranslucentPolicy) {
                case CONTINUE:
                    break;
                case THROW_ERROR:
                    throw new IllegalArgumentException(exceptionMessage);
                case USE_FALLBACK:
                    LoggerManager.getInstance().e(TAG, exceptionMessage);
                    useFallbackAnimation = true;
                    break;
            }
        }

        if (toInfo.mIsTranslucent) {
            String exceptionMessage = "SharedElement push animation don't support translucent destination scene: " + toInfo.mSceneClass + ", source scene: " + fromInfo.mSceneClass;
            switch (this.mTranslucentPolicy) {
                case CONTINUE:
                    break;
                case THROW_ERROR:
                    throw new IllegalArgumentException(exceptionMessage);
                case USE_FALLBACK:
                    LoggerManager.getInstance().e(TAG, exceptionMessage);
                    useFallbackAnimation = true;
                    break;
            }
        }

        if (useFallbackAnimation || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            this.mFallbackAnimationExecutor.setAnimationViewGroup(mAnimationViewGroup);
            this.mFallbackAnimationExecutor.executePushChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            return;
        }

        executePushChangeV21(fromInfo, toInfo, endAction, cancellationSignal);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void executePushChangeV21(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo
            toInfo, @NonNull final Runnable endAction, @NonNull final CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        fromView.setVisibility(View.VISIBLE);
        final View toView = toInfo.mSceneView;

        final SharedElementViewTransitionExecutor sharedElementViewTransitionExecutor = new SharedElementViewTransitionExecutor(mSharedElementTransition, mOtherTransition);
        if (mAnimationDuration >= 0) {
            sharedElementViewTransitionExecutor.setAnimationDuration(mAnimationDuration);
        }
        sharedElementViewTransitionExecutor.setSuppressLayoutSceneView(false);
        final Runnable fallbackAction = new Runnable() {
            @Override
            public void run() {
                mFallbackAnimationExecutor.setAnimationViewGroup(mAnimationViewGroup);
                mFallbackAnimationExecutor.executePushChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            }
        };

        if (this.mDelayEnterTransitionExecute) {
            toView.setVisibility(View.INVISIBLE);
            final CancellationSignalList cancellationSignalListAdapter = new CancellationSignalList();
            this.mEnterTransitionRunnable = new Runnable() {
                @Override
                public void run() {
                    toView.setVisibility(View.VISIBLE);
                    sharedElementViewTransitionExecutor.executePushChange(fromView, toView, new Runnable() {
                        @Override
                        public void run() {
                            if (!toInfo.mIsTranslucent) {
                                fromView.setVisibility(View.GONE);
                            }
                            endAction.run();
                        }
                    }, cancellationSignalListAdapter.getChildCancellationSignal(), mSharedElementNotFoundPolicy, fallbackAction);
                }
            };
            cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                @Override
                public void onCancel() {
                    mEnterTransitionRunnable = null;
                    toView.setVisibility(View.VISIBLE);
                    if (!toInfo.mIsTranslucent) {
                        fromView.setVisibility(View.GONE);
                    }
                    cancellationSignalListAdapter.cancel();
                }
            });
        } else {
            sharedElementViewTransitionExecutor.executePushChange(fromView, toView, new Runnable() {
                @Override
                public void run() {
                    if (!toInfo.mIsTranslucent) {
                        fromView.setVisibility(View.GONE);
                    }
                    endAction.run();
                }
            }, cancellationSignal, mSharedElementNotFoundPolicy, fallbackAction);
        }
    }

    @Override
    public final void executePopChangeCancelable(@NonNull AnimationInfo
                                                         fromInfo, @NonNull AnimationInfo toInfo, @NonNull final Runnable endAction,
                                                 @NonNull CancellationSignal cancellationSignal) {
        boolean useFallbackAnimation = false;
        if (fromInfo.mIsTranslucent) {
            String exceptionMessage = "SharedElement pop animation don't support translucent source scene: " + fromInfo.mSceneClass + ", destination scene: " + toInfo.mSceneClass;
            switch (this.mTranslucentPolicy) {
                case CONTINUE:
                    break;
                case THROW_ERROR:
                    throw new IllegalArgumentException(exceptionMessage);
                case USE_FALLBACK:
                    LoggerManager.getInstance().e(TAG, exceptionMessage);
                    useFallbackAnimation = true;
                    break;
            }
        }

        if (toInfo.mIsTranslucent) {
            String exceptionMessage = "SharedElement pop animation don't support translucent destination scene: " + toInfo.mSceneClass + ", source scene: " + fromInfo.mSceneClass;
            switch (this.mTranslucentPolicy) {
                case CONTINUE:
                    break;
                case THROW_ERROR:
                    throw new IllegalArgumentException(exceptionMessage);
                case USE_FALLBACK:
                    LoggerManager.getInstance().e(TAG, exceptionMessage);
                    useFallbackAnimation = true;
                    break;
            }
        }

        if (useFallbackAnimation || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            this.mFallbackAnimationExecutor.setAnimationViewGroup(mAnimationViewGroup);
            this.mFallbackAnimationExecutor.executePopChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            return;
        }

        executePopChangeV21(fromInfo, toInfo, endAction, cancellationSignal);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void executePopChangeV21(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo
            toInfo, @NonNull final Runnable endAction, @NonNull final CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;

        if (!mDisableRemoveView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mAnimationViewGroup.getOverlay().add(fromView);
            } else {
                mAnimationViewGroup.addView(fromView);
            }
        }

        fromView.setVisibility(View.VISIBLE);

        final Runnable fallbackAction = new Runnable() {
            @Override
            public void run() {
                mFallbackAnimationExecutor.setAnimationViewGroup(mAnimationViewGroup);
                mFallbackAnimationExecutor.executePopChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            }
        };

        final SharedElementViewTransitionExecutor executor = new SharedElementViewTransitionExecutor(mSharedElementTransition, mOtherTransition);
        if (mAnimationDuration >= 0) {
            executor.setAnimationDuration(mAnimationDuration);
        }
        executor.setSuppressLayoutSceneView(false);
        executor.executePopChange(fromView, toView, new Runnable() {
            @Override
            public void run() {
                fromView.setVisibility(View.GONE);

                if (!mDisableRemoveView) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        mAnimationViewGroup.getOverlay().remove(fromView);
                    } else {
                        mAnimationViewGroup.removeView(fromView);
                    }
                }

                endAction.run();
            }
        }, cancellationSignal, mSharedElementNotFoundPolicy, fallbackAction);
    }
}
