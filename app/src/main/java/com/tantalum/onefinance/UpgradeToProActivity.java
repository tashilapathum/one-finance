package com.tantalum.onefinance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;


public class UpgradeToProActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    private Context context;
    SharedPreferences sharedPref;
    private TextView tvProPrice;
    public static final String TAG = "UpgradeToProActivity";
    public static final String PRODUCT_ID = "one_finance_pro_v2";
    private BillingProcessor bp;
    private boolean isBillingReady = false;
    private Button btnBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

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

        tvProPrice = findViewById(R.id.tvProPrice);
        btnBuy = findViewById(R.id.btnBuy);
        btnBuy.setText(R.string.loading);
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

        //initialize billing
        bp = new BillingProcessor(context, null, this);
        bp.initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPref.getBoolean("exit", false)) {
            finishAndRemoveTask();
        }

        boolean scroll = getIntent().getBooleanExtra("scroll", false);
        if (scroll) {
            ScrollView scrollView = findViewById(R.id.scrollView);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
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
        /*if (price.getText().toString().contains("…"))
            Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        else
            continueBuyOrRestore();*/
        startProcess();
    }

    private void onClickRestore() {
        sharedPref.edit().putBoolean("fromRestore", true).apply();
        TextView price = findViewById(R.id.tvProPrice);
        continueBuyOrRestore();
    }

    private void continueBuyOrRestore() {
        if (isNetworkAvailable()) {
            try {
                startProcess();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            }
        } else {
            new MaterialAlertDialogBuilder(UpgradeToProActivity.this)
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

    private void startProcess() {
        if (isBillingReady)
            bp.purchase(UpgradeToProActivity.this, PRODUCT_ID);
        else
            Toast.makeText(context, R.string.loading, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBillingInitialized() {
        isBillingReady = true;
        btnBuy.setText(R.string.buy);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {
        UpgradeHandler.activatePro(context);
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        if (error != null)
            Log.e(TAG, error.getMessage());
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    /*@Override
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
                //sharedPref.edit().putBoolean("MyWalletPro", true).apply();
                UpgradeHandler.activatePro(context);
                Toast.makeText(this, R.string.p_restored, Toast.LENGTH_LONG).show();
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.p_restored)
                        .setMessage(R.string.thank_u_for_pro)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(UpgradeToProActivity.this, MainActivity.class));
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
            //sharedPref.edit().putBoolean("MyWalletPro", true).apply();
            UpgradeHandler.activatePro(context);
        }

        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                    new MaterialAlertDialogBuilder(UpgradeToProActivity.this)
                            .setTitle(R.string.p_success)
                            .setMessage(R.string.thank_u_for_pro)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(UpgradeToProActivity.this, MainActivity.class));
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
            new MaterialAlertDialogBuilder(UpgradeToProActivity.this)
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
        skuList.add("one_finance_pro");

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
                        if ("one_finance_pro".equals(sku))
                            proVersionPrice = price;
                        tvProPrice.setText(proVersionPrice); //show price
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
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {

            }
        });
    }*/

}




