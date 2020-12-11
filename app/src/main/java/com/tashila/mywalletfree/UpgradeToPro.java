package com.tashila.mywalletfree;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideos;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.mopub.common.logging.MoPubLog.LogLevel.DEBUG;

public class UpgradeToPro extends AppCompatActivity implements PurchasesUpdatedListener, BillingClientStateListener, MoPubRewardedVideoListener {
    SharedPreferences sharedPref;
    private BillingClient billingClient;
    private TextView tvProPrice;
    BillingFlowParams flowParams;
    public static final String TAG = "UpgradeToPro";
    private MaterialButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*------------------------------Essential for every activity-----------------------------*/

        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        //language
        String language = sharedPref.getString("language", "english");
        if (language.equals("සිංහල")) {
            Locale locale = new Locale("si");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        //theme
        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_upgrade_to_pro);
            View layout = findViewById(R.id.root_layout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_upgrade_to_pro);
        }

        /*----------------------------------------------------------------------------------------*/

        //billing
        billingClient = BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(this);

        //ads
        final SdkConfiguration.Builder configBuilder = new SdkConfiguration.Builder("920b6145fb1546cf8b5cf2ac34638bb7");
        configBuilder.withLogLevel(DEBUG);
        MoPub.initializeSdk(this, configBuilder.build(), initSdkListener());
        MoPubRewardedVideos.setRewardedVideoListener(this);

        tvProPrice = findViewById(R.id.tvProPrice);
        button = findViewById(R.id.btnRemoveAds);
        Button btnBuy = findViewById(R.id.btnBuy);
        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBuy();
            }
        });
        Button btnRestore = findViewById(R.id.btnRestore);
        btnRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRestore();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPref.getBoolean("exit", false)) {
            finishAndRemoveTask();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.edit().putBoolean("exit", false).apply();
        sharedPref.edit().putBoolean("fromRestore", false).apply();
    }

    @Override //so the language change works with dark mode
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    public void goBack(View view) {
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        sharedPref.edit().putBoolean("fromRestore", false).apply();
    }


    /*--------------------------------------- purchase -------------------------------------------*/

    private void onClickBuy() {
        TextView price = findViewById(R.id.tvProPrice);
        if (price.getText().toString().contains("…"))
            Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        else
            continueBuyOrRestore();
    }

    private void onClickRestore() {
        sharedPref.edit().putBoolean("fromRestore", true).apply();
        TextView price = findViewById(R.id.tvProPrice);
        if (price.getText().toString().contains("…"))
            Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        else
            continueBuyOrRestore();
    }

    private void continueBuyOrRestore() {
        if (isNetworkAvailable()) {
            try {
                billingClient.launchBillingFlow(this, flowParams);
            } catch (NullPointerException e) {
                e.printStackTrace();
                Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            }
        } else {
            new MaterialAlertDialogBuilder(UpgradeToPro.this)
                    .setTitle(R.string.connec_failed)
                    .setMessage(R.string.must_have_internet)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onClickBuy();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null)
            for (Purchase purchase : list)
                handlePurchase(purchase);
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED)
            Toast.makeText(this, R.string.p_cancelled, Toast.LENGTH_SHORT).show();
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            boolean fromRestore = sharedPref.getBoolean("fromRestore", false);
            if (!fromRestore)
                Toast.makeText(this, R.string.p_already, Toast.LENGTH_SHORT).show();
            else {
                sharedPref.edit().putBoolean("MyWalletPro", true).apply();
                Toast.makeText(this, R.string.p_restored, Toast.LENGTH_LONG).show();
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.p_restored)
                        .setMessage(R.string.thank_u_for_pro)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(UpgradeToPro.this, MainActivity.class));
                            }
                        })
                        .setCancelable(false)
                        .show();
                sharedPref.edit().putBoolean("fromRestore", false).apply();
            }
        } else Toast.makeText(this, R.string.p_failed, Toast.LENGTH_SHORT).show();
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            sharedPref.edit().putBoolean("MyWalletPro", true).apply();
        }

        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                    new MaterialAlertDialogBuilder(UpgradeToPro.this)
                            .setTitle(R.string.p_success)
                            .setMessage(R.string.thank_u_for_pro)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(UpgradeToPro.this, MainActivity.class));
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            });
        }
    }

    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            Log.i(TAG, "Billing Response OK!");
            querySkuDetails();
            queryPurchases();
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        if (!isFinishing()) {
            new MaterialAlertDialogBuilder(UpgradeToPro.this)
                    .setTitle(R.string.connection_problem)
                    .setMessage(R.string.failed_gplay)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            recreate();
                        }
                    })
                    .show();
        }
    }

    public void querySkuDetails() {
        List<String> skuList = new ArrayList<>();
        skuList.add("upgrade_to_pro");

        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                .setSkusList(skuList)
                .build();

        Log.i(TAG, "querySkuDetailsAsync");
        billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, List<SkuDetails> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                    for (SkuDetails skuDetails : list) {
                        String sku = skuDetails.getSku();
                        String price = skuDetails.getPrice();
                        String proVersionPrice = null;
                        if ("upgrade_to_pro".equals(sku))
                            proVersionPrice = price;
                        tvProPrice.setText(proVersionPrice); //show price
                        findViewById(R.id.priceSub).setVisibility(View.VISIBLE);
                        flowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                    }
                }
            }
        });
    }

    public void queryPurchases() {
        if (!billingClient.isReady()) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready");
        }
        billingClient.queryPurchases(BillingClient.SkuType.INAPP);
    }


    /*------------------------------------- rewarded ad -----------------------------------------*/

    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                MoPubRewardedVideos.loadRewardedVideo("920b6145fb1546cf8b5cf2ac34638bb7");
            }
        };
    }

    public void showAd(View view) {
        if (MoPubRewardedVideos.hasRewardedVideo("920b6145fb1546cf8b5cf2ac34638bb7"))
            MoPubRewardedVideos.showRewardedVideo("920b6145fb1546cf8b5cf2ac34638bb7");
        else
            Toast.makeText(this, getString(R.string.loading), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
        button.setText(R.string.watch_ad);
    }

    @Override
    public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage("Failed to load the ad\n\n" + "Error code: " + errorCode.toString())
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    public void onRewardedVideoStarted(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage("Failed to play the ad\n\n" + "Error code: " + errorCode.toString())
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    public void onRewardedVideoClicked(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoClosed(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {
        Toast.makeText(this, getString(R.string.removed_ads), Toast.LENGTH_LONG).show();

        int today = new DateTimeHandler().getDayOfYear();
        int adsDeadline = sharedPref.getInt("adsDeadline", 0);
        int daysWithoutAds = adsDeadline - today;

        //countdown
        LocalDateTime deadline = LocalDateTime.now().plusDays(5);
        int newAdsDeadline = daysWithoutAds + new DateTimeHandler(deadline).getDayOfYear();
        sharedPref.edit().putInt("adsDeadline", newAdsDeadline).apply();
        TextView tvDays = findViewById(R.id.days);
        tvDays.append(String.valueOf(newAdsDeadline));

        //load next
        button.setText(R.string.load_ad);
        MoPubRewardedVideos.loadRewardedVideo("920b6145fb1546cf8b5cf2ac34638bb7");
    }
}




