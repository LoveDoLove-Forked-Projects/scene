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
package com.bytedance.scene;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.ThreadUtility;
import com.bytedance.scene.utlity.Utility;

/**
 * Created by JiangQi on 7/30/18.
 */
public final class NavigationSceneUtility {
    private static final String LIFE_CYCLE_FRAGMENT_TAG = GroupSceneUtility.LIFE_CYCLE_FRAGMENT_TAG;

    private NavigationSceneUtility() {
    }

    public static final class Builder {
        @NonNull
        private final Activity mActivity;
        @NonNull
        private final Class<? extends Scene> mRootSceneClazz;
        @Nullable
        private Bundle mRootSceneArguments;
        private boolean mDrawWindowBackground = true;
        private boolean mFixSceneBackgroundEnabled = true;
        @DrawableRes
        private int mSceneBackgroundResId = 0;
        @IdRes
        private int mIdRes = android.R.id.content;
        private boolean mSupportRestore = false;
        @Nullable
        private SceneComponentFactory mRootSceneComponentFactory;
        @NonNull
        private String mTag = LIFE_CYCLE_FRAGMENT_TAG;
        private boolean mImmediate = true;
        private float mAutoRecycleInvisibleScenesThreshold = 0F;
        private boolean mOnlyRestoreVisibleScene = false;
        private boolean mSeparateCreateFromCreateView = false;

        private Builder(@NonNull Activity activity, @NonNull Class<? extends Scene> rootSceneClazz) {
            this.mActivity = Utility.requireNonNull(activity, "Activity can't be null");
            this.mRootSceneClazz = Utility.requireNonNull(rootSceneClazz, "Root Scene class can't be null");
        }

        @NonNull
        public Builder rootSceneArguments(@Nullable Bundle rootSceneArguments) {
            this.mRootSceneArguments = rootSceneArguments;
            return this;
        }

        @NonNull
        public Builder toView(@IdRes int idRes) {
            this.mIdRes = idRes;
            return this;
        }

        @NonNull
        public Builder supportRestore(boolean supportRestore) {
            this.mSupportRestore = supportRestore;
            return this;
        }

        @NonNull
        public Builder rootSceneComponentFactory(@Nullable SceneComponentFactory rootSceneComponentFactory) {
            this.mRootSceneComponentFactory = rootSceneComponentFactory;
            return this;
        }

        @NonNull
        public Builder rootScene(@NonNull final Scene scene) {
            if (scene.getClass() != this.mRootSceneClazz) {
                throw new IllegalArgumentException("Scene type error, must be " + this.mRootSceneClazz + " instance");
            }

            this.mRootSceneComponentFactory = new SceneComponentFactory() {
                @Nullable
                @Override
                public Scene instantiateScene(@NonNull ClassLoader cl, @NonNull String className, @Nullable Bundle bundle) {
                    return scene;
                }
            };
            return this;
        }

        @NonNull
        public Builder drawWindowBackground(boolean drawWindowBackground) {
            this.mDrawWindowBackground = drawWindowBackground;
            return this;
        }

        @NonNull
        public Builder fixSceneWindowBackgroundEnabled(boolean fixSceneBackground) {
            this.mFixSceneBackgroundEnabled = fixSceneBackground;
            return this;
        }

        @NonNull
        public Builder sceneBackground(@DrawableRes int resId) {
            this.mSceneBackgroundResId = resId;
            return this;
        }

        @NonNull
        public Builder tag(@NonNull String tag) {
            this.mTag = Utility.requireNonNull(tag, "Tag can't be null");
            return this;
        }

        @NonNull
        public Builder immediate(boolean immediate) {
            this.mImmediate = immediate;
            return this;
        }

        @NonNull
        public Builder autoRecycleInvisibleScenesThreshold(@FloatRange(from = 0.0, to = 1.0)float threshold) {
            this.mAutoRecycleInvisibleScenesThreshold = threshold;
            return this;
        }

        @NonNull
        public Builder onlyRestoreVisibleScene(boolean onlyRestoreVisibleScene) {
            this.mOnlyRestoreVisibleScene = onlyRestoreVisibleScene;
            return this;
        }

        @NonNull
        public Builder separateCreateFromCreateView(boolean separateCreateFromCreateView) {
            this.mSeparateCreateFromCreateView = separateCreateFromCreateView;
            return this;
        }

        @NonNull
        public SceneDelegate build() {
            NavigationSceneOptions navigationSceneOptions = new NavigationSceneOptions(this.mRootSceneClazz, this.mRootSceneArguments);
            navigationSceneOptions.setDrawWindowBackground(this.mDrawWindowBackground);
            navigationSceneOptions.setFixSceneWindowBackgroundEnabled(this.mFixSceneBackgroundEnabled);
            navigationSceneOptions.setSceneBackground(this.mSceneBackgroundResId);
            navigationSceneOptions.setAutoRecycleInvisibleScenesThreshold(this.mAutoRecycleInvisibleScenesThreshold);
            navigationSceneOptions.setOnlyRestoreVisibleScene(this.mOnlyRestoreVisibleScene);
            return setupWithActivity(this.mActivity, this.mIdRes, navigationSceneOptions, this.mRootSceneComponentFactory, this.mSupportRestore, this.mTag, this.mImmediate, this.mSeparateCreateFromCreateView);
        }
    }

    @NonNull
    public static Builder setupWithActivity(@NonNull final Activity activity,
                                            @NonNull Class<? extends Scene> rootScene) {
        return new Builder(activity, rootScene);
    }

