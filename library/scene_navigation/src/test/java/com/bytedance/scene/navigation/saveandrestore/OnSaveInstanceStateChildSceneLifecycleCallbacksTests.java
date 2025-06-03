package com.bytedance.scene.navigation.saveandrestore;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.Scope;
import com.bytedance.scene.State;
import com.bytedance.scene.interfaces.ChildSceneLifecycleAdapterCallbacks;
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.navigation.NavigationSourceUtility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class OnSaveInstanceStateChildSceneLifecycleCallbacksTests {
    @Test
    public void testNavigationSceneNotRecursiveExcludingOnSceneSaveInstanceState() {
        final AtomicBoolean isPreCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreViewCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreActivityCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreStartedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreResumedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreSaveInstanceState = new AtomicBoolean(false);
        final AtomicBoolean isPrePausedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreStoppedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreViewDestroyedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreDestroyedCalled = new AtomicBoolean(false);

        final AtomicBoolean isSuperCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isSuperViewCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isSuperActivityCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isSuperStartedCalled = new AtomicBoolean(false);
        final AtomicBoolean isSuperResumedCalled = new AtomicBoolean(false);
        final AtomicBoolean isSuperSaveInstanceState = new AtomicBoolean(false);
        final AtomicBoolean isSuperPausedCalled = new AtomicBoolean(false);
        final AtomicBoolean isSuperStoppedCalled = new AtomicBoolean(false);
        final AtomicBoolean isSuperViewDestroyedCalled = new AtomicBoolean(false);
        final AtomicBoolean isSuperDestroyedCalled = new AtomicBoolean(false);

        final AtomicBoolean isCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isViewCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isActivityCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isStartedCalled = new AtomicBoolean(false);
        final AtomicBoolean isResumedCalled = new AtomicBoolean(false);
        final AtomicBoolean isSaveInstanceState = new AtomicBoolean(false);
        final AtomicBoolean isPausedCalled = new AtomicBoolean(false);
        final AtomicBoolean isStoppedCalled = new AtomicBoolean(false);
        final AtomicBoolean isViewDestroyedCalled = new AtomicBoolean(false);
        final AtomicBoolean isDestroyedCalled = new AtomicBoolean(false);

        ChildSceneLifecycleCallbacks callbacks = new ChildSceneLifecycleCallbacks() {
            @Override
            public void onPreSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (!isPreCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNull(scene.getView());
            }

            @Override
            public void onPreSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (!isPreViewCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
            }

            @Override
            public void onPreSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (!isPreActivityCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
            }

            @Override
            public void onPreSceneStarted(@NonNull Scene scene) {
                if (!isPreStartedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreSceneResumed(@NonNull Scene scene) {
                if (!isPreResumedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreScenePaused(@NonNull Scene scene) {
                if (!isPrePausedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreSceneStopped(@NonNull Scene scene) {
                if (!isPreStoppedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreSceneViewDestroyed(@NonNull Scene scene) {
                if (!isPreViewDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreSceneDestroyed(@NonNull Scene scene) {
                if (!isPreDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState) {
                if (!isPreSaveInstanceState.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSuperSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                assertTrue(isPreCreatedCalled.get());
                if (!isSuperCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSuperSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                assertTrue(isPreViewCreatedCalled.get());
                if (!isSuperViewCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSuperSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                assertTrue(isPreActivityCreatedCalled.get());
                if (!isSuperActivityCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSuperSceneStarted(@NonNull Scene scene) {
                assertTrue(isPreStartedCalled.get());
                if (!isSuperStartedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSuperSceneResumed(@NonNull Scene scene) {
                assertTrue(isPreResumedCalled.get());
                if (!isSuperResumedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSuperSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState) {
                assertTrue(isPreSaveInstanceState.get());
                if (!isSuperSaveInstanceState.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSuperScenePaused(@NonNull Scene scene) {
                assertTrue(isPrePausedCalled.get());
                if (!isSuperPausedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSuperSceneStopped(@NonNull Scene scene) {
                assertTrue(isPreStoppedCalled.get());
                if (!isSuperStoppedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSuperSceneViewDestroyed(@NonNull Scene scene) {
                assertTrue(isPreViewDestroyedCalled.get());
                if (!isSuperViewDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSuperSceneDestroyed(@NonNull Scene scene) {
                assertTrue(isPreDestroyedCalled.get());
                if (!isSuperDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                assertTrue(isPreCreatedCalled.get());
                assertTrue(isSuperCreatedCalled.get());

                if (!isCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNull(scene.getView());
                assertSame(State.NONE, scene.getState());
            }

            @Override
            public void onSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                assertTrue(isPreViewCreatedCalled.get());
                assertTrue(isSuperViewCreatedCalled.get());

                if (!isViewCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
                if (scene.isSeparateCreateFromCreateView()) {
                    assertSame(State.CREATED, scene.getState());
                } else {
                    assertSame(State.NONE, scene.getState());
                }
            }

            @Override
            public void onSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                assertTrue(isPreActivityCreatedCalled.get());
                assertTrue(isSuperActivityCreatedCalled.get());

                if (!isActivityCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
                assertSame(State.VIEW_CREATED, scene.getState());//Scene state is set to State.Activity_CREATED after onActivityCreated() and onSceneActivityCreated()
            }

            @Override
            public void onSceneStarted(@NonNull Scene scene) {
                assertTrue(isPreStartedCalled.get());
                assertTrue(isSuperStartedCalled.get());

                if (!isStartedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.ACTIVITY_CREATED, scene.getState());
            }

            @Override
            public void onSceneResumed(@NonNull Scene scene) {
                assertTrue(isPreResumedCalled.get());
                assertTrue(isSuperResumedCalled.get());

                if (!isResumedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.STARTED, scene.getState());
            }

            @Override
            public void onSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState) {
                assertTrue(isPreSaveInstanceState.get());
                assertTrue(isSuperSaveInstanceState.get());

                if (!isSaveInstanceState.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onScenePaused(@NonNull Scene scene) {
                assertTrue(isPrePausedCalled.get());
                assertTrue(isSuperPausedCalled.get());

                if (!isPausedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.STARTED, scene.getState());
            }

            @Override
            public void onSceneStopped(@NonNull Scene scene) {
                assertTrue(isPreStoppedCalled.get());
                assertTrue(isSuperStoppedCalled.get());

                if (!isStoppedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.ACTIVITY_CREATED, scene.getState());
            }

            @Override
            public void onSceneViewDestroyed(@NonNull Scene scene) {
                assertTrue(isPreViewDestroyedCalled.get());
                assertTrue(isSuperViewDestroyedCalled.get());

                if (!isViewDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
                if (scene.isSeparateCreateFromCreateView()) {
                    assertSame(State.CREATED, scene.getState());
                } else {
                    assertSame(State.NONE, scene.getState());
                }
            }

            @Override
            public void onSceneDestroyed(@NonNull Scene scene) {
                assertTrue(isPreDestroyedCalled.get());
                assertTrue(isSuperDestroyedCalled.get());

                if (!isDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNull(scene.getView());
            }
        };

        final Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };

        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(rootScene.getClass());
        options.setUsePostInLifecycle(true);
        navigationScene.setArguments(options.toBundle());
        navigationScene.registerChildSceneLifecycleCallbacks(callbacks, false);

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        SceneComponentFactory sceneComponentFactory = new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                if (className.equals(rootScene.getClass().getName())) {
                    return rootScene;
                }
                return null;
            }
        };

        navigationScene.setDefaultNavigationAnimationExecutor(null);
        navigationScene.setRootSceneComponentFactory(sceneComponentFactory);

        SceneLifecycleManager<NavigationScene> lifecycleManager = new SceneLifecycleManager<>();
        lifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, rootScopeFactory,
                true, null);
        lifecycleManager.onStart();
        lifecycleManager.onResume();
        lifecycleManager.onPause();
        lifecycleManager.onSaveInstanceState(new Bundle());
        lifecycleManager.onStop();
        lifecycleManager.onDestroyView();

        assertTrue(isPreCreatedCalled.get());
        assertTrue(isPreViewCreatedCalled.get());
        assertTrue(isPreActivityCreatedCalled.get());
        assertTrue(isPreStartedCalled.get());
        assertTrue(isPreResumedCalled.get());
        assertTrue(isPrePausedCalled.get());
        assertTrue(isPreSaveInstanceState.get());
        assertTrue(isPreStoppedCalled.get());
        assertTrue(isPreViewDestroyedCalled.get());
        assertTrue(isPreDestroyedCalled.get());

        assertTrue(isCreatedCalled.get());
        assertTrue(isViewCreatedCalled.get());
        assertTrue(isActivityCreatedCalled.get());
        assertTrue(isStartedCalled.get());
        assertTrue(isResumedCalled.get());
        assertTrue(isPausedCalled.get());
        assertTrue(isSaveInstanceState.get());
        assertTrue(isStoppedCalled.get());
        assertTrue(isViewDestroyedCalled.get());
        assertTrue(isDestroyedCalled.get());
    }

    @Test
    public void testNavigationSceneUnregisterChildSceneLifecycleCallbacks() {
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        });
        ChildSceneLifecycleCallbacks callbacks = new ChildSceneLifecycleAdapterCallbacks();
        navigationScene.registerChildSceneLifecycleCallbacks(callbacks, false);
        navigationScene.unregisterChildSceneLifecycleCallbacks(callbacks);
    }

    public static class TestActivity extends Activity {
        public FrameLayout mFrameLayout;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mFrameLayout = new FrameLayout(this);
            setContentView(mFrameLayout);
        }
    }
}
