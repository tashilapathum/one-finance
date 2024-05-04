package com.tantalum.onefinance;

import android.app.Application;

import com.google.firebase.FirebaseApp;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CaocConfig.Builder.create()
                .errorDrawable(R.drawable.customactivityoncrash_error_image)
                .apply();
        FirebaseApp.initializeApp(getApplicationContext());
    }
}
