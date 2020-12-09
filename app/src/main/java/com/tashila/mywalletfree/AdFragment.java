package com.tashila.mywalletfree;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdFragment extends Fragment {
    private View view;
    private MoPubView adView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ad_fragment, null);
        final SdkConfiguration.Builder configBuilder = new SdkConfiguration.Builder("252412d5e9364a05ab77d9396346d73d");
        configBuilder.withLogLevel(MoPubLog.LogLevel.DEBUG);
        MoPub.initializeSdk(getActivity(), configBuilder.build(), sdkInitializationListener());
        adView = view.findViewById(R.id.rectAd);

        return view;
    }

    private SdkInitializationListener sdkInitializationListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                adView.setAdUnitId("252412d5e9364a05ab77d9396346d73d");
                adView.loadAd();
            }
        };
    }
}
