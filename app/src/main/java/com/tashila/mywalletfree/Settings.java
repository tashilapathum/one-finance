package com.tashila.mywalletfree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;

import androidx.appcompat.app.AlertDialog;

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

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.shreyaspatil.material.navigationview.MaterialNavigationView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Settings extends AppCompatActivity
        implements MaterialNavigationView.OnNavigationItemSelectedListener, TimePickerDialog.OnTimeSetListener {
    public static final String TAG = "Settings";
    SharedPreferences sharedPref;
    private DrawerLayout drawer;
    String timeString;
    private MaterialCheckBox exitCheckBox;
    private MaterialCheckBox suggestCheckBox;
    private MaterialCheckBox undoCheckBox;
    private MaterialCheckBox negativeCheckBox;
    private MaterialCheckBox qlShortcutCheckBox;
    private MaterialCheckBox tapHideCheckBox;
    private MaterialCheckBox refreshCheckBox;
    private FirebaseAnalytics firebaseAnalytics;
    private MaterialNavigationView navigationView;
    private boolean isMyWalletPro;

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

        //suggestions setting
        suggestCheckBox = findViewById(R.id.suggestCheck);
        suggestCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked())
                    sharedPref.edit().putBoolean("showSuggestions", true).apply();
                else
                    sharedPref.edit().putBoolean("showSuggestions", false).apply();
            }
        });
        boolean suggestEnabled = sharedPref.getBoolean("showSuggestions", false);
        if (suggestEnabled) suggestCheckBox.setChecked(true);

        //undo action setting
        undoCheckBox = findViewById(R.id.undoCheck);
        undoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked())
                    sharedPref.edit().putBoolean("undoActionEnabled", true).apply();
                else
                    sharedPref.edit().putBoolean("undoActionEnabled", false).apply();
            }
        });
        boolean undoActionEnabled = sharedPref.getBoolean("undoActionEnabled", true);
        if (undoActionEnabled) undoCheckBox.setChecked(true);

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

        //quick list shortcut setting
        qlShortcutCheckBox = findViewById(R.id.qlShortcutCheck);
        qlShortcutCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked())
                    sharedPref.edit().putBoolean("qlShortcutEnabled", true).apply();
                else
                    sharedPref.edit().putBoolean("qlShortcutEnabled", false).apply();
            }
        });
        boolean qlShortcutEnabled = sharedPref.getBoolean("qlShortcutEnabled", false);
        if (qlShortcutEnabled) qlShortcutCheckBox.setChecked(true);

        //quick list shortcut setting
        tapHideCheckBox = findViewById(R.id.tapHideCheck);
        tapHideCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked())
                    sharedPref.edit().putBoolean("tapToHideEnabled", true).apply();
                else
                    sharedPref.edit().putBoolean("tapToHideEnabled", false).apply();
            }
        });
        boolean tapToHideEnabled = sharedPref.getBoolean("tapToHideEnabled", false);
        if (tapToHideEnabled) tapHideCheckBox.setChecked(true);

        //auto refresh setting
        refreshCheckBox = findViewById(R.id.refreshCheck);
        refreshCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked())
                    sharedPref.edit().putBoolean("autoRefreshEnabled", true).apply();
                else
                    sharedPref.edit().putBoolean("autoRefreshEnabled", false).apply();
            }
        });
        boolean autoRefreshEnabled = sharedPref.getBoolean("autoRefreshEnabled", false);
        if (autoRefreshEnabled) refreshCheckBox.setChecked(true);
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
                    Intent intent = new Intent(this, TransactionHistory.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_reports: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_reports)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, Reports.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_accounts: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_accounts)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, AccountManager.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_settings: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_settings)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, Settings.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_get_pro: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_get_pro)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, UpgradeToPro.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_about: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_about)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, About.class);
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
        Intent intent = new Intent(Settings.this, EditQuickList.class);
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

    public void suggestions(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "suggestions_checkbox");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (!suggestCheckBox.isChecked())
            suggestCheckBox.setChecked(true);
        else
            suggestCheckBox.setChecked(false);
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
        if (isMyWalletPro) {
            DialogFragment timePicker = new TimePickerFrag();
            timePicker.show(getSupportFragmentManager(), "time picker");
        } else purchaseProForThis();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        saveTime(c);
        startAlarm(c);
    }

    public void saveTime(Calendar c) {
        timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        sharedPref.edit().putString("timeString", timeString).apply();
    }

    public void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        int reqCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

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

    public void chooseHome(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "set_home");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (isMyWalletPro) {
            DialogChooseHome dialogChooseHome = new DialogChooseHome();
            dialogChooseHome.show(getSupportFragmentManager(), "choose home dialog");
        } else purchaseProForThis();
    }

    public void walletContent(View view) {
        DialogWalletContent dialogWalletContent = new DialogWalletContent();
        dialogWalletContent.show(getSupportFragmentManager(), "wallet content dialog");
    }

    public void inputMode(View view) {
        DialogInputMode dialogInputMode = new DialogInputMode();
        dialogInputMode.show(getSupportFragmentManager(), "input mode dialog");
    }

    public void undoAction(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "undo_action_checkbox");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (!undoCheckBox.isChecked())
            undoCheckBox.setChecked(true);
        else
            undoCheckBox.setChecked(false);
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

                        Toast.makeText(Settings.this, getString(R.string.completed_success), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.this, MainActivity.class));
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    public void qlShortcut(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "ql_shortcut_checkbox");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (!qlShortcutCheckBox.isChecked())
            qlShortcutCheckBox.setChecked(true);
        else
            qlShortcutCheckBox.setChecked(false);
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

    public void buttonType(View view) {
        if (isMyWalletPro) {
            DialogButtonType dialogButtonType = new DialogButtonType();
            dialogButtonType.show(getSupportFragmentManager(), "button type dialog");
        } else purchaseProForThis();
    }

    public void tapToHide(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "tap_hide_checkbox");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (!tapHideCheckBox.isChecked())
            tapHideCheckBox.setChecked(true);
        else
            tapHideCheckBox.setChecked(false);
    }

    public void autoRefresh(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "auto_refresh_checkbox");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (!refreshCheckBox.isChecked())
            refreshCheckBox.setChecked(true);
        else
            refreshCheckBox.setChecked(false);
    }

    public void pin(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "pin");
        firebaseAnalytics.logEvent("used_setting", bundle);

        if (sharedPref.getBoolean("pinEnabled", false)) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.choose_pin_action)
                    .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.this, EnterPIN.class);
                            intent.putExtra("validate", true);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sharedPref.edit().putBoolean("pinEnabled", false).apply();
                            Toast.makeText(Settings.this, getString(R.string.removed), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        else {
            Intent intent = new Intent(this, EnterPIN.class);
            intent.putExtra("newPin", true);
            startActivity(intent);
        }
    }

    public void purchaseProForThis() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.pro_feature)
                .setMessage(R.string.buy_pro_for_this)
                .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.this, UpgradeToPro.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
