package com.tantalum.onefinance.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;

import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tantalum.onefinance.AboutActivity;
import com.tantalum.onefinance.AlertReceiver;
import com.tantalum.onefinance.categories.CategoriesActivity;
import com.tantalum.onefinance.quicklist.QuickListActivity;
import com.tantalum.onefinance.EnterPIN;
import com.tantalum.onefinance.MainActivity;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.reports.ReportsActivity;
import com.tantalum.onefinance.transactions.TransactionsActivity;
import com.tantalum.onefinance.pro.UpgradeToProActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "SettingsActivity";
    SharedPreferences sharedPref;
    private DrawerLayout drawer;
    String timeString;
    private MaterialSwitch exitCheckBox;
    private MaterialSwitch negativeCheckBox;
    private FirebaseAnalytics firebaseAnalytics;
    private NavigationView navigationView;
    private boolean isMyWalletPro;
    private final String HOME_OPTION_WALLET = "Wallet";
    private final String HOME_OPTION_BANK = "Bank";
    private final String HOME_OPTION_TRANSACTIONS = "Transactions";
    private final String HOME_OPTION_INVESTMENTS = "Investments";
    private final String HOME_OPTION_TOOLS = "Tools";

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
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);
            View layout = findViewById(R.id.drawer_layout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
            toolbar = findViewById(R.id.toolbar);
            toolbar.setBackground(getDrawable(R.color.colorToolbarDark));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);
            toolbar = findViewById(R.id.toolbar);
        }

        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (sharedPref.getBoolean("MyWalletPro", false)) {
            View navHeader = navigationView.getHeaderView(0);
            TextView tvAppName = navHeader.findViewById(R.id.appName);
            tvAppName.setText(R.string.my_wallet_pro);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /*----------------------------------------------------------------------------------------*/
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        isMyWalletPro = sharedPref.getBoolean("MyWalletPro", false);
        ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);

        if (isMyWalletPro) hideProLabels(viewGroup);

        //exit confirm setting
        exitCheckBox = findViewById(R.id.exitCheck);
        exitCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked())
                    sharedPref.edit().putBoolean("exitConfirmation", true).apply();
                else
                    sharedPref.edit().putBoolean("exitConfirmation", false).apply();
            }
        });
        boolean confirmExitEnabled = sharedPref.getBoolean("exitConfirmation", false);
        if (confirmExitEnabled) exitCheckBox.setChecked(true);

        //negative setting
        negativeCheckBox = findViewById(R.id.negativeCheck);
        negativeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked())
                    sharedPref.edit().putBoolean("negativeEnabled", true).apply();
                else
                    sharedPref.edit().putBoolean("negativeEnabled", false).apply();
            }
        });
        boolean negativeEnabled = sharedPref.getBoolean("negativeEnabled", false);
        if (negativeEnabled) negativeCheckBox.setChecked(true);
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

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_settings);
        if (sharedPref.getBoolean("exit", false)) {
            finishAndRemoveTask();
        }
        sharedPref.edit().putBoolean("modifiedSettings", true).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.edit().putBoolean("exit", false).apply();
    }

    //nav drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_home)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("showPinScreen", false);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_recent_trans: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_recent_trans)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, TransactionsActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_categories: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_categories)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, CategoriesActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_reports: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_reports)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, ReportsActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_settings: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_settings)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_pro: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_pro)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, UpgradeToProActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_share: {
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
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_about)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, AboutActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_exit: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_exit)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    sharedPref.edit().putBoolean("exit", true).apply();
                    finishAndRemoveTask();
                }
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
            super.onBackPressed();
        }
    }

    private void hideProLabels(ViewGroup viewGroup) {
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup)
                hideProLabels((ViewGroup) view);
            else if (view instanceof TextView)
                if (((TextView) view).getText().equals("Pro"))
                    view.setVisibility(View.INVISIBLE);
        }
    }

    public void editQuickList(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "edit_quick_list");
        firebaseAnalytics.logEvent("used_setting", bundle);
        Intent intent = new Intent(SettingsActivity.this, QuickListActivity.class);
        startActivity(intent);
    }

    public void selectLanguage(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "select_lang");
        firebaseAnalytics.logEvent("used_setting", bundle);
        DialogLanguage dialogLanguage = new DialogLanguage();
        dialogLanguage.show(getSupportFragmentManager(), "language dialog");
    }

    public void selectCurrency(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "select_currency");
        firebaseAnalytics.logEvent("used_setting", bundle);
        DialogCurrency dialogCurrency = new DialogCurrency();
        dialogCurrency.show(getSupportFragmentManager(), "currency dialog");
    }

    public void editBudget(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "edit_budget");
        firebaseAnalytics.logEvent("used_setting", bundle);
        DialogBudget dialogBudget = new DialogBudget();
        dialogBudget.show(getSupportFragmentManager(), "budget dialog");
    }

    public void exitConfirm(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "exit_confirm_checkbox");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (!exitCheckBox.isChecked())
            exitCheckBox.setChecked(true);
        else
            exitCheckBox.setChecked(false);
    }

    /*-------------------------Notification time--------------------------*/

    public void editNotifyTime(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "edit_notify_time");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (isMyWalletPro) showTimePicker();
        else purchaseProForThis();
    }

    private void showTimePicker() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTitleText("Select reminder time")
                .build();
        timePicker.addOnPositiveButtonClickListener(dialog -> {
            int hourOfDay = timePicker.getHour();
            int minute = timePicker.getMinute();

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);

            saveTime(c);
            startAlarm(c);
        });
        timePicker.show(getSupportFragmentManager(), "time picker");
    }

    public void saveTime(Calendar c) {
        timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        sharedPref.edit().putString("timeString", timeString).apply();
    }

    public void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        int reqCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            pendingIntent = PendingIntent.getBroadcast(this, reqCode, intent, PendingIntent.FLAG_IMMUTABLE);
        else
            pendingIntent = PendingIntent.getBroadcast(this, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                c.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );

        Toast.makeText(this, getString(R.string.notification_set_at) + timeString + getString(R.string.everydayy), Toast.LENGTH_LONG).show();
    }
    /*------------------end of notification time------------------*/

    public void setTheme(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "set_theme");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (isMyWalletPro) {
            DialogTheme dialogTheme = new DialogTheme();
            dialogTheme.show(getSupportFragmentManager(), "theme dialog");
        } else purchaseProForThis();
    }

    //choose home screen
    public void chooseHome(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "set_home");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (isMyWalletPro) {
            final int[] home = {0};
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.set_home_screen)
                    .setSingleChoiceItems(new CharSequence[]{
                                    getString(R.string.wallet),
                                    getString(R.string.bank),
                                    getString(R.string.recent_transactions),
                                    getString(R.string.investments),
                                    getString(R.string.tools)
                            },
                            getCheckedHomeIndex(),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int checkedIndex) {
                                    home[0] = checkedIndex;
                                }
                            }
                    )
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveCheckedHome(home[0]);
                        }
                    })
                    .show();
        } else purchaseProForThis();
    }

    private int getCheckedHomeIndex() {
        String homeScreen = sharedPref.getString("homeScreen", "Wallet");
        int id = 0;
        switch (homeScreen) {
            case HOME_OPTION_WALLET: {
                id = 0;
                break;
            }
            case HOME_OPTION_BANK: {
                id = 1;
                break;
            }
            case HOME_OPTION_TRANSACTIONS: {
                id = 2;
                break;
            }
            case HOME_OPTION_INVESTMENTS: {
                id = 3;
                break;
            }
            case HOME_OPTION_TOOLS: {
                id = 4;
                break;
            }
        }
        return id;
    }

    private void saveCheckedHome(int checkedIndex) {
        String home = null;
        switch (checkedIndex) {
            case 0: {
                home = HOME_OPTION_WALLET;
                break;
            }
            case 1: {
                home = HOME_OPTION_BANK;
                break;
            }
            case 2: {
                home = HOME_OPTION_TRANSACTIONS;
                break;
            }
            case 3: {
                home = HOME_OPTION_INVESTMENTS;
                break;
            }
            case 4: {
                home = HOME_OPTION_TOOLS;
                break;
            }
        }

        sharedPref.edit().putString("homeScreen", home).apply();
        Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
    }

    public void negativeBalance(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "negative_enabled_checkbox");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (!negativeCheckBox.isChecked())
            negativeCheckBox.setChecked(true);
        else
            negativeCheckBox.setChecked(false);
    }

    public void restoreDefaults(View view) {
        /*retain the following data and clear everything else.
            - balance
            - MyWalletPro
            - haveAccounts
            - alreadyDidInitSetup
          */
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.reset_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get needed data
                        String balance = sharedPref.getString("balance", "0.00");
                        boolean alreadyInit = sharedPref.getBoolean("alreadyDidInitSetup", false);
                        boolean hasAccounts = sharedPref.getBoolean("haveAccounts", false);
                        boolean isPro = sharedPref.getBoolean("MyWalletPro", false);

                        //clear
                        sharedPref.edit().clear().apply();

                        //restore
                        sharedPref.edit().putString("balance", balance).apply();
                        sharedPref.edit().putBoolean("alreadyDidInitSetup", alreadyInit).apply();
                        sharedPref.edit().putBoolean("haveAccounts", hasAccounts).apply();
                        sharedPref.edit().putBoolean("MyWalletPro", isPro).apply();

                        Toast.makeText(SettingsActivity.this, getString(R.string.completed_success), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }


    public void notifications(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "notifications_option");
        firebaseAnalytics.logEvent("used_setting", bundle);

        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        //for Android 5-7
        intent.putExtra("app_package", getPackageName());
        intent.putExtra("app_uid", getApplicationInfo().uid);
        //for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        startActivity(intent);
    }

    public void pin(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "pin");
        firebaseAnalytics.logEvent("used_setting", bundle);

        if (isMyWalletPro) {
            if (sharedPref.getBoolean("pinEnabled", false)) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.choose_pin_action)
                        .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(SettingsActivity.this, EnterPIN.class);
                                intent.putExtra("validate", true);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sharedPref.edit().putBoolean("pinEnabled", false).apply();
                                Toast.makeText(SettingsActivity.this, getString(R.string.removed), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            } else {
                Intent intent = new Intent(this, EnterPIN.class);
                intent.putExtra("newPin", true);
                startActivity(intent);
            }
        } else
            purchaseProForThis();
    }

    public void purchaseProForThis() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.pro_feature)
                .setMessage(R.string.buy_pro_for_this)
                .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(SettingsActivity.this, UpgradeToProActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
