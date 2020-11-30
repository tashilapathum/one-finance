package com.tashila.mywalletfree;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

public class EnterPIN extends AppCompatActivity {
    private PinLockView pinLockView;
    private boolean newPin;
    private boolean validate;
    private boolean confirm;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_pin);
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

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

    private PinLockListener pinLockListener = new PinLockListener() {
        @Override
        public void onComplete(String pin) {
            if (newPin) {
                sharedPref.edit().putString("tempPin", pin).apply();
                Intent intent = new Intent(EnterPIN.this, EnterPIN.class);
                intent.putExtra("confirm", true);
                startActivity(intent);
                finish();
            } else if (confirm) {
                if (pin.contains(sharedPref.getString("tempPin", null))) {
                    sharedPref.edit().putString("userPin", pin).apply();
                    sharedPref.edit().putBoolean("pinEnabled", true).apply();
                    Toast.makeText(EnterPIN.this, R.string.saved, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    pinLockView.resetPinLockView();
                    Toast.makeText(EnterPIN.this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                }
            } else if (validate) {
                if (pin.contains(sharedPref.getString("userPin", null))) {
                    Intent intent = new Intent(EnterPIN.this, EnterPIN.class);
                    intent.putExtra("newPin", true);
                    startActivity(intent);
                    finish();
                } else {
                    pinLockView.resetPinLockView();
                    Toast.makeText(EnterPIN.this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (pin.contains(sharedPref.getString("userPin", null))) {
                    Intent intent = new Intent(EnterPIN.this, MainActivity.class);
                    intent.putExtra("pinCompleted", true);
                    startActivity(intent);
                } else {
                    pinLockView.resetPinLockView();
                    Toast.makeText(EnterPIN.this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
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

    public void goBack(View view) {
        finish();
    }
}