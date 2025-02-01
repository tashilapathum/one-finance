package com.tantalum.onefinance.reports;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.tantalum.onefinance.Amount;
import com.tantalum.onefinance.R;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeeklyReportsAdapter extends ListAdapter<WeeklyReportsFragment.WeeklyReport, WeeklyReportsAdapter.ReportHolder> {
    public static final String TAG = "WeeklyReportsAdapter";
    private Context context;
    private String currency;
    private String theme;
    private boolean isFromWallet;

    protected WeeklyReportsAdapter(boolean isFromWallet) {
        super(DIFF_CALLBACK);
        this.isFromWallet = isFromWallet;
    }

    private static final DiffUtil.ItemCallback<WeeklyReportsFragment.WeeklyReport> DIFF_CALLBACK = new DiffUtil.ItemCallback<WeeklyReportsFragment.WeeklyReport>() {
        @Override
        public boolean areItemsTheSame(@NonNull WeeklyReportsFragment.WeeklyReport oldItem, @NonNull WeeklyReportsFragment.WeeklyReport newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull WeeklyReportsFragment.WeeklyReport oldItem, @NonNull WeeklyReportsFragment.WeeklyReport newItem) {
            return false;
        }
    };

    @NonNull
    @Override
    public ReportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_weekly_report, parent, false);
        return new ReportHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportHolder holder, int position) {
        WeeklyReportsFragment.WeeklyReport weeklyReport = getItem(position);
        context = holder.itemView.getContext();
        SharedPreferences sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
        theme = sharedPref.getString("theme", "light");

        //Strings
        holder.tvWeek.setText(weeklyReport.getWeek());
        holder.tvIncome.setText(weeklyReport.getIncome());
        holder.tvExpenses.setText(weeklyReport.getExpenses());
        holder.tvBudget.setText(weeklyReport.getBudget());
        holder.tvBudgetLeft.setText(weeklyReport.getBudgetLeft());
        if (weeklyReport.getBudgetLeft().contains("-"))
            holder.tvBudgetLeft.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        else
            holder.tvBudgetLeft.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
        holder.tvHighestExpense.setText(weeklyReport.getHighestExpense());
        holder.tvHighestItem.setText(weeklyReport.getHighestItem());
        if (!weeklyReport.getIncomeDiff().contains("null")
                && !weeklyReport.getIncomeDiff().equals("(+.00)") && !weeklyReport.getIncomeDiff().equals("(-.00)")) {
            holder.tvIncomeDiff.setVisibility(View.VISIBLE);
            holder.tvIncomeDiff.setText(weeklyReport.getIncomeDiff());
            if (weeklyReport.getIncomeDiff().contains("+"))
                holder.tvIncomeDiff.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            else
                holder.tvIncomeDiff.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        } else
            holder.tvIncomeDiff.setVisibility(View.GONE);
        if (!weeklyReport.getExpensesDiff().contains("null")
                && !weeklyReport.getExpensesDiff().equals("(+.00)") && !weeklyReport.getExpensesDiff().equals("(-.00)")) {
            holder.tvExpensesDiff.setVisibility(View.VISIBLE);
            holder.tvExpensesDiff.setText(weeklyReport.getExpensesDiff());
            if (weeklyReport.getExpensesDiff().contains("+-")) { //that weird thing of 'today'
                String diff = weeklyReport.getExpensesDiff().replace("+", "");
                holder.tvExpensesDiff.setText(diff);
            }
            if (weeklyReport.getExpensesDiff().contains("-"))
                holder.tvExpensesDiff.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            else
                holder.tvExpensesDiff.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        } else
            holder.tvExpensesDiff.setVisibility(View.GONE);
        holder.tvMostIncome.setText(weeklyReport.getMostIncomeDay().replace("null", "N/A"));
        holder.tvMostExpense.setText(weeklyReport.getMostExpenseDay().replace("null", "N/A"));

        //charts
        if (weeklyReport.getIncome() != null || weeklyReport.getExpenses() != null)
            drawIncomeExpenseChart(holder.chartInEx, weeklyReport);
        if (new Amount(context, weeklyReport.getBudget()).getAmountValue() != 0)
            drawBudgetChart(holder.chartBudget, weeklyReport);
        if (weeklyReport.getWeek().contains(context.getString(R.string.r_this_week)))
            holder.daily_data_layout.setVisibility(View.GONE);
        drawDailyDetailsChart(holder.chartDaily, weeklyReport);
    }

    private void drawBudgetChart(HorizontalBarChart chartView, WeeklyReportsFragment.WeeklyReport weeklyReport) {
        List<BarEntry> budgetUsed = new ArrayList<>();
        double budget = Double.parseDouble(weeklyReport.getBudget().replace(currency, ""));
        double expenses = Double.parseDouble(weeklyReport.getExpenses().replace(currency, ""));
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

    private void drawIncomeExpenseChart(HorizontalBarChart chartView, WeeklyReportsFragment.WeeklyReport weeklyReport) {
        List<BarEntry> incomes = new ArrayList<>();
        List<BarEntry> expenses = new ArrayList<>();
        incomes.add(new BarEntry(1f, Float.parseFloat(weeklyReport.getIncome().replace(currency, ""))));
        expenses.add(new BarEntry(2f, Float.parseFloat(weeklyReport.getExpenses().replace(currency, ""))));
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

    private void drawDailyDetailsChart(BarChart chartView, WeeklyReportsFragment.WeeklyReport weeklyReport) {
        List<BarEntry> dailyDetails = new ArrayList<>();
        for (int i = 0; i < 7; i++)
            dailyDetails.add(new BarEntry(
                    (float) i, new float[]{
                    Float.parseFloat(weeklyReport.getDailyIncomes().get(i)),
                    Float.parseFloat(weeklyReport.getDailyExpenses().get(i))
            }));
        BarDataSet dailyDetailsSet = new BarDataSet(dailyDetails, "");
        String[] labels = {context.getString(R.string.incomes), context.getString(R.string.expenses)};
        //add x axis labels (days of week)
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int firstDay = weekFields.getFirstDayOfWeek().getValue();
        final List<String> xLabels = new ArrayList<>();
        for (int x = 0; x < 7; x++)
            xLabels.add(LocalDate.now().with(DayOfWeek.of(x + 1)).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));

        dailyDetailsSet.setColors(
                context.getResources().getColor(android.R.color.holo_green_light),
                context.getResources().getColor(android.R.color.holo_orange_dark));
        dailyDetailsSet.setStackLabels(labels);
        BarData data = new BarData(dailyDetailsSet);
        chartView.setData(data);
        chartView.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return xLabels.get((int) value);
            }
        });
        chartView.getXAxis().setTextColor(context.getResources().getColor(R.color.colorDivider));
        chartView.getAxisLeft().setDrawGridLines(false);
        chartView.getAxisRight().setDrawGridLines(false);
        chartView.getXAxis().setDrawGridLines(false);
        chartView.getAxisLeft().setDrawAxisLine(false);
        chartView.getAxisRight().setDrawAxisLine(false);
        chartView.getXAxis().setDrawAxisLine(false);
        chartView.getAxisRight().setDrawLabels(false);
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

    static class ReportHolder extends RecyclerView.ViewHolder {
        private final TextView tvWeek;
        private final TextView tvIncome;
        private final TextView tvExpenses;
        private final TextView tvBudget;
        private final TextView tvBudgetLeft;
        private final TextView tvHighestExpense;
        private final TextView tvHighestItem;
        private final TextView tvIncomeDiff;
        private final TextView tvExpensesDiff;
        private final HorizontalBarChart chartInEx;
        private final HorizontalBarChart chartBudget;
        private final TextView tvMostIncome;
        private final TextView tvMostExpense;
        private final BarChart chartDaily;
        private final LinearLayout daily_data_layout;

        public ReportHolder(@NonNull View itemView) {
            super(itemView);
            tvWeek = itemView.findViewById(R.id.week);
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
            tvMostIncome = itemView.findViewById(R.id.mostIncome);
            tvMostExpense = itemView.findViewById(R.id.mostExpense);
            chartDaily = itemView.findViewById(R.id.chartDaily);
            daily_data_layout = itemView.findViewById(R.id.daily_data_layout);
        }
    }
}
