package com.tashila.mywalletfree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    SharedPreferences sharedPref;
    public static final String TAG = "MainActivity";
    BottomNavigationView bottomNav;
    private FirebaseAnalytics firebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //to exit the app
        if (getIntent().getBooleanExtra("shouldExit", false))
            finish();

        /*------------------------------Essential for every activity------------------------------*/
        Toolbar toolbar;
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        //language
        String language = sharedPref.getString("language", "english");
        Locale locale = null;
        if (language.equals("සිංහල"))
            locale = new Locale("si");
        else
            locale = new Locale("en");

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());


        //theme
        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            View layout = findViewById(R.id.drawer_layout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
            toolbar = findViewById(R.id.toolbar);
            toolbar.setBackground(getDrawable(R.color.colorToolbarDark));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            toolbar = findViewById(R.id.toolbar);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /*----------------------------------------------------------------------------------------*/
        AndroidThreeTen.init(this);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        String homeScreen = sharedPref.getString("homeScreen", "wallet");

        ////STARTUP////
        //when requested after applying settings
        if (sharedPref.getBoolean("reqOpenBank", false)) {
            navigateScreens(new BankFragment(), "BankFragment", R.id.nav_bank);
            sharedPref.edit().putBoolean("reqOpenBank", false).apply();
        }
        //when set as home screen (won't set it back to false)
        else if (homeScreen.equalsIgnoreCase("bank"))
            navigateScreens(new BankFragment(), "BankFragment", R.id.nav_bank);
        else if (homeScreen.equalsIgnoreCase("cart"))
            navigateScreens(new CartFragment(), "CartFragment", R.id.nav_cart);
        else if (homeScreen.equalsIgnoreCase("bills"))
            navigateScreens(new BillsFragment(), "BillsFragment", R.id.nav_bills);
            //when starting the app normally
        else
            navigateScreens(new WalletFragment(), "WalletFragment", R.id.nav_wallet);

        //set notification
        boolean isNotificationSet = sharedPref.getBoolean("isNotificationSet", false);
        if (!isNotificationSet) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 20); //20 means default is at 8
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent notifyIntent = new Intent(this, AlertReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, notifyIntent, 0);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            sharedPref.edit().putBoolean("isNotificationSet", true).apply();
        }

        if (getPackageName().contains("debug"))
            sharedPref.edit().putBoolean("MyWalletPro", true).apply();
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

    public void onStart() {
        super.onStart();
        //init setup
        Intent intent = new Intent(this, InitialSetup.class);
        boolean alreadyDid = sharedPref.getBoolean("alreadyDidInitSetup", false);
        if (!alreadyDid) startActivity(intent);
        else {
            //what's new
            if (!sharedPref.getBoolean("whatsNewShownV0.2.8", false)) {
                DialogWhatsNew dialogWhatsNew = new DialogWhatsNew();
                dialogWhatsNew.show(getSupportFragmentManager(), "whats new dialog");
                sharedPref.edit().putBoolean("whatsNewShownV0.2.8", true).apply();
            }
        }
    }

    //nav drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.nav_home: {
                bundle.putString("feature", "home");
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_recent_trans: {
                bundle.putString("feature", "transactions");
                Intent intent = new Intent(this, TransactionHistory.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_reports: {
                bundle.putString("feature", "reports");
                Intent intent = new Intent(this, ReportsNEW.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_settings: {
                bundle.putString("feature", "settings");
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_get_pro: {
                bundle.putString("feature", "get_pro");
                Intent intent = new Intent(this, UpgradeToPro.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_about: {
                bundle.putString("feature", "about");
                Intent intent = new Intent(this, About.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_exit: {
                bundle.putString("feature", "exit");
                sharedPref.edit().putBoolean("exit", true).apply();
                finishAndRemoveTask();
                break;
            }
        }
        firebaseAnalytics.logEvent("used_feature", bundle);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            boolean confirmExit = sharedPref.getBoolean("exitConfirmation", false);
            if (confirmExit) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.exit_confirm)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sharedPref.edit().putBoolean("exit", true).apply();
                                finishAndRemoveTask();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            } else finishAndRemoveTask();
        }
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
    }

    //bottom nav
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    String fragmentTag = null;
                    switch (item.getItemId()) {
                        case R.id.nav_wallet: {
                            selectedFragment = new WalletFragment();
                            fragmentTag = "WalletFragment";
                            break;
                        }
                        case R.id.nav_bank: {
                            selectedFragment = new BankFragment();
                            fragmentTag = "BankFragment";
                            break;
                        }
                        case R.id.nav_cart: {
                            selectedFragment = new CartFragment();
                            fragmentTag = "CartFragment";
                            break;
                        }
                        case R.id.nav_bills: {
                            selectedFragment = new BillsFragment();
                            fragmentTag = "BillsFragment";
                            break;
                        }
                    }
                    rateApp(); //to make counts when navigating screens
                    navigateScreens(selectedFragment, fragmentTag, item.getItemId());
                    return true;
                }
            };

    private void navigateScreens(Fragment selectedFragment, String fragmentTag, int itemId) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, selectedFragment, fragmentTag)
                .commit();
        Bundle bundle = new Bundle();
        switch (itemId) {
            case R.id.nav_wallet: {
                bottomNav.getMenu().getItem(0).setChecked(true);
                bundle.putString("feature", "Wallet");
                break;
            }
            case R.id.nav_bank: {
                bottomNav.getMenu().getItem(1).setChecked(true);
                bundle.putString("feature", "Bank");
                break;
            }
            case R.id.nav_cart: {
                bottomNav.getMenu().getItem(2).setChecked(true);
                bundle.putString("feature", "Cart");
                break;
            }
            case R.id.nav_bills: {
                bottomNav.getMenu().getItem(3).setChecked(true);
                bundle.putString("feature", "Bills");
                break;
            }
        }
        firebaseAnalytics.logEvent("used_feature", bundle);
    }

    private void rateApp() {
        boolean alreadyRated = sharedPref.getBoolean("alreadyRated", false);
        int actionCount = sharedPref.getInt("actionCount", 0);
        sharedPref.edit().putInt("actionCount", actionCount + 1).apply();
        if (!alreadyRated & actionCount >= 14) {
            final ReviewManager reviewManager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = reviewManager.requestReviewFlow();
            request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
                @Override
                public void onComplete(@NonNull Task<ReviewInfo> task) {
                    if (task.isSuccessful()) {
                        ReviewInfo reviewInfo = task.getResult();
                        Task<Void> flow = reviewManager.launchReviewFlow(MainActivity.this, reviewInfo);
                        flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                sharedPref.edit().putBoolean("alreadyRated", true).apply();
                            }
                        });
                    }
                }
            });
            request.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.enjoying_the_app)
                            .setMessage(R.string.rate_description)
                            .setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sharedPref.edit().putBoolean("alreadyRated", true).apply();
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.tashila.mywalletfree"));
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sharedPref.edit().putInt("actionCount", 0).apply();
                                }
                            })
                            .setNeutralButton("Never", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sharedPref.edit().putBoolean("alreadyRated", true).apply();
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    sharedPref.edit().putInt("actionCount", 0).apply();
                                }
                            })
                            .show();
                }
            });
        }
    }
}

//TODO: Update what's new (and what's new dialog shared preference key)









