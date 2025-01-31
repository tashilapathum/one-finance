package com.tantalum.onefinance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.mynameismidori.currencypicker.CurrencyPicker;
import com.mynameismidori.currencypicker.CurrencyPickerListener;

import java.util.Locale;

public class InitialSetupActivity extends AppCompatActivity {
    public static final String TAG = "InitialSetupActivity";
    private SharedPreferences sharedPref;
    private RadioGroup radioGroup;
    private TextInputLayout tilCurrency;
    private EditText etCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_setup);
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        radioGroup = findViewById(R.id.radioGroup);
        RadioButton rb1 = findViewById(R.id.otEnglish);
        RadioButton rb2 = findViewById(R.id.otSinhala);
        tilCurrency = findViewById(R.id.addCurrency);
        etCurrency = tilCurrency.getEditText();
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
        findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrencyPicker picker = CurrencyPicker.newInstance("Select currency");  // dialog title
                picker.setListener(new CurrencyPickerListener() {
                    @Override
                    public void onSelectCurrency(String name, String code, String symbol, int flagDrawableResID) {
                        etCurrency.setText(symbol);
                        picker.dismiss();
                    }
                });
                picker.show(getSupportFragmentManager(), "CURRENCY_PICKER");
            }
        });

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickContinue();
            }
        });
        ((CheckBox) findViewById(R.id.agreeCheck)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                btnContinue.setEnabled(b);
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

    public void onClickContinue() {
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
        Toast.makeText(this, "Welcome to One Finance!", Toast.LENGTH_LONG).show();

        finish();
    }

    public void setQuickCurrency(View view) {
        int btnID = view.getId();
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
        }
        etCurrency.setText(currency);
    }

    public void openPrivacyPolicy(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tashila.me/projects/one-finance/privacy"));
        startActivity(intent);
    }

    public void openTC(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tashila.me/projects/one-finance/terms"));
        startActivity(intent);
    }

}
