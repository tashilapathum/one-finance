package com.tashila.mywalletfree;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class DailyReportsAdapter extends ListAdapter<DailyReportsFragment.DailyReport, DailyReportsAdapter.ReportHolder> {
    private SharedPreferences sharedPref;
    private Context context;
    private String currency;
    private String theme;

    protected DailyReportsAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<DailyReportsFragment.DailyReport> DIFF_CALLBACK = new DiffUtil.ItemCallback<DailyReportsFragment.DailyReport>() {
        @Override
        public boolean areItemsTheSame(@NonNull DailyReportsFragment.DailyReport oldItem, @NonNull DailyReportsFragment.DailyReport newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull DailyReportsFragment.DailyReport oldItem, @NonNull DailyReportsFragment.DailyReport newItem) {
            return false;
        }
    };

    @NonNull
    @Override
    public ReportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_daily_report, parent, false);
        return new ReportHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportHolder holder, int position) {
        DailyReportsFragment.DailyReport dailyReport = getItem(position);
        context = holder.itemView.getContext();
        sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
        theme = sharedPref.getString("theme", "light");

        //Strings
        holder.tvDate.setText(dailyReport.getDate());
        holder.tvIncome.setText(dailyReport.getIncome());
        holder.tvExpenses.setText(dailyReport.getExpenses());
        holder.tvBudget.setText(dailyReport.getBudget());
        holder.tvBudgetLeft.setText(dailyReport.getBudgetLeft());
        if (dailyReport.getBudgetLeft().contains("-"))
            holder.tvBudgetLeft.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        else
            holder.tvBudgetLeft.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
        holder.tvHighestExpense.setText(dailyReport.getHighestExpense());
        holder.tvHighestItem.setText(dailyReport.getHighestItem());
        if (!dailyReport.getIncomeDiff().contains("null")
                && !dailyReport.getIncomeDiff().equals("(+.00)") && !dailyReport.getIncomeDiff().equals("(-.00)")) {
            holder.tvIncomeDiff.setVisibility(View.VISIBLE);
            holder.tvIncomeDiff.setText(dailyReport.getIncomeDiff());
            if (dailyReport.getIncomeDiff().contains("+"))
                holder.tvIncomeDiff.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            else
                holder.tvIncomeDiff.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        }
        else
            holder.tvIncomeDiff.setVisibility(View.GONE);
        if (!dailyReport.getExpensesDiff().contains("null")
                && !dailyReport.getExpensesDiff().equals("(+.00)") && !dailyReport.getExpensesDiff().equals("(-.00)")) {
            holder.tvExpensesDiff.setVisibility(View.VISIBLE);
            holder.tvExpensesDiff.setText(dailyReport.getExpensesDiff());
            if (dailyReport.getExpensesDiff().contains("+-")) { //that weird thing of 'today'
                String diff = dailyReport.getExpensesDiff().replace("+", "");
                holder.tvExpensesDiff.setText(diff);
            }
            if (dailyReport.getExpensesDiff().contains("-"))
                holder.tvExpensesDiff.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            else
                holder.tvExpensesDiff.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        }
        else
            holder.tvExpensesDiff.setVisibility(View.GONE);

        //charts
        if (dailyReport.getIncome() != null || dailyReport.getExpenses() != null)
            drawIncomeExpenseChart(holder.chartInEx, dailyReport);
        if (new Amount(context, dailyReport.getBudget()).getAmountValue() != 0)
            drawBudgetChart(holder.chartBudget, dailyReport);
    }

    private void drawIncomeExpenseChart(HorizontalBarChart chartView, DailyReportsFragment.DailyReport dailyReport) {
        List<BarEntry> incomes = new ArrayList<>();
        List<BarEntry> expenses = new ArrayList<>();
        incomes.add(new BarEntry(1f, Float.parseFloat(dailyReport.getIncome().replace(currency, ""))));
        expenses.add(new BarEntry(2f, Float.parseFloat(dailyReport.getExpenses().replace(currency, ""))));
        BarDataSet expensesSet = new BarDataSet(expenses, "Expenses");
        expensesSet.setColor(context.getResources().getColor(R.color.colorRed));
        BarDataSet incomesSet = new BarDataSet(incomes, "Income");
        incomesSet.setColor(context.getResources().getColor(R.color.colorBlue));
        BarData data = new BarData(expensesSet, incomesSet);
        chartView.setData(data);
        chartView.getAxisLeft().setDrawGridLines(false);
        chartView.getAxisRight().setDrawGridLines(false);
        chartView.getXAxis().setDrawGridLines(false);
        chartView.getAxisLeft().setDrawAxisLine(false);
        chartView.getAxisRight().setDrawAxisLine(false);
        chartView.getXAxis().setDrawAxisLine(false);
        chartView.getAxisRight().setDrawLabels(false);
        chartView.getXAxis().setDrawLabels(false);
        chartView.animateY(250, Easing.EaseInOutSine);
        chartView.getDescription().setEnabled(false);
        chartView.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        chartView.getLegend().setTextColor(context.getResources().getColor(R.color.colorDivider));
        chartView.getAxisLeft().setTextColor(context.getResources().getColor(R.color.colorDivider));
        chartView.getAxisRight().setTextColor(context.getResources().getColor(R.color.colorDivider));
        chartView.getAxisLeft().setAxisMinimum(0);
        chartView.getBarData().setDrawValues(false);
        chartView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (theme.equalsIgnoreCase("dark"))
            chartView.getRenderer().getPaintRender().setShadowLayer(4, 3, -2, context.getResources().getColor(R.color.colorShadowDark));
        else
            chartView.getRenderer().getPaintRender().setShadowLayer(4, 3, -2, context.getResources().getColor(R.color.colorShadow));
        chartView.invalidate();
    }

    private void drawBudgetChart(HorizontalBarChart chartView, DailyReportsFragment.DailyReport dailyReport) {
        List<BarEntry> budgetUsed = new ArrayList<>();
        double budget = Double.parseDouble(dailyReport.getBudget().replace(currency, ""));
        double expenses = Double.parseDouble(dailyReport.getExpenses().replace(currency, ""));
        double usedBudget = (expenses / budget) * 100;
        if (expenses > budget)
            usedBudget = 100;
        budgetUsed.add(new BarEntry(1f, (float) usedBudget));
        BarDataSet budgetSet = new BarDataSet(budgetUsed, context.getString(R.string.budget_usage));
        budgetSet.setColor(context.getResources().getColor(R.color.colorAccent));
        BarData data = new BarData(budgetSet);
        chartView.setData(data);
        chartView.getAxisLeft().setDrawGridLines(false);
        chartView.getAxisLeft().setDrawAxisLine(false);
        chartView.getAxisRight().setDrawGridLines(false);
        chartView.getAxisRight().setDrawAxisLine(false);
        chartView.getAxisRight().setDrawLabels(false);
        chartView.getXAxis().setDrawGridLines(false);
        chartView.getXAxis().setDrawAxisLine(false);
        chartView.getXAxis().setDrawLabels(false);
        chartView.animateY(250, Easing.EaseInOutSine);
        chartView.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        chartView.getLegend().setTextColor(context.getResources().getColor(R.color.colorDivider));
        chartView.getAxisLeft().setTextColor(context.getResources().getColor(R.color.colorDivider));
        chartView.getAxisRight().setTextColor(context.getResources().getColor(R.color.colorDivider));
        chartView.getBarData().setDrawValues(false);
        chartView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (theme.equalsIgnoreCase("dark"))
            chartView.getRenderer().getPaintRender().setShadowLayer(4, 3, -2, context.getResources().getColor(R.color.colorShadowDark));
        else
            chartView.getRenderer().getPaintRender().setShadowLayer(4, 3, -2, context.getResources().getColor(R.color.colorShadow));
        chartView.getAxisLeft().setAxisMinimum(0);
        chartView.getAxisLeft().setAxisMaximum(100);
        chartView.getAxisLeft().setLabelCount(5, true);
        Amount amount = new Amount(context, usedBudget);
        chartView.getDescription().setText(amount.getAmountStringWithoutCurrency() + context.getString(R.string.budget_used));
        chartView.getDescription().setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
        chartView.invalidate();
    }

    static class ReportHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private TextView tvIncome;
        private TextView tvExpenses;
        private TextView tvBudget;
        private TextView tvBudgetLeft;
        private TextView tvHighestExpense;
        private TextView tvHighestItem;
        private TextView tvIncomeDiff;
        private TextView tvExpensesDiff;
        private HorizontalBarChart chartInEx;
        private HorizontalBarChart chartBudget;

        public ReportHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.date);
            tvIncome = itemView.findViewById(R.id.income);
            tvExpenses = itemView.findViewById(R.id.expenses);
            tvBudget = itemView.findViewById(R.id.budget);
            tvBudgetLeft = itemView.findViewById(R.id.budgetLeft);
            tvHighestExpense = itemView.findViewById(R.id.highestExpense);
            tvHighestItem = itemView.findViewById(R.id.highestItem);
            tvIncomeDiff = itemView.findViewById(R.id.incomeDiff);
            tvExpensesDiff = itemView.findViewById(R.id.expensesDiff);
            chartInEx = itemView.findViewById(R.id.chartInEx);
            chartBudget = itemView.findViewById(R.id.chartBudget);
        }
    }
}
