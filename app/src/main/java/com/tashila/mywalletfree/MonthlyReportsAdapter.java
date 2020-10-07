package com.tashila.mywalletfree;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MonthlyReportsAdapter extends ListAdapter<MonthlyReportsFragment.MonthlyReport, MonthlyReportsAdapter.ReportHolder> {
    public static final String TAG = "MonthlyReportsAdapter";
    private Context context;
    private String currency;
    private String theme;

    protected MonthlyReportsAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<MonthlyReportsFragment.MonthlyReport> DIFF_CALLBACK = new DiffUtil.ItemCallback<MonthlyReportsFragment.MonthlyReport>() {
        @Override
        public boolean areItemsTheSame(@NonNull MonthlyReportsFragment.MonthlyReport oldItem, @NonNull MonthlyReportsFragment.MonthlyReport newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MonthlyReportsFragment.MonthlyReport oldItem, @NonNull MonthlyReportsFragment.MonthlyReport newItem) {
            return false;
        }
    };

    @NonNull
    @Override
    public ReportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_monthly_report, parent, false);
        return new ReportHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportHolder holder, int position) {
        MonthlyReportsFragment.MonthlyReport monthlyReport = getItem(position);
        context = holder.itemView.getContext();
        SharedPreferences sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
        theme = sharedPref.getString("theme", "light");

        //Strings
        holder.tvMonth.setText(monthlyReport.getMonth());
        holder.tvIncome.setText(monthlyReport.getIncome());
        holder.tvExpenses.setText(monthlyReport.getExpenses());
        holder.tvBudget.setText(monthlyReport.getBudget());
        holder.tvBudgetLeft.setText(monthlyReport.getBudgetLeft());
        if (monthlyReport.getBudgetLeft().contains("-"))
            holder.tvBudgetLeft.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        else
            holder.tvBudgetLeft.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
        holder.tvHighestExpense.setText(monthlyReport.getHighestExpense());
        holder.tvHighestItem.setText(monthlyReport.getHighestItem());
        if (!monthlyReport.getIncomeDiff().contains("null")
                && !monthlyReport.getIncomeDiff().equals("(+.00)") && !monthlyReport.getIncomeDiff().equals("(-.00)")) {
            holder.tvIncomeDiff.setVisibility(View.VISIBLE);
            holder.tvIncomeDiff.setText(monthlyReport.getIncomeDiff());
            if (monthlyReport.getIncomeDiff().contains("+"))
                holder.tvIncomeDiff.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            else
                holder.tvIncomeDiff.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        } else
            holder.tvIncomeDiff.setVisibility(View.GONE);
        if (!monthlyReport.getExpensesDiff().contains("null")
                && !monthlyReport.getExpensesDiff().equals("(+.00)") && !monthlyReport.getExpensesDiff().equals("(-.00)")) {
            holder.tvExpensesDiff.setVisibility(View.VISIBLE);
            holder.tvExpensesDiff.setText(monthlyReport.getExpensesDiff());
            if (monthlyReport.getExpensesDiff().contains("+-")) { //that weird thing of 'today'
                String diff = monthlyReport.getExpensesDiff().replace("+", "");
                holder.tvExpensesDiff.setText(diff);
            }
            if (monthlyReport.getExpensesDiff().contains("-"))
                holder.tvExpensesDiff.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            else
                holder.tvExpensesDiff.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        } else
            holder.tvExpensesDiff.setVisibility(View.GONE);
        holder.tvAverageIncome.setText(monthlyReport.getAverageIncome());
        holder.tvAverageExpense.setText(monthlyReport.getAverageExpenses());
        if (monthlyReport.getMostIncomeDay() != null)
            holder.tvMostIncome.setText(monthlyReport.getMostIncomeDay());
        else
            holder.tvMostIncome.setText(null);
        if (monthlyReport.getMostExpenseDay() != null)
            holder.tvMostExpense.setText(monthlyReport.getMostExpenseDay());
        else
            holder.tvMostExpense.setText(null);

        //charts
        if (monthlyReport.getIncome() != null || monthlyReport.getExpenses() != null)
            drawIncomeExpenseChart(holder.chartInEx, monthlyReport);
        if (new Amount(context, monthlyReport.getBudget()).getAmountValue() != 0)
            drawBudgetChart(holder.chartBudget, monthlyReport);
        drawDailyChart(holder.chartDaily, monthlyReport);
    }

    private void drawBudgetChart(HorizontalBarChart chartView, MonthlyReportsFragment.MonthlyReport monthlyReport) {
        List<BarEntry> budgetUsed = new ArrayList<>();
        double budget = Double.parseDouble(monthlyReport.getBudget().replace(currency, ""));
        double expenses = Double.parseDouble(monthlyReport.getExpenses().replace(currency, ""));
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
        chartView.animateY(1000, Easing.EaseOutCirc);
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

    private void drawIncomeExpenseChart(HorizontalBarChart chartView, MonthlyReportsFragment.MonthlyReport monthlyReport) {
        List<BarEntry> incomes = new ArrayList<>();
        List<BarEntry> expenses = new ArrayList<>();
        incomes.add(new BarEntry(1f, Float.parseFloat(monthlyReport.getIncome().replace(currency, ""))));
        expenses.add(new BarEntry(2f, Float.parseFloat(monthlyReport.getExpenses().replace(currency, ""))));
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
        chartView.animateY(1000, Easing.EaseOutCirc);
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

    private void drawDailyChart(LineChart chartView, MonthlyReportsFragment.MonthlyReport monthlyReport) {
        List<Entry> dailyIncomes = new ArrayList<>();
        List<String> dailyIncomesList = monthlyReport.getIncomesOfMonth();
        for (int x = 0; x < dailyIncomesList.size(); x++) {
            Entry entry = new Entry((float) x, Float.parseFloat(dailyIncomesList.get(x)));
            dailyIncomes.add(entry);
        }
        LineDataSet incomesDataSet = new LineDataSet(dailyIncomes, context.getResources().getString(R.string.income));
        incomesDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        incomesDataSet.setDrawValues(false);
        incomesDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        incomesDataSet.setColor(context.getResources().getColor(R.color.colorLightBlue));

        List<Entry> dailyExpenses = new ArrayList<>();
        List<String> dailyExpensesList = monthlyReport.getExpensesOfMonth();
        for (int x = 0; x < dailyExpensesList.size(); x++) {
            Entry entry = new Entry((float) x, Float.parseFloat(dailyExpensesList.get(x)));
            dailyExpenses.add(entry);
        }
        LineDataSet expensesDataSet = new LineDataSet(dailyExpenses, context.getResources().getString(R.string.expenses));
        expensesDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        expensesDataSet.setDrawValues(false);
        expensesDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        expensesDataSet.setColor(context.getResources().getColor(R.color.colorLightRed));

        List<ILineDataSet> dataSetList = new ArrayList<>();
        dataSetList.add(incomesDataSet);
        dataSetList.add(expensesDataSet);
        LineData data = new LineData(dataSetList);
        chartView.animateY(1000, Easing.EaseOutCirc);
        chartView.getDescription().setEnabled(false);
        chartView.getXAxis().setEnabled(false);
        chartView.getXAxis().setAxisLineColor(context.getResources().getColor(R.color.colorDividerLight));
        chartView.getAxisLeft().setAxisLineColor(context.getResources().getColor(R.color.colorDividerLight));
        chartView.getAxisLeft().setTextColor(context.getResources().getColor(R.color.colorDivider));
        chartView.getAxisLeft().setGridColor(context.getResources().getColor(R.color.colorDividerLight));
        chartView.getAxisRight().setTextColor(context.getResources().getColor(R.color.colorDivider));
        chartView.getAxisRight().setAxisLineColor(context.getResources().getColor(R.color.colorDividerLight));
        chartView.getAxisRight().setGridColor(context.getResources().getColor(R.color.colorDividerLight));
        chartView.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        chartView.getLegend().setTextColor(context.getResources().getColor(R.color.colorDivider));
        chartView.setData(data);
        chartView.invalidate();
    }

    static class ReportHolder extends RecyclerView.ViewHolder {
        private TextView tvMonth;
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
        private LineChart chartDaily;
        private TextView tvAverageIncome;
        private TextView tvAverageExpense;
        private TextView tvMostIncome;
        private TextView tvMostExpense;

        public ReportHolder(@NonNull View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.month);
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
            chartDaily = itemView.findViewById(R.id.chartDaily);
            tvAverageIncome = itemView.findViewById(R.id.avIncome);
            tvAverageExpense = itemView.findViewById(R.id.avExpense);
            tvMostIncome = itemView.findViewById(R.id.mostIncome);
            tvMostExpense = itemView.findViewById(R.id.mostExpense);
        }
    }
}
