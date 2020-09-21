package com.tashila.mywalletfree;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountDetails extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private String currency;
    private AccountsViewModel accountsViewModel;
    private Account account;
    public static final String TAG = "AccountDetails";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        accountsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(AccountsViewModel.class);
        account = getSelectedAccount();
        currency = sharedPref.getString("currency", "");

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
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_account_details);
        }

        setData();
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

    private void setData() {
        TextView tvAccName = findViewById(R.id.adAccName);
        TextView tvBalance = findViewById(R.id.adBalance);
        TextView tvBalHighest = findViewById(R.id.highestBal);
        TextView tvBalLowest = findViewById(R.id.lowestBal);
        TextView tvBalAverage = findViewById(R.id.averageBal);
        TextView tvInterest = findViewById(R.id.adInterest);
        TextView tvAccNumber = findViewById(R.id.adAccNumber);
        TextView tvExDetails = findViewById(R.id.adExDetails);
        TextView tvCreatedDate = findViewById(R.id.adCreatedDate);
        TextView tvLastUpdated = findViewById(R.id.adLastUpdated);

        tvAccName.setText(account.getAccName());
        tvBalance.setText(currency + account.getAccBalance());
        List<Double> balanceList = account.getBalanceHistory();
        double highest = 0;
        double lowest = 0;
        double total = 0;
        DecimalFormat df = new DecimalFormat("#.00");
        Log.i(TAG, "balanceList: "+balanceList.size());
        for (int i = 0; i < balanceList.size(); i++) {
            if (balanceList.get(i) > highest)
                highest = balanceList.get(i);
            if (balanceList.get(i) < lowest)
                lowest = balanceList.get(i);
            total = total + balanceList.get(i);
        }
        double average = total / balanceList.size();
        tvBalHighest.append(currency + df.format(highest));
        tvBalLowest.append(currency + df.format(lowest));
        tvBalAverage.append(currency + df.format(average));
        //if (account.getBalanceHistory().size() < 1)
        createBalanceChart();
        tvInterest.setText(account.getInterestRate());
        tvAccNumber.setText(account.getAccNumber());
        tvExDetails.setText(account.getMoreDetails());
        String createdDate = account.getActivities().get(0).split("###")[1];
        String lastUpdated = account.getActivities().get(account.getActivities().size() - 1).split("###")[1];
        tvCreatedDate.setText(createdDate);
        tvLastUpdated.setText(lastUpdated);
    }

    public void goBack(View view) {
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
    }

    private void createBalanceChart() {
        LineChart lineChart = findViewById(R.id.AccBalanceChart);
        List<Entry> values = new ArrayList<>();
        List<Double> balanceList = account.getBalanceHistory();
        for (int x = 0; x < balanceList.size(); x++) {
            Entry entry = new Entry((float) x, balanceList.get(x).floatValue());
            values.add(entry);
        }
        LineDataSet dataSet = new LineDataSet(values, getResources().getString(R.string.balance));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        List<ILineDataSet> dataSetList = new ArrayList<>();
        dataSetList.add(dataSet);
        LineData data = new LineData(dataSetList);
        lineChart.animateXY(1000, 1000, Easing.EaseInCubic);
        lineChart.setData(data);
        lineChart.invalidate();
    }

    private Account getSelectedAccount() {
        Account account = null;
        List<Account> accountList = accountsViewModel.getAllAccounts();
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).isSelected())
                account = accountList.get(i);
        }
        return account;
    }

}
