package com.bytedance.scene;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;

import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 11/6/18.
 */
public class SceneLifecycleManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "SceneLifecycleManager";

    private NavigationScene mNavigationScene;
    private boolean mSupportRestore = false;

    public void onActivityCreated(@NonNull Activity activity,
                                  @NonNull ViewGroup viewGroup,
                                  @NonNull NavigationScene navigationScene,
                                  @NonNull NavigationScene.NavigationSceneHost navigationSceneHost,
                                  @NonNull Scope.RootScopeFactory rootScopeFactory,
                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                  @Nullable Bundle savedInstanceState) {
        if (navigationScene.getState() != State.NONE) {
            throw new IllegalStateException("NavigationScene state must be " + State.NONE.name);
        }
        if (activity == null) {
            throw new NullPointerException("activity can't be null");
        }
        if (navigationScene == null) {
            throw new NullPointerException("viewGroup can't be null");
        }
        if (navigationScene == null) {
            throw new NullPointerException("navigationScene can't be null");
        }
        if (navigationScene == null) {
            throw new NullPointerException("navigationSceneHost can't be null");
        }
        if (navigationScene == null) {
            throw new NullPointerException("rootScopeFactory can't be null");
        }

        this.mSupportRestore = navigationSceneHost.isSupportRestore();
        if (!this.mSupportRestore && savedInstanceState != null) {
            throw new IllegalArgumentException("savedInstanceState should be null when not support restore");
        }

        log("onActivityCreated");

        this.mNavigationScene = navigationScene;
        this.mNavigationScene.setRootScopeFactory(rootScopeFactory);
        this.mNavigationScene.setNavigationSceneHost(navigationSceneHost);
        this.mNavigationScene.setRootSceneComponentFactory(rootSceneComponentFactory);
        this.mNavigationScene.dispatchAttachActivity(activity);
        this.mNavigationScene.dispatchAttachScene(null);
        this.mNavigationScene.dispatchCreate(savedInstanceState);
        this.mNavigationScene.dispatchCreateView(savedInstanceState, viewGroup);
        viewGroup.addView(this.mNavigationScene.getView(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.mNavigationScene.dispatchActivityCreated(savedInstanceState);
    }

    public void onStart() {
        if (this.mNavigationScene.getState() != State.ACTIVITY_CREATED) {
            throw new IllegalStateException("NavigationScene state must be " + State.ACTIVITY_CREATED.name);
        }
        log("onStart");
        this.mNavigationScene.dispatchStart();
    }

    public void onResume() {
        if (this.mNavigationScene.getState() != State.STARTED) {
            throw new IllegalStateException("NavigationScene state must be " + State.STARTED.name);
        }
        log("onResume");
        this.mNavigationScene.dispatchResume();
    }

    public void onPause() {
        if (this.mNavigationScene.getState() != State.RESUMED) {
            throw new IllegalStateException("NavigationScene state must be " + State.RESUMED.name);
        }
        log("onPause");
        this.mNavigationScene.dispatchPause();
    }

    public void onStop() {
        if (this.mNavigationScene.getState() != State.STARTED) {
            throw new IllegalStateException("NavigationScene state must be " + State.STARTED.name);
        }
        log("onStop");
        this.mNavigationScene.dispatchStop();
    }

    public void onDestroyView() {
        if (this.mNavigationScene.getState() != State.ACTIVITY_CREATED) {
            throw new IllegalStateException("NavigationScene state must be " + State.ACTIVITY_CREATED.name);
        }
        log("onDestroyView");
        this.mNavigationScene.dispatchDestroyView();
        this.mNavigationScene.dispatchDestroy();
        this.mNavigationScene.dispatchDetachScene();
        this.mNavigationScene.dispatchDetachActivity();
        this.mNavigationScene.setRootSceneComponentFactory(null);
        this.mNavigationScene.setNavigationSceneHost(null);
        this.mNavigationScene.setRootScopeFactory(null);
        this.mNavigationScene = null;
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (!this.mSupportRestore) {
            throw new IllegalArgumentException("cant invoke onSaveInstanceState when not support restore");
        }

        log("onSaveInstanceState");
        this.mNavigationScene.dispatchSaveInstanceState(outState);
    }

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        log("onConfigurationChanged");
        if (this.mNavigationScene != null) {
            this.mNavigationScene.onConfigurationChanged(newConfig);
        }
    }

    private void log(String log) {
        if (DEBUG) {
            Log.d(TAG + "#" + hashCode(), log);
        }
    }
}
