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
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Settings extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TimePickerDialog.OnTimeSetListener {
    public static final String TAG = "Settings";
    SharedPreferences sharedPref;
    private DrawerLayout drawer;
    String timeString;
    private MaterialCheckBox exitCheckBox;
    private MaterialCheckBox suggestCheckBox;
    private FirebaseAnalytics firebaseAnalytics;

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
        }
        else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);
            toolbar = findViewById(R.id.toolbar);
        }

        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView =findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /*----------------------------------------------------------------------------------------*/
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

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
        if (sharedPref.getBoolean("exit", false)) {
            finishAndRemoveTask();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.edit().putBoolean("exit", false).apply();
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
                Intent intent = new Intent(this, ReportsOverviewFragment.class);
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
        }
        else {
            super.onBackPressed();
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
        if (sharedPref.getBoolean("MyWalletPro", false)) {
            DialogFragment timePicker = new TimePickerFrag();
            timePicker.show(getSupportFragmentManager(), "time picker");
        }
        else purchaseProForThis();
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
        if (sharedPref.getBoolean("MyWalletPro", false)) {
            DialogTheme dialogTheme = new DialogTheme();
            dialogTheme.show(getSupportFragmentManager(), "theme dialog");
        }
        else purchaseProForThis();
    }

    public void chooseHome(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "set_home");
        firebaseAnalytics.logEvent("used_setting", bundle);
        if (sharedPref.getBoolean("MyWalletPro", false)) {
            DialogChooseHome dialogChooseHome = new DialogChooseHome();
            dialogChooseHome.show(getSupportFragmentManager(), "choose home dialog");
        }
        else purchaseProForThis();
    }

    public void manageAccounts(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("setting", "manage_acc");
        firebaseAnalytics.logEvent("used_setting", bundle);
        Intent intent = new Intent(this, AccountManager.class);
        startActivity(intent);
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
