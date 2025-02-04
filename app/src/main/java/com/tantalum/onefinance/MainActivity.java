package com.tantalum.onefinance;

import static com.tantalum.onefinance.Constants.SP_HOME_SCREEN;
import static com.tantalum.onefinance.pro.UpgradeHandler.ONE_FINANCE_PRO;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tantalum.onefinance.accounts.AccountManager;
import com.tantalum.onefinance.bank.BankFragment;
import com.tantalum.onefinance.categories.CategoriesActivity;
import com.tantalum.onefinance.investments.InvestmentViewFragment;
import com.tantalum.onefinance.investments.InvestmentsFragment;
import com.tantalum.onefinance.pro.UpgradeToProActivity;
import com.tantalum.onefinance.quicklist.QuickListActivity;
import com.tantalum.onefinance.reports.ReportsActivity;
import com.tantalum.onefinance.settings.SettingsActivity;
import com.tantalum.onefinance.transactions.TransactionsFragment;
import com.tantalum.onefinance.wallet.WalletFragment;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "MainActivity";
    private DrawerLayout drawer;
    private SharedPreferences sharedPref;
    private BottomNavigationView bottomNav;
    private FirebaseAnalytics firebaseAnalytics;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        //to exit the app
        if (getIntent().getBooleanExtra("shouldExit", false))
            finish();

        //PIN
        if (sharedPref.getBoolean("pinEnabled", false)) {
            if (getIntent().getBooleanExtra("showPinScreen", true)) {
                boolean isPinCompleted = getIntent().getBooleanExtra("pinCompleted", false);
                if (!isPinCompleted) {
                    startActivity(new Intent(this, EnterPINActivity.class));
                    finish();
                }
            }
        }

        /*------------------------------Essential for every activity------------------------------*/

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
        String theme = sharedPref.getString("theme", "");
        if (theme.equalsIgnoreCase("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme.equalsIgnoreCase("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.AppTheme);
        } else {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    sharedPref.edit().putString("theme", "dark").apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;

                case Configuration.UI_MODE_NIGHT_NO:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    sharedPref.edit().putString("theme", "light").apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    setTheme(R.style.AppTheme);
                    break;
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        drawer = findViewById(R.id.drawer_layout);
        if (theme.equalsIgnoreCase("dark"))
            drawer.setBackground(AppCompatResources.getDrawable(this, R.drawable.background_gradient_dark));
        else
            drawer.setBackground(AppCompatResources.getDrawable(this, R.drawable.background_gradient_light));

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawer,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        /*----------------------------------------------------------------------------------------*/
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setOnItemReselectedListener(navReListener);
        String homeScreen = sharedPref.getString(SP_HOME_SCREEN, "wallet");

        ////STARTUP////
        //when requested after applying settings
        if (sharedPref.getBoolean("reqOpenBank", false)) {
            navigateScreens(new BankFragment(), "BankFragment", R.id.nav_bank);
            sharedPref.edit().putBoolean("reqOpenBank", false).apply();
        }
        else { // when set as home screen (won't set it back to false)
            switch (homeScreen.toLowerCase()) {
                case "bank":
                    navigateScreens(new BankFragment(), "BankFragment", R.id.nav_bank);
                    break;
                case "transactions":
                    navigateScreens(new TransactionsFragment(), "BillsFragment", R.id.nav_trans);
                    break;
                case "investments":
                    navigateScreens(new InvestmentsFragment(), "BillsFragment", R.id.nav_invest);
                    break;
                case "tools":
                    navigateScreens(new ToolsFragment(), "BillsFragment", R.id.nav_tools);
                    break;
                default:
                    navigateScreens(new WalletFragment(), "WalletFragment", R.id.nav_wallet);
                    break;
            }
        }

        sharedPref.edit().putBoolean("MyWalletPro", true).apply(); //DO NOT CHANGE
        if (getPackageName().contains("debug"))
            sharedPref.edit().putBoolean(ONE_FINANCE_PRO, true).apply();

        //updates
        UpdateManager updateManager = new UpdateManager(this);
        updateManager.checkForUpdates();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle the drawer toggle click event
        if (toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
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
        if (!alreadyDid)
            startActivity(intent);
        else {
            //what's new
            if (!sharedPref.getBoolean("whatsNewShown" + getString(R.string.version), false)) {
                DialogWhatsNew dialogWhatsNew = new DialogWhatsNew();
                dialogWhatsNew.show(getSupportFragmentManager(), "whats new dialog");
                sharedPref.edit().putBoolean("whatsNewShown" + getString(R.string.version), true).apply();
            }
            setNotification();
        }
        rateApp(); //to make counts
    }

    //nav drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.nav_home:
                firebaseAnalytics.logEvent("drawer_home", null);
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.nav_categories:
                firebaseAnalytics.logEvent("drawer_categories", null);
                intent = new Intent(this, CategoriesActivity.class);
                break;
            case R.id.nav_reports:
                firebaseAnalytics.logEvent("drawer_reports", null);
                intent = new Intent(this, ReportsActivity.class);
                break;
            case R.id.nav_quick_list:
                firebaseAnalytics.logEvent("drawer_quick_list", null);
                intent = new Intent(this, QuickListActivity.class);
                break;
            case R.id.nav_accounts:
                firebaseAnalytics.logEvent("drawer_accounts", null);
                intent = new Intent(this, AccountManager.class);
                break;
            case R.id.nav_settings:
                firebaseAnalytics.logEvent("drawer_settings", null);
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.nav_pro:
                firebaseAnalytics.logEvent("drawer_pro", null);
                intent = new Intent(this, UpgradeToProActivity.class);
                break;
            case R.id.nav_share:
                firebaseAnalytics.logEvent("drawer_share", null);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share One Finance");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.tantalum.onefinance");
                startActivity(Intent.createChooser(shareIntent, "Share One Finance"));
                break;
            case R.id.nav_about:
                firebaseAnalytics.logEvent("drawer_about", null);
                intent = new Intent(this, AboutActivity.class);
                break;
            case R.id.nav_exit:
                firebaseAnalytics.logEvent("drawer_exit", null);
                sharedPref.edit().putBoolean("exit", true).apply();
                finishAndRemoveTask();
                break;
        }

        if (intent != null && navigationView.getCheckedItem().getItemId() != item.getItemId())
            startActivity(intent);
        else
            drawer.closeDrawer(GravityCompat.START);
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

        if (fm.getBackStackEntryCount() > 0)
            fm.popBackStack();

        else if (childFm != null)
            if (childFm.getBackStackEntryCount() > 0)
                childFm.popBackStack();
            else processExit();
        else processExit();
    }

    private void setNotification() {
        boolean isNotificationSet = sharedPref.getBoolean("isNotificationSet", false);
        if (!isNotificationSet) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 20); //20 means default is at 8
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent notifyIntent = new Intent(this, AlertReceiver.class);
            PendingIntent pendingIntent = null;
            int requestCode = 1;
            pendingIntent = PendingIntent.getBroadcast(this, requestCode, notifyIntent, PendingIntent.FLAG_IMMUTABLE);

            alarmManager.cancel(pendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            sharedPref.edit().putBoolean("isNotificationSet", true).apply();
        }
    }

    private void processExit() {
        boolean confirmExit = sharedPref.getBoolean("exitConfirmation", false);
        if (confirmExit) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.exit_confirm)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        sharedPref.edit().putBoolean("exit", true).apply();
                        finishAndRemoveTask();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        } else finishAndRemoveTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_home);
        if (sharedPref.getBoolean("exit", false))
            finishAndRemoveTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.edit().putBoolean("exit", false).apply();
        sharedPref.edit().putBoolean("chooseAccFromWallet", false).apply();
        sharedPref.edit().putBoolean("transferFromBank", false).apply();
    }

    //bottom nav
    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                String fragmentTag = null;
                switch (item.getItemId()) {
                    case R.id.nav_wallet: {
                        selectedFragment = new WalletFragment();
                        fragmentTag = "WalletFragment";
                        getSupportActionBar().setSubtitle(R.string.wallet);
                        break;
                    }
                    case R.id.nav_bank: {
                        selectedFragment = new BankFragment();
                        fragmentTag = "BankFragment";
                        getSupportActionBar().setSubtitle(R.string.bank);
                        break;
                    }
                    case R.id.nav_trans: {
                        selectedFragment = new TransactionsFragment();
                        fragmentTag = "TransactionsFragment";
                        getSupportActionBar().setSubtitle(R.string.transactions);
                        break;
                    }
                    case R.id.nav_invest: {
                        selectedFragment = new InvestmentsFragment();
                        fragmentTag = "InvestmentsFragment";
                        setInvestmentsNavigator((InvestmentsFragment) selectedFragment);
                        getSupportActionBar().setSubtitle(R.string.investments);
                        break;
                    }
                    case R.id.nav_tools: {
                        selectedFragment = new ToolsFragment();
                        fragmentTag = "ToolsFragment";
                        setToolsNavigator((ToolsFragment) selectedFragment);
                        getSupportActionBar().setSubtitle(R.string.tools);
                        break;
                    }
                }
                navigateScreens(selectedFragment, fragmentTag, item.getItemId());
                return true;
            };

    private final NavigationBarView.OnItemReselectedListener navReListener = item -> {
        if (item.getItemId() == R.id.nav_tools) {
            ToolsFragment selectedFragment = new ToolsFragment();
            String fragmentTag = "ToolsFragment";
            setToolsNavigator(selectedFragment);
            navigateScreens(selectedFragment, fragmentTag, item.getItemId());
        }
    };

    private void setToolsNavigator(ToolsFragment toolsFragment) {
        toolsFragment.setToolSelectedListener((fragment1, fragmentTag1) ->
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment1, fragmentTag1)
                        .addToBackStack(fragmentTag1)
                        .commit()
        );
    }

    private void setInvestmentsNavigator(InvestmentsFragment investmentsFragment) {
        investmentsFragment.setInvestmentClickListener(investment ->
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new InvestmentViewFragment(investment))
                        .addToBackStack(null)
                        .commit()
        );
    }

    private void navigateScreens(Fragment selectedFragment, String fragmentTag, int itemId) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (selectedFragment instanceof ToolsFragment)
            setToolsNavigator((ToolsFragment) selectedFragment);
        else if (selectedFragment instanceof InvestmentsFragment)
            setInvestmentsNavigator((InvestmentsFragment) selectedFragment);

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
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle(R.string.enjoying_the_app)
                    .setMessage(R.string.rate_description)
                    .setPositiveButton("Rate", (dialogInterface, i) -> {
                        sharedPref.edit().putBoolean("alreadyRated", true).apply();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.tantalum.onefinance"));
                        startActivity(intent);
                    })
                    .setNegativeButton("Later", (dialogInterface, i) -> sharedPref.edit().putInt("actionCount", 1).apply())
                    .setNeutralButton("Never", (dialogInterface, i) -> sharedPref.edit().putBoolean("alreadyRated", true).apply())
                    .setOnDismissListener(dialogInterface -> sharedPref.edit().putInt("actionCount", 0).apply())
                    .show();
        }
    }

    public void removeAds(View view) {
        Intent intent = new Intent(this, UpgradeToProActivity.class);
        intent.putExtra("scroll", true);
        startActivity(intent);
    }
}









