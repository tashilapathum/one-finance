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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class DailyReportsAdapter extends ListAdapter<DailyReportsFragment.DailyReport, DailyReportsAdapter.ReportHolder> {
    private SharedPreferences sharedPref;

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
        Context context = holder.itemView.getContext();
        sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String currency = sharedPref.getString("currency", "");

        //Strings
        holder.tvDate.setText(dailyReport.getDate());
        holder.tvIncome.setText(dailyReport.getIncome());
        holder.tvExpenses.setText(dailyReport.getExpenses());
        holder.tvBudget.setText(dailyReport.getBudget());
        holder.tvBudgetLeft.setText(dailyReport.getBudgetLeft());
        if (dailyReport.getBudgetLeft().contains("-"))
            holder.tvBudgetLeft.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        holder.tvHighestExpense.setText(dailyReport.getHighestExpense());
        holder.tvHighestItem.setText(dailyReport.getHighestItem());
        if (!dailyReport.getIncomeDiff().equals("(+.00)")) {
            holder.tvIncomeDiff.setVisibility(View.VISIBLE);
            holder.tvIncomeDiff.setText(dailyReport.getIncomeDiff());
            if (dailyReport.getIncomeDiff().contains("+"))
                holder.tvIncomeDiff.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            else
                holder.tvIncomeDiff.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        }
        if (!dailyReport.getExpensesDiff().equals("(+.00)")) {
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

        //charts
        List<BarEntry> incomes = new ArrayList<>();
        List<BarEntry> expenses = new ArrayList<>();
        incomes.add(new BarEntry(1f, Float.parseFloat(dailyReport.getIncome().replace(currency, ""))));
        expenses.add(new BarEntry(2f, Float.parseFloat(dailyReport.getExpenses().replace(currency, ""))));
        BarDataSet expensesSet = new BarDataSet(expenses, "Expenses");
        expensesSet.setColor(context.getResources().getColor(R.color.colorRed));
        BarDataSet incomesSet = new BarDataSet(incomes, "Income");
        incomesSet.setColor(context.getResources().getColor(R.color.colorBlue));
        BarData data = new BarData(expensesSet, incomesSet);
        holder.chartInEx.setData(data);
        holder.chartInEx.getAxisLeft().setDrawGridLines(false);
        holder.chartInEx.getAxisRight().setDrawGridLines(false);
        holder.chartInEx.getXAxis().setDrawGridLines(false);
        holder.chartInEx.getAxisLeft().setDrawAxisLine(false);
        holder.chartInEx.getAxisRight().setDrawAxisLine(false);
        holder.chartInEx.getXAxis().setDrawAxisLine(false);
        holder.chartInEx.getAxisRight().setDrawLabels(false);
        holder.chartInEx.getXAxis().setDrawLabels(false);
        holder.chartInEx.animateY(250, Easing.EaseInOutSine);
        holder.chartInEx.getDescription().setEnabled(false);
        holder.chartInEx.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        holder.chartInEx.getLegend().setTextColor(R.color.colorDivider);
        holder.chartInEx.getAxisLeft().setTextColor(R.color.colorDivider);
        holder.chartInEx.getAxisRight().setTextColor(R.color.colorDivider);
        holder.chartInEx.getBarData().setDrawValues(false);
        holder.chartInEx.invalidate();
    }

    class ReportHolder extends RecyclerView.ViewHolder {
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
