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
import com.github.mikephil.charting.utils.ColorTemplate;

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

        account = (Account) getIntent().getSerializableExtra("neededAccount");
        if (account == null)
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
        List<String> balanceList = account.getBalanceHistory();
        double highest = 0;
        double lowest = Double.parseDouble(balanceList.get(0));
        double total = 0;
        DecimalFormat df = new DecimalFormat("#.00");
        Log.i(TAG, "balanceList: " + balanceList.size());
        for (int i = 0; i < balanceList.size(); i++) {
            double currentItem = Double.parseDouble(balanceList.get(i));
            if (currentItem > highest)
                highest = currentItem;
            if (currentItem < lowest)
                lowest = currentItem;
            total = total + currentItem;
        }
        double average = total / balanceList.size();
        tvBalHighest.append(currency + df.format(highest));
        tvBalLowest.append(currency + df.format(lowest));
        tvBalAverage.append(currency + df.format(average));
        createBalanceChart();
        if (account.isMultiInterest()) {
            String formattedInterests = "";
            String[] interests = account.getInterestRate().split("~~~");
            for (int i = 0; i < interests.length; i++) {
                String interest = currency + interests[i].split("~")[0]
                        + " - "
                        + currency + interests[i].split("~")[1]
                        + " -> "
                        + interests[i].split("~")[2]
                        + "\n";
                formattedInterests = formattedInterests + interest;
            }
            tvInterest.setText(formattedInterests);
        } else
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
        List<String> balanceList = account.getBalanceHistory();
        for (int x = 0; x < balanceList.size(); x++) {
            Entry entry = new Entry((float) x, Float.parseFloat(balanceList.get(x)));
            values.add(entry);
        }
        LineDataSet dataSet = new LineDataSet(values, getResources().getString(R.string.balance));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(getResources().getColor(R.color.colorAccent));
        List<ILineDataSet> dataSetList = new ArrayList<>();
        dataSetList.add(dataSet);
        LineData data = new LineData(dataSetList);
        lineChart.animateY(1000, Easing.EaseInSine);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getXAxis().setEnabled(false);
        lineChart.getAxisLeft().setTextColor(getResources().getColor(R.color.colorDivider));
        lineChart.getAxisRight().setTextColor(getResources().getColor(R.color.colorDivider));
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
