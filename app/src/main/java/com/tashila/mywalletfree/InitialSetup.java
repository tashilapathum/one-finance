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
        radioGroup = findViewById(R.id.radioGroup);
        RadioButton rb1 = findViewById(R.id.otEnglish);
        RadioButton rb2 = findViewById(R.id.otSinhala);
        rb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLangRadio(view.getId());
            }
        });
        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLangRadio(view.getId());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPref.getString("language", "english").equalsIgnoreCase("සිංහල"))
            radioGroup.check(radioGroup.getChildAt(1).getId());
    }

    public void saveLangRadio(int i) {
        Log.i(TAG, "value of i: "+i);
        Locale locale = null;
        String language = null;
        if (i == R.id.otSinhala) {//0-en, 1-si
            locale = new Locale("si");
            language = "සිංහල";
        }
        if (i == R.id.otEnglish) {
            locale = new Locale("en");
            language = "English";
        }
        
        sharedPref.edit().putString("language", language).apply();
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_initial_setup);
        recreate();
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
        else
            sharedPref.edit().putString("balance", "0.00").apply();

        //budget
        TextInputLayout tilAddBudget = findViewById(R.id.addBudget);
        EditText editBudget = tilAddBudget.getEditText();
        if (!editBudget.getText().toString().isEmpty()) {
            String monthlyBudget = editBudget.getText().toString();
            sharedPref.edit().putString("monthlyBudget", monthlyBudget).apply();
        }
        else
            sharedPref.edit().putString("monthlyBudget", "0.00").apply();


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
            case R.id.ru: {
                currency = "රු.";
                break;
            }
            case R.id.rs: {
                currency = "Rs.";
                break;
            }
            case R.id.lkr: {
                currency = "LKR";
                break;
            }
        }
        etCurrency.setText(currency);
    }

}
