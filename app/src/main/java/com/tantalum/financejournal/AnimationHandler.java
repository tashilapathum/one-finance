package com.tantalum.financejournal;

import android.content.res.Resources;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;

public class AnimationHandler {

    public LayoutAnimationController getSlideUpController() {
        //animation
        AnimationSet set = new AnimationSet(true);
        //fade in
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        fadeIn.setFillAfter(true);
        set.addAnimation(fadeIn);
        //slide up
        Animation slideUp = new TranslateAnimation(0,0, Resources.getSystem().getDisplayMetrics().heightPixels/2, 0);
        slideUp.setInterpolator(new AccelerateDecelerateInterpolator());
        slideUp.setDuration(400);
        set.addAnimation(slideUp);
        //controller
        return new LayoutAnimationController(set, 0.07f);
    }

}
