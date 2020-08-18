package com.tashila.mywalletfree;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.YearMonth;

import java.util.Locale;

public class InitialSetup extends AppCompatActivity {
    public static final String TAG = "InitialSetup";
    SharedPreferences sharedPref;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_setup);
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        AndroidThreeTen.init(this);
    }

    public void saveLangRadio(View view) {
        radioGroup = findViewById(R.id.radioGroup);
        View radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
        int radioId = radioGroup.indexOfChild(radioButton);
        RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
        String language = btn.getText().toString();
        sharedPref.edit().putString("language", language).apply();

        Locale locale = null;
        int checkingId=0;
        if (radioId == 1) {//0-en, 1-si
            locale = new Locale("si");
            checkingId = R.id.otSinhala;
        }
        if (radioId == 0) {
            locale = new Locale("en");
            checkingId = R.id.otEnglish;
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_initial_setup);
        radioGroup.check(checkingId);
    }

    public void onClickContinue(View view) {
        //language (in case of not tapping the language)
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        View radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
        int radioId = radioGroup.indexOfChild(radioButton);
        RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
        String language = btn.getText().toString();
        sharedPref.edit().putString("language", language).apply();

        //currency
        TextInputLayout tilAddCurrency = findViewById(R.id.addCurrency);
        EditText addCurrency = tilAddCurrency.getEditText();
        sharedPref.edit().putString("currency", addCurrency.getText().toString()).apply();

        //balance
        TextInputLayout tilAddBalance = findViewById(R.id.addBalance);
        EditText addBalance = tilAddBalance.getEditText();
        if (!addBalance.getText().toString().isEmpty())
            sharedPref.edit().putString("balance", addBalance.getText().toString()).apply();

        //budget
        TextInputLayout tilAddBudget = findViewById(R.id.addBudget);
        EditText editBudget = tilAddBudget.getEditText();
        if (!editBudget.getText().toString().isEmpty()) {
            String monthlyBudget = editBudget.getText().toString();
            String weeklyBudget = String.valueOf(Double.parseDouble(monthlyBudget) / 4);
            String dailyBudget = String.valueOf(Double.parseDouble(monthlyBudget) / YearMonth.now().lengthOfMonth());

            sharedPref.edit().putString("monthlyBudget", monthlyBudget).apply();
            sharedPref.edit().putString("weeklyBudget", weeklyBudget).apply();
            sharedPref.edit().putString("dailyBudget", dailyBudget).apply();
        }

        //next
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        sharedPref.edit().putBoolean("alreadyDidInitSetup", true).apply();
        Toast.makeText(this, "Welcome to My Wallet!", Toast.LENGTH_LONG).show();

        finish();
    }

    public void setQuickCurrency(View view) {
        int btnID = view.getId();
        TextInputLayout tilCurrency = findViewById(R.id.addCurrency);
        EditText etCurrency = tilCurrency.getEditText();
        String currency = null;
        switch (btnID) {
            case R.id.dollar: {
                currency = "$";
                break;
            }
            case R.id.euro: {
                currency = "€";
                break;
            }
            case R.id.yen: {
                currency = "¥";
                break;
            }
            case R.id.pound: {
                currency = "£";
                break;
            }
            case R.id.rupee: {
                currency = "₹";
                break;
            }
        }
        etCurrency.setText(currency);
    }

}
