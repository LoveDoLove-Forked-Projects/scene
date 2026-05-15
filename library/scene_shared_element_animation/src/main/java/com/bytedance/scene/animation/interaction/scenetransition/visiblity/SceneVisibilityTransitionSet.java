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
//package com.bytedance.scene.animation.interaction.scenetransition.visiblity;
//
//import android.animation.Animator;
//import androidx.annotation.NonNull;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.bytedance.scene.animation.TransitionUtils;
//import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
//import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimationSet;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class SceneVisibilityTransitionSet extends SceneVisibilityTransition {
//    private ArrayList<SceneVisibilityTransition> mTransitions = new ArrayList<>();
//
//    @Override
//    public void captureValue(@NonNull View view, @NonNull ViewGroup rootView) {
//        super.captureValue(view, rootView);
//        for (SceneVisibilityTransition sceneTransition : mTransitions) {
//            sceneTransition.captureValue(view, rootView);
//        }
//    }
//
//    public void addSceneVisibilityTransition(SceneVisibilityTransition sceneTransition) {
//        this.mTransitions.add(sceneTransition);
//    }
//
//    public void removeSceneVisibilityTransition(SceneVisibilityTransition sceneTransition) {
//        this.mTransitions.remove(sceneTransition);
//    }
//
//    @Override
//    public InteractionAnimation getAnimation(boolean appear) {
//        InteractionAnimationSet InteractionAnimationSet = new InteractionAnimationSet();
//        for (SceneVisibilityTransition sceneTransition : mTransitions) {
//            InteractionAnimationSet.addInteractionAnimation(sceneTransition.getAnimation(appear));
//        }
//        return InteractionAnimationSet;
//    }
//
//    @Override
//    public Animator getAnimator(boolean appear) {
//        List<Animator> animatorList = new ArrayList<>();
//        for (SceneVisibilityTransition sceneTransition : mTransitions) {
//            animatorList.add(sceneTransition.getAnimator(appear));
//        }
//
//        long minDelay = Long.MAX_VALUE;
//        for (Animator animator : animatorList) {
//            minDelay = Math.min(minDelay, animator.getStartDelay());
//        }
//
//        //保证肯定有一个是0 delay
//        for (Animator animator : animatorList) {
//            animator.setStartDelay(animator.getStartDelay() - minDelay);
//        }
//        return TransitionUtils.mergeAnimators(animatorList);
//    }
//
//    @Override
//    public void setDuration(long duration) {
//        super.setDuration(duration);
//        for (SceneVisibilityTransition sceneTransition : mTransitions) {
//            sceneTransition.setDuration(duration);
//        }
//    }
//
//    @Override
//    public void finish(boolean appear) {
//        for (SceneVisibilityTransition sceneTransition : mTransitions) {
//            sceneTransition.finish(appear);
//        }
//    }
//}
