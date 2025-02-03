package com.tantalum.onefinance.pro;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.tantalum.onefinance.R;

import java.util.Locale;


public class UpgradeToProActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    private Context context;
    private SharedPreferences sharedPref;
    public static final String TAG = "UpgradeToProActivity";
    public static final String PRODUCT_ID = "one_finance_pro_v2";
    private BillingProcessor bp;
    private boolean isBillingReady = false;
    private Button btnBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

        /*------------------------------Essential for every activity------------------------------*/
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        //language
        String language = sharedPref.getString("language", "english");
        Locale locale;
        if (language.equals("සිංහල"))
            locale = new Locale("si");
        else
            locale = new Locale("en");

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_to_pro);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        /*----------------------------------------------------------------------------------------*/

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

}




