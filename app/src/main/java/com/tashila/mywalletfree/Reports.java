package com.tashila.mywalletfree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Reports extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    SharedPreferences sharedPref;
    private DrawerLayout drawer;

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

        boolean alreadyShown = sharedPref.getBoolean("betaInaccuracy", false);
        if (!alreadyShown) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.beta_feature)
                    .setMessage(R.string.reports_test_des)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPref.edit().putBoolean("betaInaccuracy", true).apply();
                        }
                    })
                    .show();
        }

        //-------------------DAY-------------------//
        //get views
        TextView tvTodaySpent = findViewById(R.id.todaySpent);
        TextView tvTodayBudget = findViewById(R.id.todayBudgetLeft);
        TextView tvTodayBudgetLeft = findViewById(R.id.todayPercent);
        TextView tvYesterSpent = findViewById(R.id.yesterSpent);
        TextView tvYesterBudget = findViewById(R.id.yesterBudgetLeft);
        TextView tvYesterBudgetLeft = findViewById(R.id.yesterPercent);

        //get Data
        String todaySpent = sharedPref.getString("todaySpent", "0");
        String dailyBudget = sharedPref.getString("dailyBudget", "N/A");
        String todayBudgetLeft = sharedPref.getString("todayBudgetLeft", "N/A");
        String yesterSpent = sharedPref.getString("yesterSpent", "0");
        String yesterBudgetLeft = sharedPref.getString("yesterBudgetLeft", "N/A");
        DecimalFormat df = new DecimalFormat("0.00");
        if (!dailyBudget.equals("N/A"))
            dailyBudget = df.format(Double.parseDouble(dailyBudget));
        if (!todayBudgetLeft.equals("N/A"))
            todayBudgetLeft = df.format(Double.parseDouble(todayBudgetLeft));
        if (!yesterBudgetLeft.equals("N/A"))
            yesterBudgetLeft = df.format(Double.parseDouble(yesterBudgetLeft));

        //set data to views
        tvTodaySpent.append(todaySpent);
        tvTodayBudget.append(dailyBudget);
        tvTodayBudgetLeft.append(todayBudgetLeft);
        tvYesterSpent.append(yesterSpent);
        tvYesterBudget.append(dailyBudget);
        tvYesterBudgetLeft.append(yesterBudgetLeft);

        //pie charts
        createChart(todaySpent, dailyBudget, R.id.todayChart); //today
        createChart(yesterSpent, dailyBudget, R.id.yesterChart); //yesterday


        //-------------------WEEK-------------------//
        //get views
        TextView tvThisWeekSpent = findViewById(R.id.thisWeekSpent);
        TextView tvThisWeekBudget = findViewById(R.id.thisWeekBudgetLeft);
        TextView tvThisWeekBudgetLeft = findViewById(R.id.thisWeekPercent);
        TextView tvLastWeekSpent = findViewById(R.id.lastWeekSpent);
        TextView tvLastWeekBudget = findViewById(R.id.lastWeekBudgetLeft);
        TextView tvLastWeekBudgetLeft = findViewById(R.id.lastWeekPercent);

        //get Data
        String thisWeekSpent = sharedPref.getString("weekSpent", "0");
        String weeklyBudget = sharedPref.getString("weeklyBudget", "N/A");
        String thisWeekBudgetLeft = sharedPref.getString("weekBudgetLeft", "N/A");
        String lastWeekSpent = sharedPref.getString("lastWeekSpent", "0");
        String lastWeekBudgetLeft = sharedPref.getString("lastWeekBudgetLeft", "N/A");
        if (!weeklyBudget.equals("N/A"))
            weeklyBudget = df.format(Double.parseDouble(weeklyBudget));
        if (!thisWeekBudgetLeft.equals("N/A"))
            thisWeekBudgetLeft = df.format(Double.parseDouble(thisWeekBudgetLeft));
        if (!lastWeekBudgetLeft.equals("N/A"))
            lastWeekBudgetLeft = df.format(Double.parseDouble(lastWeekBudgetLeft));

        //set data to views
        tvThisWeekSpent.append(thisWeekSpent);
        tvThisWeekBudget.append(weeklyBudget);
        tvThisWeekBudgetLeft.append(thisWeekBudgetLeft);
        tvLastWeekSpent.append(lastWeekSpent);
        tvLastWeekBudget.append(weeklyBudget);
        tvLastWeekBudgetLeft.append(lastWeekBudgetLeft);

        //charts
        createChart(thisWeekSpent, weeklyBudget, R.id.thisWeekChart);
        createChart(lastWeekSpent, weeklyBudget, R.id.lastWeekChart);


        //-------------------MONTH-------------------//
        //get views
        TextView tvThisMonthSpent = findViewById(R.id.thisMonthSpent);
        TextView tvThisMonthBudget = findViewById(R.id.thisMonthBudgetLeft);
        TextView tvThisMonthBudgetLeft = findViewById(R.id.thisMonthPercent);
        TextView tvLastMonthSpent = findViewById(R.id.lastMonthSpent);
        TextView tvLastMonthBudget = findViewById(R.id.lastMonthBudgetLeft);
        TextView tvLastMonthBudgetLeft = findViewById(R.id.lastMonthPercent);

        //get Data
        String thisMonthSpent = sharedPref.getString("monthSpent", "0");
        String monthlyBudget = sharedPref.getString("monthlyBudget", "N/A");
        String thisMonthBudgetLeft = sharedPref.getString("monthBudgetLeft", "N/A");
        String lastMonthSpent = sharedPref.getString("lastMonthSpent", "0");
        String lastMonthBudgetLeft = sharedPref.getString("lastMonthBudgetLeft", "N/A");
        if (!monthlyBudget.equals("N/A"))
            monthlyBudget = df.format(Double.parseDouble(monthlyBudget));
        if (!thisMonthBudgetLeft.equals("N/A"))
            thisMonthBudgetLeft = df.format(Double.parseDouble(thisMonthBudgetLeft));
        if (!lastMonthBudgetLeft.equals("N/A"))
            lastMonthBudgetLeft = df.format(Double.parseDouble(lastMonthBudgetLeft));

        //set data to views
        tvThisMonthSpent.append(thisMonthSpent);
        tvThisMonthBudget.append(monthlyBudget);
        tvThisMonthBudgetLeft.append(thisMonthBudgetLeft);
        tvLastMonthSpent.append(lastMonthSpent);
        tvLastMonthBudget.append(monthlyBudget);
        tvLastMonthBudgetLeft.append(lastMonthBudgetLeft);

        //charts
        createChart(thisMonthSpent, monthlyBudget, R.id.thisMonthChart);
        createChart(lastMonthSpent, monthlyBudget, R.id.lastMonthChart);
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
            fBudgetLeftPercent = 100 - fSpentPercent;

            //prepare chart
            PieChart pieChart = findViewById(chartId);
            pieChart.setUsePercentValues(true);
            pieChart.setDrawHoleEnabled(false);
            pieChart.setCenterText(((int) fSpentPercent) + getString(R.string.spent_percent));
            pieChart.setCenterTextTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            pieChart.getLegend().setEnabled(false);
            pieChart.setDrawEntryLabels(false);
            pieChart.getDescription().setEnabled(false);
            pieChart.getDescription().setTextColor(R.attr.colorAccent);
            pieChart.animateY(1000, Easing.EaseInOutCubic);

            //set data
            ArrayList<PieEntry> yValues = new ArrayList<>();
            yValues.add(new PieEntry(fSpentPercent, "Spent"));
            if (fBudgetLeft >= 0) yValues.add(new PieEntry(fBudgetLeftPercent, "Budget Left"));
            else yValues.add(new PieEntry(0f, "Budget Left"));
            PieDataSet dataSet = new PieDataSet(yValues, "Budget");
            dataSet.setColors(ColorTemplate.LIBERTY_COLORS);
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
                DialogAbout dialogAbout = new DialogAbout();
                dialogAbout.show(getSupportFragmentManager(), "about dialog");
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




