    /**
     * use {@link #setupWithActivity(Activity, Class)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity, @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  boolean supportRestore,
                                                  final boolean separateCreateFromCreateView) {
        return setupWithActivity(activity, savedInstanceState,
                new NavigationSceneOptions(rootScene, null), null, supportRestore, separateCreateFromCreateView);
    }

    /**
     * use {@link #setupWithActivity(Activity, Class)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity, @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  boolean supportRestore,
                                                  final boolean separateCreateFromCreateView) {
        return setupWithActivity(activity, savedInstanceState,
                new NavigationSceneOptions(rootScene, null), rootSceneComponentFactory, supportRestore, separateCreateFromCreateView);
    }

    /**
     * use {@link #setupWithActivity(Activity, Class)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity, @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  boolean supportRestore,
                                                  final boolean separateCreateFromCreateView) {
        return setupWithActivity(activity, android.R.id.content, savedInstanceState, navigationSceneOptions, null, supportRestore, separateCreateFromCreateView);
    }

    /**
     * use {@link #setupWithActivity(Activity, Class)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity, @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  boolean supportRestore,
                                                  final boolean separateCreateFromCreateView) {
        return setupWithActivity(activity, android.R.id.content, savedInstanceState, navigationSceneOptions, rootSceneComponentFactory, supportRestore, separateCreateFromCreateView);
    }

    /**
     * use {@link #setupWithActivity(Activity, Class)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity,
                                                  @IdRes int idRes,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  final boolean supportRestore,
                                                  final boolean separateCreateFromCreateView) {
        return setupWithActivity(activity, idRes, savedInstanceState, navigationSceneOptions,
                rootSceneComponentFactory, supportRestore, LIFE_CYCLE_FRAGMENT_TAG, true, separateCreateFromCreateView);
    }

    /**
     * use {@link #setupWithActivity(Activity, Class)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity,
                                                  @IdRes int idRes,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  final boolean supportRestore,
                                                  @NonNull String tag,
                                                  boolean immediate,
                                                  final boolean separateCreateFromCreateView) {
        return setupWithActivity(activity, idRes, navigationSceneOptions, rootSceneComponentFactory, supportRestore, tag, immediate, separateCreateFromCreateView);
    }

    @NonNull
    private static SceneDelegate setupWithActivity(@NonNull final Activity activity,
                                                   @IdRes int idRes,
                                                   @NonNull NavigationSceneOptions navigationSceneOptions,
                                                   @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                   final boolean supportRestore,
                                                   @NonNull String tag,
                                                   boolean immediate,
                                                   final boolean separateCreateFromCreateView) {
        ThreadUtility.checkUIThread();
        if (tag == null) {
            throw new IllegalArgumentException("tag cant be null");
        }
        checkDuplicateTag(activity, tag);

        final NavigationScene navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                navigationSceneOptions.toBundle());
        navigationScene.setSeparateCreateFromCreateView(separateCreateFromCreateView);
        if (!Utility.isActivityStatusValid(activity)) {
            return new DestroyedSceneDelegate(navigationScene);
        }
        navigationScene.setRootSceneComponentFactory(rootSceneComponentFactory);

        FragmentManager fragmentManager = activity.getFragmentManager();
        LifeCycleFragment lifeCycleFragment = (LifeCycleFragment) fragmentManager.findFragmentByTag(tag);
        if (lifeCycleFragment != null && !supportRestore) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(lifeCycleFragment);
            Utility.commitFragment(fragmentManager, transaction, immediate);
            lifeCycleFragment = null;
        }

        ViewFinder viewFinder = new ActivityViewFinder(activity);

        ScopeHolderFragment targetScopeHolderFragment = null;
        SceneLifecycleDispatcher<NavigationScene> dispatcher = null;
        if (lifeCycleFragment != null) {
            final ScopeHolderFragment scopeHolderFragment = ScopeHolderFragment.install(activity, tag, false, immediate);
            dispatcher = new SceneLifecycleDispatcher<>(idRes, viewFinder, navigationScene, scopeHolderFragment, supportRestore);
            lifeCycleFragment.setSceneContainerLifecycleCallback(dispatcher);
            targetScopeHolderFragment = scopeHolderFragment;
        } else {
            lifeCycleFragment = LifeCycleFragment.newInstance(supportRestore);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(idRes, lifeCycleFragment, tag);

            final ScopeHolderFragment scopeHolderFragment = ScopeHolderFragment.install(activity, tag, !supportRestore, immediate);
            dispatcher = new SceneLifecycleDispatcher<>(idRes, viewFinder, navigationScene, scopeHolderFragment, supportRestore);
            lifeCycleFragment.setSceneContainerLifecycleCallback(dispatcher);

            Utility.commitFragment(fragmentManager, transaction, immediate);
            targetScopeHolderFragment = scopeHolderFragment;
        }
        final LifeCycleFragmentSceneDelegate delegate = new LifeCycleFragmentSceneDelegate(activity, navigationScene, lifeCycleFragment, targetScopeHolderFragment, immediate);
        return delegate;
    }

    private static void checkDuplicateTag(@NonNull Activity activity, @NonNull String tag) {
        GroupSceneUtility.checkDuplicateTag(activity, tag);
    }

    static void removeTag(@NonNull Activity activity, @NonNull String tag) {
        GroupSceneUtility.removeTag(activity, tag);
    }
}