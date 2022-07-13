package com.tantalum.onefinance.accounts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.bank.AccountActivitiesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountDetails extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private AccountsViewModel accountsViewModel;
    private Account selectedAccount;
    public static final String TAG = "AccountDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        accountsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(AccountsViewModel.class);

        selectedAccount = getSelectedAccount();

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
        TextView tvAccName = findViewById(R.id.accName);
        TextView tvAccDetails = findViewById(R.id.accDetails);

        tvAccName.setText(selectedAccount.getAccName());
        if (selectedAccount.getMoreDetails().isEmpty())
            findViewById(R.id.imCopy).setVisibility(View.GONE);
        else
            tvAccDetails.setText(selectedAccount.getMoreDetails());

        createBalanceChart();

        List<String> activityHistory = selectedAccount.getActivities();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        AccountActivitiesAdapter adapter = new AccountActivitiesAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.scheduleLayoutAnimation();
        adapter.submitList(activityHistory);
    }

    public void goBack(View view) {
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
    }

    private void createBalanceChart() {
        LineChart lineChart = findViewById(R.id.AccBalanceChart);
        List<Entry> values = new ArrayList<>();
        List<String> balanceList = selectedAccount.getBalanceHistory();
        for (int x = 0; x < balanceList.size(); x++) {
            Entry entry = new Entry((float) x, Float.parseFloat(balanceList.get(x)));
            values.add(entry);
        }
        LineDataSet dataSet = new LineDataSet(values, getResources().getString(R.string.balance));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(getResources().getColor(R.color.colorAccent));
        dataSet.setDrawFilled(true);
        List<ILineDataSet> dataSetList = new ArrayList<>();
        dataSetList.add(dataSet);
        LineData data = new LineData(dataSetList);
        lineChart.animateY(1000, Easing.EaseOutCirc);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getXAxis().setEnabled(false);
        lineChart.getAxisLeft().setTextColor(getResources().getColor(R.color.colorDivider));
        lineChart.getAxisRight().setTextColor(getResources().getColor(R.color.colorDivider));
        lineChart.setData(data);
        lineChart.invalidate();
    }

    private Account getSelectedAccount() {
        Account selectedAccount = null;
        String accName = getIntent().getStringExtra("neededAccountName");
        List<Account> accountList = accountsViewModel.getAllAccounts();
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getAccName().equals(accName))
                selectedAccount = accountList.get(i);
        }
        return selectedAccount;
    }

    public void editAccount(View view) {
        Intent intent = new Intent(this, NewAccount.class);
        intent.putExtra("isNewAccount", false);
        intent.putExtra("neededAccountName", selectedAccount.getAccName());
        startActivity(intent);
    }

    public void copy(View view) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("extraDetails", ((TextView) findViewById(R.id.accDetails)).getText());
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
    }

}
