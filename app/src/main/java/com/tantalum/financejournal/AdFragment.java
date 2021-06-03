package com.tantalum.financejournal;

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
    //test ad: 252412d5e9364a05ab77d9396346d73d
    //real ad: 07603c394938458889922bfca070cb34
    private View view;
    private MoPubView adView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ad_fragment, null);
        final SdkConfiguration.Builder configBuilder = new SdkConfiguration.Builder("07603c394938458889922bfca070cb34");
        configBuilder.withLogLevel(MoPubLog.LogLevel.DEBUG);
        /*//adColony
        Map<String, String> adColonyConfigs = new HashMap<>();
        adColonyConfigs.put("appId", "app061c223313354c859e");
        String[] allZoneIds = new String[]{"vz4433c4cd77034ce88c", "vze71acd6eab1348b1a9"};
        adColonyConfigs.put("allZoneConfigs", Arrays.toString(allZoneIds));
        configBuilder.withMediatedNetworkConfiguration(AdColonyAdapterConfiguration.class.getName(), adColonyConfigs);*/
        MoPub.initializeSdk(getActivity(), configBuilder.build(), sdkInitializationListener());
        adView = view.findViewById(R.id.rectAd);

        return view;
    }

    private SdkInitializationListener sdkInitializationListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                adView.setAdUnitId("07603c394938458889922bfca070cb34");
                adView.loadAd();
            }
        };
    }
}
