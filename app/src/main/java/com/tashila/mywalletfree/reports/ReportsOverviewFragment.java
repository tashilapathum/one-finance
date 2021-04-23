package com.tashila.mywalletfree.reports;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.tashila.mywalletfree.DateTimeHandler;
import com.tashila.mywalletfree.R;
import com.tashila.mywalletfree.transactions.TransactionItem;
import com.tashila.mywalletfree.transactions.TransactionsViewModel;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class ReportsOverviewFragment extends Fragment {
    SharedPreferences sharedPref;
    private View view;
    public static final String TAG = "Reports";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reports_overview, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", MODE_PRIVATE);
        createReports();

        return view;
    }

    private void createReports() {
        TextView tvTodayIncome = view.findViewById(R.id.todayIncome);
        TextView tvTodaySpent = view.findViewById(R.id.todaySpent);
        TextView tvTodayBudget = view.findViewById(R.id.todayBudgetLeft);
        TextView tvTodayBudgetLeft = view.findViewById(R.id.todayPercent);
        TextView tvYesterdayIncome = view.findViewById(R.id.yesterIncome);
        TextView tvYesterdaySpent = view.findViewById(R.id.yesterSpent);
        TextView tvYesterdayBudget = view.findViewById(R.id.yesterBudgetLeft);
        TextView tvYesterdayBudgetLeft = view.findViewById(R.id.yesterPercent);
        TextView tvThisWeekIncome = view.findViewById(R.id.thisWeekIncome);
        TextView tvThisWeekSpent = view.findViewById(R.id.thisWeekSpent);
        TextView tvThisWeekBudget = view.findViewById(R.id.thisWeekBudgetLeft);
        TextView tvThisWeekBudgetLeft = view.findViewById(R.id.thisWeekPercent);
        TextView tvLastWeekIncome = view.findViewById(R.id.lastWeekIncome);
        TextView tvLastWeekSpent = view.findViewById(R.id.lastWeekSpent);
        TextView tvLastWeekBudget = view.findViewById(R.id.lastWeekBudgetLeft);
        TextView tvLastWeekBudgetLeft = view.findViewById(R.id.lastWeekPercent);
        TextView tvThisMonthIncome = view.findViewById(R.id.thisMonthIncome);
        TextView tvThisMonthSpent = view.findViewById(R.id.thisMonthSpent);
        TextView tvThisMonthBudget = view.findViewById(R.id.thisMonthBudgetLeft);
        TextView tvThisMonthBudgetLeft = view.findViewById(R.id.thisMonthPercent);
        TextView tvLastMonthIncome = view.findViewById(R.id.lastMonthIncome);
        TextView tvLastMonthSpent = view.findViewById(R.id.lastMonthSpent);
        TextView tvLastMonthBudget = view.findViewById(R.id.lastMonthBudgetLeft);
        TextView tvLastMonthBudgetLeft = view.findViewById(R.id.lastMonthPercent);

        //get all records from database
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(this, ViewModelProvider
                .AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
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
            PieChart pieChart = view.findViewById(chartId);
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

}




















