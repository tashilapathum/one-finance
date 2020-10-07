package com.tashila.mywalletfree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.WeekFields;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class Reports extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    SharedPreferences sharedPref;
    private DrawerLayout drawer;
    public static final String TAG = "Reports";

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*------------------------------Essential for every activity------------------------------*/
        Toolbar toolbar;
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            setTheme(R.style.AppThemeDark);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_reports_old);
            View layout = findViewById(R.id.drawer_layout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
            toolbar = findViewById(R.id.toolbar);
            toolbar.setBackground(getDrawable(R.color.colorToolbarDark));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_reports_old);
            toolbar = findViewById(R.id.toolbar);
        }

        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /*----------------------------------------------------------------------------------------*/
        AndroidThreeTen.init(this);

        showTopMsg();
        createReports();
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

    private void showTopMsg() {
        MaterialButton btnGotIt = findViewById(R.id.gotIt);
        if (!sharedPref.getBoolean("detailedReportsGotIt", false)) {
            final CardView cardView = findViewById(R.id.gotItCard);
            cardView.setVisibility(View.VISIBLE);
            btnGotIt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cardView.setVisibility(View.GONE);
                    sharedPref.edit().putBoolean("detailedReportsGotIt", true).apply();
                }
            });
        }
    }

    private void createReports() {
        TextView tvTodayIncome = findViewById(R.id.todayIncome);
        TextView tvTodaySpent = findViewById(R.id.todaySpent);
        TextView tvTodayBudget = findViewById(R.id.todayBudgetLeft);
        TextView tvTodayBudgetLeft = findViewById(R.id.todayPercent);
        TextView tvYesterdayIncome = findViewById(R.id.yesterIncome);
        TextView tvYesterdaySpent = findViewById(R.id.yesterSpent);
        TextView tvYesterdayBudget = findViewById(R.id.yesterBudgetLeft);
        TextView tvYesterdayBudgetLeft = findViewById(R.id.yesterPercent);
        TextView tvThisWeekIncome = findViewById(R.id.thisWeekIncome);
        TextView tvThisWeekSpent = findViewById(R.id.thisWeekSpent);
        TextView tvThisWeekBudget = findViewById(R.id.thisWeekBudgetLeft);
        TextView tvThisWeekBudgetLeft = findViewById(R.id.thisWeekPercent);
        TextView tvLastWeekIncome = findViewById(R.id.lastWeekIncome);
        TextView tvLastWeekSpent = findViewById(R.id.lastWeekSpent);
        TextView tvLastWeekBudget = findViewById(R.id.lastWeekBudgetLeft);
        TextView tvLastWeekBudgetLeft = findViewById(R.id.lastWeekPercent);
        TextView tvThisMonthIncome = findViewById(R.id.thisMonthIncome);
        TextView tvThisMonthSpent = findViewById(R.id.thisMonthSpent);
        TextView tvThisMonthBudget = findViewById(R.id.thisMonthBudgetLeft);
        TextView tvThisMonthBudgetLeft = findViewById(R.id.thisMonthPercent);
        TextView tvLastMonthIncome = findViewById(R.id.lastMonthIncome);
        TextView tvLastMonthSpent = findViewById(R.id.lastMonthSpent);
        TextView tvLastMonthBudget = findViewById(R.id.lastMonthBudgetLeft);
        TextView tvLastMonthBudgetLeft = findViewById(R.id.lastMonthPercent);

        //get all records from database
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(this, ViewModelProvider
                .AndroidViewModelFactory.getInstance(getApplication())).get(TransactionsViewModel.class);
        List<TransactionItem> transactionsList;
        transactionsList = transactionsViewModel.getTransactionsList();

        double dTodayIncome = 0;
        double dTodaySpent = 0;
        double dYesterdayIncome = 0;
        double dYesterdaySpent = 0;
        double dThisWeekIncome = 0;
        double dThisWeekSpent = 0;
        double dLastWeekIncome = 0;
        double dLastWeekSpent = 0;
        double dThisMonthIncome = 0;
        double dThisMonthSpent = 0;
        double dLastMonthIncome = 0;
        double dLastMonthSpent = 0;

        //income and expenses
        LocalDate currentDate = LocalDate.now();
        for (int i = 0; i < transactionsList.size(); i++) {
            TransactionItem transaction = transactionsList.get(i);
            LocalDateTime transactionDate = new DateTimeHandler(transaction.getUserDate()).getLocalDateTime();
            if (!transaction.isBankRelated()) {
                //daily
                if (transactionDate.getDayOfYear() == currentDate.getDayOfYear()) {
                    if (transaction.getPrefix().equals("+"))
                        dTodayIncome = dTodayIncome + Double.parseDouble(transaction.getAmount());
                    else
                        dTodaySpent = dTodaySpent + Double.parseDouble(transaction.getAmount());
                }
                if (transactionDate.getDayOfYear() == currentDate.getDayOfYear() - 1) {
                    if (transaction.getPrefix().equals("+"))
                        dYesterdayIncome = dYesterdayIncome + Double.parseDouble(transaction.getAmount());
                    else
                        dYesterdaySpent = dYesterdaySpent + Double.parseDouble(transaction.getAmount());
                }
                //weekly
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int weekOfYearTransaction = transactionDate.get(weekFields.weekOfWeekBasedYear());
                int weekOfYearCurrent = currentDate.get(weekFields.weekOfWeekBasedYear());
                Log.i(TAG, "week1:" + weekOfYearTransaction + " ,week2:" + weekOfYearCurrent);
                if (weekOfYearTransaction == weekOfYearCurrent) {
                    if (transaction.getPrefix().equals("+"))
                        dThisWeekIncome = dThisWeekIncome + Double.parseDouble(transaction.getAmount());
                    else
                        dThisWeekSpent = dThisWeekSpent + Double.parseDouble(transaction.getAmount());
                }
                if (weekOfYearTransaction == weekOfYearCurrent - 1) {
                    if (transaction.getPrefix().equals("+"))
                        dLastWeekIncome = dLastWeekIncome + Double.parseDouble(transaction.getAmount());
                    else
                        dLastWeekSpent = dLastWeekSpent + Double.parseDouble(transaction.getAmount());
                }
                //monthly
                if (transactionDate.getMonthValue() == currentDate.getMonthValue()) {
                    if (transaction.getPrefix().equals("+"))
                        dThisMonthIncome = dThisMonthIncome + Double.parseDouble(transaction.getAmount());
                    else
                        dThisMonthSpent = dThisMonthSpent + Double.parseDouble(transaction.getAmount());
                }
                if (transactionDate.getMonthValue() == currentDate.getMonthValue() - 1) {
                    if (transaction.getPrefix().equals("+"))
                        dLastMonthIncome = dLastMonthIncome + Double.parseDouble(transaction.getAmount());
                    else
                        dLastMonthSpent = dLastMonthSpent + Double.parseDouble(transaction.getAmount());
                }
            }
        }
        setData(tvTodayIncome, dTodayIncome);
        setData(tvTodaySpent, dTodaySpent);
        setData(tvYesterdayIncome, dYesterdayIncome);
        setData(tvYesterdaySpent, dYesterdaySpent);
        setData(tvThisWeekIncome, dThisWeekIncome);
        setData(tvThisWeekSpent, dThisWeekSpent);
        setData(tvLastWeekIncome, dLastWeekIncome);
        setData(tvLastWeekSpent, dLastWeekSpent);
        setData(tvThisMonthIncome, dThisMonthIncome);
        setData(tvThisMonthSpent, dThisMonthSpent);
        setData(tvLastMonthSpent, dLastMonthSpent);
        setData(tvLastMonthIncome, dLastMonthIncome);

        //budget and budget left
        String monthlyBudgetStr = sharedPref.getString("monthlyBudget", "N/A");
        double monthlyBudget = 0;
        double weeklyBudget;
        double dailyBudget;
        if (monthlyBudgetStr != null && !monthlyBudgetStr.equals("N/A")) {
            monthlyBudget = Double.parseDouble(monthlyBudgetStr);
        }
        weeklyBudget = monthlyBudget / 4;
        dailyBudget = monthlyBudget / YearMonth.now().lengthOfMonth();
        setData(tvTodayBudget, dailyBudget);
        setData(tvYesterdayBudget, dailyBudget);
        setData(tvThisWeekBudget, weeklyBudget);
        setData(tvLastWeekBudget, weeklyBudget);
        setData(tvThisMonthBudget, monthlyBudget);
        setData(tvLastMonthBudget, monthlyBudget);
        setData(tvTodayBudgetLeft, dailyBudget - dTodaySpent);
        setData(tvYesterdayBudgetLeft, dailyBudget - dYesterdaySpent);
        setData(tvThisWeekBudgetLeft, weeklyBudget - dThisWeekSpent);
        setData(tvLastWeekBudgetLeft, weeklyBudget - dLastWeekSpent);
        setData(tvThisMonthBudgetLeft, monthlyBudget - dThisMonthSpent);
        setData(tvLastMonthBudgetLeft, monthlyBudget - dLastMonthSpent);

        //charts
        createChart(String.valueOf(dTodaySpent), String.valueOf(dailyBudget), R.id.todayChart);
        createChart(String.valueOf(dYesterdaySpent), String.valueOf(dailyBudget), R.id.yesterChart);
        createChart(String.valueOf(dThisWeekSpent), String.valueOf(weeklyBudget), R.id.thisWeekChart);
        createChart(String.valueOf(dLastWeekSpent), String.valueOf(weeklyBudget), R.id.lastWeekChart);
        createChart(String.valueOf(dThisMonthSpent), String.valueOf(monthlyBudget), R.id.thisMonthChart);
        createChart(String.valueOf(dLastMonthSpent), String.valueOf(monthlyBudget), R.id.lastMonthChart);

    }

    private void setData(TextView textView, double value) {
        String currency = sharedPref.getString("currency", "");
        DecimalFormat df = new DecimalFormat("#.00");
        if (value == 0)
            textView.append(currency + "0.00");
        else
            textView.append(currency + df.format(value));
    }

    private void createChart(String spentAmount, String budget, int chartId) {
        float fBudget;
        float fBudgetLeft;
        float fSpentPercent;
        float fBudgetLeftPercent;
        float fSpent = Float.parseFloat(spentAmount);
        if (!budget.equals("N/A")) {
            //calculate
            fBudget = Float.parseFloat(budget);
            fBudgetLeft = fBudget - fSpent;
            fSpentPercent = (fSpent / fBudget) * 100;
            if (fBudget == 0) fSpentPercent = 0;
            fBudgetLeftPercent = 100 - fSpentPercent;

            //prepare chart
            PieChart pieChart = findViewById(chartId);
            pieChart.setUsePercentValues(true);
            pieChart.setDrawHoleEnabled(false);
            if (fBudget != 0)
                pieChart.setCenterText(((int) fSpentPercent) + getString(R.string.spent_percent));
            else
                pieChart.setCenterText(getString(R.string.budget_na));
            pieChart.setCenterTextTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            pieChart.getLegend().setEnabled(false);
            pieChart.setDrawEntryLabels(false);
            pieChart.getDescription().setEnabled(false);
            pieChart.animateY(1000, Easing.EaseOutCirc);

            //set data
            ArrayList<PieEntry> yValues = new ArrayList<>();
            yValues.add(new PieEntry(fSpentPercent, "Spent"));
            if (fBudgetLeft >= 0) yValues.add(new PieEntry(fBudgetLeftPercent, "Budget Left"));
            else yValues.add(new PieEntry(0f, "Budget Left"));
            PieDataSet dataSet = new PieDataSet(yValues, "Budget");
            dataSet.setColors(getResources().getColor(R.color.colorBackground), getResources().getColor(android.R.color.white));
            dataSet.setDrawValues(false);
            PieData data = new PieData(dataSet);
            pieChart.setData(data);
        }
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_recent_trans: {
                Intent intent = new Intent(this, TransactionHistory.class);
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
                Intent intent = new Intent(this, About.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_exit: {
                sharedPref.edit().putBoolean("exit", true).apply();
                finishAndRemoveTask();
                break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}




















