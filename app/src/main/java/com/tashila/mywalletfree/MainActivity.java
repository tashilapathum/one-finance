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
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener {
    private DrawerLayout drawer;
    SharedPreferences sharedPref;
    public static final String TAG = "MainActivity";
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*------------------------------Essential for every activity------------------------------*/
        Toolbar toolbar;
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

        //bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        String homeScreen = sharedPref.getString("homeScreen", "wallet");

        ////STARTUP////
        //when requested after applying settings
        if (sharedPref.getBoolean("reqOpenBank", false)) {
            navigateScreens(new BankFrag(), "BankFrag", R.id.nav_bank);
            sharedPref.edit().putBoolean("reqOpenBank", false).apply();
        }
        //when set as home screen (won't set it back to false)
        else if (homeScreen.equalsIgnoreCase("bank"))
            navigateScreens(new BankFrag(), "BankFrag", R.id.nav_bank);
        else if (homeScreen.equalsIgnoreCase("cart"))
            navigateScreens(new CartFrag(), "CartFrag", R.id.nav_loans);
        else if (homeScreen.equalsIgnoreCase("bills"))
            navigateScreens(new BillsFrag(), "BillsFrag", R.id.nav_bills);
            //when starting the app normally
        else {
            navigateScreens(new WalletFrag(), "WalletFrag", R.id.nav_wallet);
            /*transaction.addToBackStack(null); //for back button press*/
        }

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

        //calculate interests
        new AccountHandler(this).calculateInterests();

        //rate dialog
        boolean alreadyRated = sharedPref.getBoolean("alreadyRated", false);
        int openCount = sharedPref.getInt("openCount", 0);
        sharedPref.edit().putInt("openCount", openCount + 1).apply();
        if (!alreadyRated & openCount >= 4) {
            new AlertDialog.Builder(this)
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
                            sharedPref.edit().putInt("openCount", 0).apply();
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
                            sharedPref.edit().putInt("openCount", 0).apply();
                        }
                    })
                    .show();
        }
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
    }


    //nav drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_recent_trans: {
                Intent intent = new Intent(this, TransHistory.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_reports: {
                Intent intent = new Intent(this, Reports.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_settings: {
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_get_pro: {
                Intent intent = new Intent(this, UpgradeToPro.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_about: {
                DialogAbout dialogAbout = new DialogAbout();
                dialogAbout.show(getSupportFragmentManager(), "about dialog");
                break;
            }
        }
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
                                finishAndRemoveTask();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            } else finishAndRemoveTask();
        }
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
                            selectedFragment = new WalletFrag();
                            fragmentTag = "WalletFrag";
                            break;
                        }
                        case R.id.nav_bank: {
                            selectedFragment = new BankFrag();
                            fragmentTag = "BankFrag";
                            break;
                        }
                        case R.id.nav_loans: {
                            selectedFragment = new CartFrag();
                            fragmentTag = "CartFrag";
                            break;
                        }
                        case R.id.nav_bills: {
                            selectedFragment = new BillsFrag();
                            fragmentTag = "BillsFrag";
                            break;
                        }
                    }
                    navigateScreens(selectedFragment, fragmentTag, item.getItemId());
                    return true;
                }
            };

    private void navigateScreens(Fragment selectedFragment, String fragmentTag, int itemId) {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, selectedFragment, fragmentTag).commit();
        switch (itemId) {
            case R.id.nav_wallet: {
                bottomNav.getMenu().getItem(0).setChecked(true);
                break;
            }
            case R.id.nav_bank: {
                bottomNav.getMenu().getItem(1).setChecked(true);
                break;
            }
            case R.id.nav_loans: {
                bottomNav.getMenu().getItem(2).setChecked(true);
                break;
            }
            case R.id.nav_bills: {
                bottomNav.getMenu().getItem(3).setChecked(true);
                break;
            }
        }
    }

    public void rateApp(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.tashila.mywalletfree"));
        startActivity(intent);
    }

    public void moreApps(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Tashila+Pathum"));
        startActivity(intent);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        LocalDateTime date = LocalDateTime.of(year, month, dayOfMonth,
                LocalDateTime.now().getHour(), LocalDateTime.now().getMinute());
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        String preDate = date.format(formatter);
        sharedPref.edit().putString("preDate", preDate).apply();

        WalletFrag walletFrag = (WalletFrag) getSupportFragmentManager().findFragmentByTag("WalletFrag");
        walletFrag.continueLongClickProcess();
    }
}

//TODO: add rate the app dialog
//TODO: change edit quick list mechanism!
//TODO: add starting balance to initial setup
//TODO: customize drawer items













