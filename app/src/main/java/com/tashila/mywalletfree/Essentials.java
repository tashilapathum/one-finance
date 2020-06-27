package com.tashila.mywalletfree;

import android.content.Context;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class Essentials {
    private Context context;

    Essentials(Context context) {
        this.context = context;
    }

    private static final float[] NEGATIVE = {
            -1.0f,     0,     0,    0, 255, // red
            0, -1.0f,     0,    0, 255, // green
            0,     0, -1.0f,    0, 255, // blue
            0,     0,     0, 1.0f,   0  // alpha
    };

    void invertDrawable(View imageView) {
        ImageView iv = (ImageView) imageView;
        Drawable d = iv.getDrawable();
        d.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
    }
}
