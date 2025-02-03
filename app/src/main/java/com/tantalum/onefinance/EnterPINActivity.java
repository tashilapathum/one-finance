package com.tantalum.onefinance;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import java.util.Locale;

public class EnterPINActivity extends AppCompatActivity {
    private PinLockView pinLockView;
    private boolean newPin;
    private boolean validate;
    private boolean confirm;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_enter_pin);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(null);
        /*----------------------------------------------------------------------------------------*/

        pinLockView = findViewById(R.id.pin_lock_view);
        pinLockView.setPinLockListener(pinLockListener);
        pinLockView.attachIndicatorDots(findViewById(R.id.indicator_dots));

        newPin = getIntent().getBooleanExtra("newPin", false);
        validate = getIntent().getBooleanExtra("validate", false);
        confirm = getIntent().getBooleanExtra("confirm", false);

        //title
        TextView tvTitle = findViewById(R.id.pinTitle);
        if (newPin)
            tvTitle.setText(R.string.new_pin);
        if (validate)
            tvTitle.setText(R.string.validate_pin);
        if (confirm)
            tvTitle.setText(R.string.confirm_pin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPref.getBoolean("exit", false))
            finishAndRemoveTask();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private PinLockListener pinLockListener = new PinLockListener() {
        @Override
        public void onComplete(String pin) {
            if (newPin) {
                sharedPref.edit().putString("tempPin", pin).apply();
                Intent intent = new Intent(EnterPINActivity.this, EnterPINActivity.class);
                intent.putExtra("confirm", true);
                startActivity(intent);
                finish();
            } else if (confirm) {
                if (pin.contains(sharedPref.getString("tempPin", null))) {
                    sharedPref.edit().putString("userPin", pin).apply();
                    sharedPref.edit().putBoolean("pinEnabled", true).apply();
                    Toast.makeText(EnterPINActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    pinLockView.resetPinLockView();
                    Toast.makeText(EnterPINActivity.this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                }
            } else if (validate) {
                if (pin.contains(sharedPref.getString("userPin", null))) {
                    Intent intent = new Intent(EnterPINActivity.this, EnterPINActivity.class);
                    intent.putExtra("newPin", true);
                    startActivity(intent);
                    finish();
                } else {
                    pinLockView.resetPinLockView();
                    Toast.makeText(EnterPINActivity.this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (pin.contains(sharedPref.getString("userPin", null))) {
                    Intent intent = new Intent(EnterPINActivity.this, MainActivity.class);
                    intent.putExtra("pinCompleted", true);
                    startActivity(intent);
                } else {
                    pinLockView.resetPinLockView();
                    Toast.makeText(EnterPINActivity.this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onEmpty() {

        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {

        }
    };
}