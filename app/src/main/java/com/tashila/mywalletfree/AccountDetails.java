package com.tashila.mywalletfree;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class AccountDetails extends AppCompatActivity {
    private TextView tvAccName;
    private TextView tvBalance;
    private TextView tvInterest;
    private TextView tvAccNumber;
    private TextView tvExDetails;
    private TextView tvCreatedDate;
    private TextView tvLastUpdated;
    SharedPreferences sharedPref;
    int accountNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            setContentView(R.layout.activity_account_details);
            View layout = findViewById(R.id.rootLayout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
        }
        else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_account_details);
        }

        tvAccName = findViewById(R.id.adAccName);
        tvBalance = findViewById(R.id.adBalance);
        tvInterest = findViewById(R.id.adInterest);
        tvAccNumber = findViewById(R.id.adAccNumber);
        tvExDetails = findViewById(R.id.adExDetails);
        tvCreatedDate = findViewById(R.id.adCreatedDate);
        tvLastUpdated = findViewById(R.id.adLastUpdated);

        accountNo = sharedPref.getInt("manageAccNo", 1);
        setDetail(tvAccName, "accountName");
        setDetail(tvInterest, "annualInterestStr");
        setDetail(tvAccNumber, "accountNumber");
        setDetail(tvExDetails, "additionalInfo");
        setDetail(tvCreatedDate, "createdDate");

        ////set last updated////
        String activities = sharedPref.getString("activities" + accountNo, "N/A");
        String[] activitiesArr = activities.split("###");
        String lastUpdated = activitiesArr[activitiesArr.length - 1]; //last timestamp
        tvLastUpdated.setText(lastUpdated);

        ////set balances////
        //get balance
        Double currentBalance = Double.parseDouble(sharedPref.getString("accountBalance" + accountNo, null));
        //for first time
        if (!sharedPref.contains("highestBalance" + accountNo))
            sharedPref.edit().putString("highestBalance" + accountNo, String.valueOf(currentBalance)).apply();
        if (!sharedPref.contains("lowestBalance" + accountNo))
            sharedPref.edit().putString("lowestBalance" + accountNo, String.valueOf(currentBalance)).apply();
        double highestBalance = Double.parseDouble(sharedPref.getString("highestBalance" + accountNo, null));
        double lowestBalance = Double.parseDouble(sharedPref.getString("lowestBalance" + accountNo, null));
        //compare and update highest and lowest
        if (currentBalance > highestBalance)
            sharedPref.edit().putString("highestBalance" + accountNo, String.valueOf(currentBalance)).apply();
        if (currentBalance < lowestBalance)
            sharedPref.edit().putString("lowestBalance" + accountNo, String.valueOf(currentBalance)).apply();
        //set updated values
        String highest = sharedPref.getString("highestBalance" + accountNo, "N/A");
        String lowest = sharedPref.getString("lowestBalance" + accountNo, "N/A");
        String balances =
                getString(R.string.current) + currentBalance + "\n"
                        + getString(R.string.highest) + highest + "\n"
                        + getString(R.string.lowest) + lowest;
        tvBalance.setText(balances);
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

    public void setDetail(TextView view, String stringKey) {
        String detail = sharedPref.getString(stringKey + accountNo, null);
        //^determines which account from here
        view.setText(detail);
    }

    public void goBack(View view) {
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
    }

}
