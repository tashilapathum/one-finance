package com.tantalum.onefinance;

import static com.tantalum.onefinance.UpgradeHandler.ONE_FINANCE_PRO;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tantalum.onefinance.bank.BankFragmentNEW;
import com.tantalum.onefinance.investments.InvestmentsFragment;
import com.tantalum.onefinance.reports.ReportsActivity;
import com.tantalum.onefinance.settings.SettingsActivity;
import com.tantalum.onefinance.transactions.TransactionsActivity;
import com.tantalum.onefinance.transactions.TransactionsFragment;
import com.tantalum.onefinance.wallet.WalletFragmentNEW;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "MainActivity";
    private DrawerLayout drawer;
    private SharedPreferences sharedPref;
    private BottomNavigationView bottomNav;
    private FirebaseAnalytics firebaseAnalytics;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        //to exit the app
        if (getIntent().getBooleanExtra("shouldExit", false))
            finish();

        //PIN
        if (sharedPref.getBoolean("pinEnabled", false)) {
            if (getIntent().getBooleanExtra("showPinScreen", true)) {
                boolean isPinCompleted = getIntent().getBooleanExtra("pinCompleted", false);
                if (!isPinCompleted) {
                    startActivity(new Intent(this, EnterPIN.class));
                    finish();
                }
            }
        }

        /*------------------------------Essential for every activity------------------------------*/
        MaterialToolbar toolbar;

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

        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleCentered(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /*----------------------------------------------------------------------------------------*/
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setOnItemReselectedListener(navReListener);
        String homeScreen = sharedPref.getString("homeScreen", "wallet");

        ////STARTUP////
        //when requested after applying settings
        if (sharedPref.getBoolean("reqOpenBank", false)) {
            navigateScreens(new BankFragmentNEW(), "BankFragment", R.id.nav_bank);
            sharedPref.edit().putBoolean("reqOpenBank", false).apply();
        }
        //when set as home screen (won't set it back to false)
        else if (homeScreen.equalsIgnoreCase("bank"))
            navigateScreens(new BankFragmentNEW(), "BankFragment", R.id.nav_bank);
        else if (homeScreen.equalsIgnoreCase("transactions"))
            navigateScreens(new TransactionsFragment(), "BillsFragment", R.id.nav_trans);
        else if (homeScreen.equalsIgnoreCase("investments"))
            navigateScreens(new InvestmentsFragment(), "BillsFragment", R.id.nav_invest);
        else if (homeScreen.equalsIgnoreCase("tools"))
            navigateScreens(new ToolsFragment(), "BillsFragment", R.id.nav_tools);
            //when starting the app normally
        else
            navigateScreens(new WalletFragmentNEW(), "WalletFragment", R.id.nav_wallet);

        //set notification
        boolean isNotificationSet = sharedPref.getBoolean("isNotificationSet", false);
        if (!isNotificationSet) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 20); //20 means default is at 8
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent notifyIntent = new Intent(this, AlertReceiver.class);
            PendingIntent pendingIntent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                pendingIntent = PendingIntent.getBroadcast(this, 1, notifyIntent, PendingIntent.FLAG_IMMUTABLE);
            else
                pendingIntent = PendingIntent.getBroadcast(this, 1, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            sharedPref.edit().putBoolean("isNotificationSet", true).apply();
        }

        sharedPref.edit().putBoolean("MyWalletPro", true).apply();
        if (getPackageName().contains("debug"))
            sharedPref.edit().putBoolean(ONE_FINANCE_PRO, true).apply();

        //updates
        UpdateManager updateManager = new UpdateManager(this);
        updateManager.checkForUpdates();
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
        Intent intent = new Intent(this, InitialSetupActivity.class);
        boolean alreadyDid = sharedPref.getBoolean("alreadyDidInitSetup", false);
        if (!alreadyDid) startActivity(intent);
        else {
            //what's new
            if (!sharedPref.getBoolean("whatsNewShown" + getString(R.string.version), false)) {
                DialogWhatsNew dialogWhatsNew = new DialogWhatsNew();
                dialogWhatsNew.show(getSupportFragmentManager(), "whats new dialog");
                sharedPref.edit().putBoolean("whatsNewShown" + getString(R.string.version), true).apply();
            }
        }
        rateApp(); //to make counts
    }

    //nav drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.nav_home: {
                bundle.putString("feature", "home");
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_home)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_recent_trans: {
                bundle.putString("feature", "transactions");
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_recent_trans)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, TransactionsActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_categories: {
                bundle.putString("feature", "categories");
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_categories)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, CategoriesActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_reports: {
                bundle.putString("feature", "reports");
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_reports)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, ReportsActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_settings: {
                bundle.putString("feature", "settings");
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_settings)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_pro: {
                bundle.putString("feature", "pro");
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_pro)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, UpgradeToProActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_share: {
                bundle.putString("feature", "share");
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_share)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Share One Finance");
                    intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.tantalum.onefinance");
                    startActivity(Intent.createChooser(intent, "Share One Finance"));
                }
                break;
            }
            case R.id.nav_about: {
                bundle.putString("feature", "about");
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_about)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, AboutActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_exit: {
                bundle.putString("feature", "exit");
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_exit)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    sharedPref.edit().putBoolean("exit", true).apply();
                    finishAndRemoveTask();
                }
                break;
            }
        }
        firebaseAnalytics.logEvent("used_feature", bundle);
        return true;
    }

    @Override
    public void onBackPressed() {
        //to check for opened fragments
        FragmentManager childFm = null;
        FragmentManager fm = getSupportFragmentManager();
        for (Fragment frag : fm.getFragments())
            if (frag.isVisible())
                childFm = frag.getChildFragmentManager();

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);

        else if (childFm != null) {
            if (childFm.getBackStackEntryCount() > 0)
                childFm.popBackStack();
            else processExit();
        } else processExit();
    }

    private void processExit() {
        boolean confirmExit = sharedPref.getBoolean("exitConfirmation", false);
        if (confirmExit) {
            new MaterialAlertDialogBuilder(this)
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

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_home);
        if (sharedPref.getBoolean("exit", false)) {
            finishAndRemoveTask();
        }
        //ads
        else {
            if (adsEnabled()) {
                //TODO: show ads
            }
        }
    }

    private boolean adsEnabled() {
        int today = new DateTimeHandler().getDayOfYear();
        int adsDeadline = sharedPref.getInt("adsDeadline", 0);
        boolean adsRemoved = false;
        if (adsDeadline != 0)
            adsRemoved = today <= adsDeadline;

        return !sharedPref.getBoolean("MyWalletPro", false) && !adsRemoved;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.edit().putBoolean("exit", false).apply();
        sharedPref.edit().putBoolean("chooseAccFromWallet", false).apply();
        sharedPref.edit().putBoolean("transferFromBank", false).apply();
    }

    //bottom nav
    private NavigationBarView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    String fragmentTag = null;
                    switch (item.getItemId()) {
                        case R.id.nav_wallet: {
                            selectedFragment = new WalletFragmentNEW();
                            fragmentTag = "WalletFragment";
                            break;
                        }
                        case R.id.nav_bank: {
                            selectedFragment = new BankFragmentNEW();
                            fragmentTag = "BankFragment";
                            break;
                        }
                        case R.id.nav_trans: {
                            selectedFragment = new TransactionsFragment();
                            fragmentTag = "TransactionsFragment";
                            break;
                        }
                        case R.id.nav_invest: {
                            selectedFragment = new InvestmentsFragment();
                            fragmentTag = "InvestmentsFragment";
                            break;
                        }
                        case R.id.nav_tools: {
                            selectedFragment = new ToolsFragment();
                            fragmentTag = "ToolsFragment";
                            break;
                        }
                    }
                    navigateScreens(selectedFragment, fragmentTag, item.getItemId());
                    return true;
                }
            };

    private NavigationBarView.OnItemReselectedListener navReListener = new NavigationBarView.OnItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {

        }
    };

    private void navigateScreens(Fragment selectedFragment, String fragmentTag, int itemId) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, selectedFragment, fragmentTag)
                .commit();
        switch (itemId) {
            case R.id.nav_wallet: {
                bottomNav.getMenu().getItem(0).setChecked(true);
                break;
            }
            case R.id.nav_bank: {
                bottomNav.getMenu().getItem(1).setChecked(true);
                break;
            }
            case R.id.nav_trans: {
                bottomNav.getMenu().getItem(2).setChecked(true);
                break;
            }
            case R.id.nav_invest: {
                bottomNav.getMenu().getItem(3).setChecked(true);
                break;
            }
            case R.id.nav_tools: {
                bottomNav.getMenu().getItem(4).setChecked(true);
                break;
            }
        }
    }

    private void rateApp() {
        boolean alreadyRated = sharedPref.getBoolean("alreadyRated", false);
        int actionCount = sharedPref.getInt("actionCount", 0);
        sharedPref.edit().putInt("actionCount", actionCount + 1).apply();
        if (!alreadyRated & (actionCount % 14 == 0)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.enjoying_the_app)
                    .setMessage(R.string.rate_description)
                    .setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sharedPref.edit().putBoolean("alreadyRated", true).apply();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.tantalum.onefinance"));
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
    }

    public void removeAds(View view) {
        Intent intent = new Intent(this, UpgradeToProActivity.class);
        intent.putExtra("scroll", true);
        startActivity(intent);
    }
}









