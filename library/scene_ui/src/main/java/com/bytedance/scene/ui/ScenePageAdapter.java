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
package com.bytedance.scene.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.UserVisibleHintGroupScene;

/**
 * Created by JiangQi on 7/30/18.
 */
public abstract class ScenePageAdapter extends PagerAdapter {
    private GroupScene mGroupScene;
    private UserVisibleHintGroupScene mCurrentScene;

    public ScenePageAdapter(GroupScene scene) {
        this.mGroupScene = scene;
    }

    public abstract UserVisibleHintGroupScene getItem(int position);

    @Nullable
    public UserVisibleHintGroupScene getCurrentScene() {
        return this.mCurrentScene;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        int id = container.getId();
        if (id == View.NO_ID) {
            throw new IllegalArgumentException("ViewPager should have view id before use ScenePageAdapter");
        }

        long itemId = getItemId(position);
        String name = makeFragmentName(itemId);

        ViewPager viewPager = (ViewPager) container;
        UserVisibleHintGroupScene scene = mGroupScene.findSceneByTag(name);
        if (scene != null) {
            // setUserVisibleHint() must be invoked first, as the GroupScene.show() is directly executed.
            configSceneUserVisibleHint(viewPager, scene, position);
            mGroupScene.show(scene);
        } else {
            scene = getItem(position);
            configSceneUserVisibleHint(viewPager, scene, position);
            mGroupScene.add(id, scene, name);
        }
        return scene;
    }

    private void configSceneUserVisibleHint(ViewPager viewPager, UserVisibleHintGroupScene scene, int position) {
        if (mCurrentScene == null) {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem == position) {
                mCurrentScene = scene;
            }
        }

        boolean visible = scene == mCurrentScene;
        if (visible) {
            if (scene.getUserVisibleHint()) {
                //to ensure that the behavior will be consistent with FragmentPagerAdapter
                scene.setUserVisibleHint(false);
            }
            scene.setUserVisibleHint(true);
        } else {
            if (scene.getUserVisibleHint()) {
                scene.setUserVisibleHint(false);
            }
        }
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        UserVisibleHintGroupScene scene = (UserVisibleHintGroupScene) object;
        if (scene != mCurrentScene) {
            if (mCurrentScene != null) {
                mCurrentScene.setUserVisibleHint(false);
            }
            if (scene != null) {
                scene.setUserVisibleHint(true);
            }
            mCurrentScene = scene;
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Scene scene = (Scene) object;
        mGroupScene.remove(scene);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return ((Scene) object).getView() == view;
    }

    public long getItemId(int position) {
        return position;
    }

    private static String makeFragmentName(long id) {
        return "android:switcher:" + id;
    }
}
