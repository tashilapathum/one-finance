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
import android.app.AlertDialog;
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
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Settings extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TimePickerDialog.OnTimeSetListener {
    public static final String TAG = "Settings";
    SharedPreferences sharedPref;
    private DrawerLayout drawer;
    String timeString;

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
            invertDrawables();
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
        }
        else {
            super.onBackPressed();
        }
    }

    public void editQuickList(View view) {
        Intent intent = new Intent(Settings.this, EditQuickList.class);
        startActivity(intent);
    }

    public void selectLanguage(View view) {
        DialogLanguage dialogLanguage = new DialogLanguage();
        dialogLanguage.show(getSupportFragmentManager(), "language dialog");
    }

    public void selectCurrency(View view) {
        DialogCurrency dialogCurrency = new DialogCurrency();
        dialogCurrency.show(getSupportFragmentManager(), "currency dialog");
    }

    public void editBudget(View view) {
        DialogBudget dialogBudget = new DialogBudget();
        dialogBudget.show(getSupportFragmentManager(), "budget dialog");
    }

    public void editNotifyTime(View view) {
        if (sharedPref.getBoolean("MyWalletPro", false)) {
            DialogFragment timePicker = new TimePickerFrag();
            timePicker.show(getSupportFragmentManager(), "time picker");
        }
        else purchaseProForThis();
    }

    /*---------------Time---------------*/
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

        Toast.makeText(this, getString(R.string.notification_set_at) + timeString + getString(R.string.everydayy), Toast.LENGTH_LONG).show();
    }
    /*------------------------------------*/

    public void setTheme(View view) {
        if (sharedPref.getBoolean("MyWalletPro", false)) {
            DialogTheme dialogTheme = new DialogTheme();
            dialogTheme.show(getSupportFragmentManager(), "theme dialog");
        }
        else purchaseProForThis();
    }

    public void chooseHome(View view) {
        if (sharedPref.getBoolean("MyWalletPro", false)) {
            DialogChooseHome dialogChooseHome = new DialogChooseHome();
            dialogChooseHome.show(getSupportFragmentManager(), "choose home dialog");
        }
        else purchaseProForThis();
    }

    public void purchaseProForThis() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.pro_feature)
                .setMessage(R.string.buy_pro_for_this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.this, UpgradeToPro.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    private void invertDrawables() {
        Essentials essentials = new Essentials(this);
        essentials.invertDrawable(findViewById(R.id.image1));
        essentials.invertDrawable(findViewById(R.id.image2));
        essentials.invertDrawable(findViewById(R.id.image3));
        essentials.invertDrawable(findViewById(R.id.image4));
        essentials.invertDrawable(findViewById(R.id.image5));
        essentials.invertDrawable(findViewById(R.id.image6));
        essentials.invertDrawable(findViewById(R.id.image7));
    }
}
