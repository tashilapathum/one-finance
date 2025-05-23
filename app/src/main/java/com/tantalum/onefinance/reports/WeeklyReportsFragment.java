package com.tantalum.onefinance.reports;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tantalum.onefinance.Amount;
import com.tantalum.onefinance.AnimationHandler;
import com.tantalum.onefinance.DateTimeHandler;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.transactions.TransactionItem;
import com.tantalum.onefinance.transactions.TransactionsViewModel;
import com.tantalum.onefinance.pro.UpgradeToProActivity;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeeklyReportsFragment extends Fragment {
    public static final String TAG = "WeeklyReportsFragment";
    private SharedPreferences sharedPref;
    private String currency;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private WeeklyReportsAdapter adapter;
    private List<WeeklyReport> weeklyReportList;
    private int week;
    private int weekCount;
    private int yearCount;
    private int year;
    private WeekFields weekFields;
    private int pickedWeek;
    private int pickedYear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_weekly_reports, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        Bundle bundle = this.getArguments();
        boolean isFromWallet = false;
        if (bundle != null)
            isFromWallet = bundle.getBoolean("fromWallet");

        weeklyReportList = new ArrayList<>();
        weekFields = WeekFields.of(Locale.getDefault());
        pickedWeek = sharedPref.getInt("reports_week", 0);
        if (pickedWeek != 0)
            week = pickedWeek;
        else
            week = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
        pickedYear = sharedPref.getInt("reports_year", 0);
        if (pickedYear != 0)
            year = pickedYear;
        else
            year = LocalDate.now().getYear();
        weekCount = 0;
        yearCount = 0;
        if (pickedWeek != 0)
            yearCount = LocalDate.now().getYear() - pickedYear;
        Log.i(TAG, "yearCount: " + yearCount);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new WeeklyReportsAdapter(isFromWallet);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutAnimation(new AnimationHandler().getSlideUpController());
        recyclerView.setAdapter(adapter);
        calculateWeeklyReport(week, year);
        if (!isFromWallet) {
            loadMultipleReports();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!recyclerView.canScrollVertically(1))
                        loadMultipleReports();
                }
            });
        }
        return view;
    }

    private void loadMultipleReports() {
        calculateWeeklyReport(week, year);
        calculateWeeklyReport(week, year);
        calculateWeeklyReport(week, year);
        calculateWeeklyReport(week, year);
        calculateWeeklyReport(week, year);
        calculateWeeklyReport(week, year);
        calculateWeeklyReport(week, year);
        calculateWeeklyReport(week, year);
        calculateWeeklyReport(week, year);
        calculateWeeklyReport(week, year);
    }

    private void calculateWeeklyReport(int week, int year) {
        currency = sharedPref.getString("currency", "");
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(getActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        List<TransactionItem> transactionsList = transactionsViewModel.getTransactionsList();
        String weekTitle;

        //card title
        weekTitle = getString(R.string.week) + week
                + "\n"
                + LocalDate.now()
                .minusYears(yearCount)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(TemporalAdjusters.previousOrSame(weekFields.getFirstDayOfWeek()))
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                + " - "
                + LocalDate.now()
                .minusYears(yearCount)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(TemporalAdjusters.previousOrSame(weekFields.getFirstDayOfWeek()))
                .plusDays(6)
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
        if (week == LocalDate.now().get(weekFields.weekOfWeekBasedYear()) && year == LocalDate.now().getYear())
            weekTitle = getString(R.string.re_this_week) + weekTitle.replace((getString(R.string.week) + week), "");
        if (week == LocalDate.now().minusWeeks(1).get(weekFields.weekOfWeekBasedYear()) && year == LocalDate.now().getYear())
            weekTitle = getString(R.string.re_last_week) + weekTitle.replace((getString(R.string.week) + week), "");

        //daily data
        double income = 0;
        double expenses = 0;
        double highestExpense = 0;
        String highestItem = "";
        List<String> dailyIncomes = new ArrayList<>(7);
        List<String> dailyExpenses = new ArrayList<>(7);
        for (int x = 0; x < 7; x++) {
            dailyIncomes.add("0");
            dailyExpenses.add("0");
        }
        for (int i = 0; i < transactionsList.size(); i++) {
            TransactionItem currentTransaction = transactionsList.get(i);
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getTimeInMillis());
            int transactionYear = dateTimeHandler.getYear();
            int transactionWeek = dateTimeHandler.getWeekOfYear();
            int transactionDay = dateTimeHandler.getDayOfWeek();
            if (transactionWeek == week && transactionYear == year) {
                if (currentTransaction.getPrefix().equals("+"))
                    income = income + Double.parseDouble(currentTransaction.getAmount());
                else {
                    if (currentTransaction.getAmountValue() > highestExpense) {
                        highestExpense = currentTransaction.getAmountValue();
                        highestItem = currentTransaction.getDescription();
                    }
                    expenses = expenses + Double.parseDouble(currentTransaction.getAmount());
                }

                for (int d = 0; d < 7; d++) {
                    if (d + 1 == transactionDay) {
                        if (currentTransaction.getPrefix().equals("+")) {
                            double dailyTotal = Double.parseDouble(dailyIncomes.get(d)) + currentTransaction.getAmountValue();
                            dailyIncomes.add(d, "" + dailyTotal);
                        } else {
                            double dailyTotal = Double.parseDouble(dailyExpenses.get(d)) + currentTransaction.getAmountValue();
                            dailyExpenses.add(d, "" + dailyTotal);
                        }
                    }
                }
            }
        }

        //to calculate differences
        double incomeOLD = 0;
        double expensesOLD = 0;
        for (
                int i = 0; i < transactionsList.size(); i++) {
            TransactionItem currentTransaction = transactionsList.get(i);
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getTimeInMillis());
            int transactionYear = dateTimeHandler.getYear();
            int transactionWeek = dateTimeHandler.getWeekOfYear();
            if (transactionWeek == week - 1 && transactionYear == year) {
                if (currentTransaction.getPrefix().equals("+"))
                    incomeOLD = incomeOLD + Double.parseDouble(currentTransaction.getAmount());
                if (currentTransaction.getPrefix().equals("-"))
                    expensesOLD = expensesOLD + Double.parseDouble(currentTransaction.getAmount());
            }
        }

        double incomeDiff = income - incomeOLD;
        double expensesDiff = expenses - expensesOLD;

        //budget
        String monthlyBudgetStr = sharedPref.getString("monthlyBudget", "N/A");
        DecimalFormat df = new DecimalFormat("#.00");
        double monthlyBudget = 0;
        if (!monthlyBudgetStr.equals("N/A")) {
            monthlyBudget = Double.parseDouble(monthlyBudgetStr);
        }

        double budget = monthlyBudget / 4;
        double budgetLeft = budget - expenses;
        //to show (+) for positive values
        String incomeDiffStr = null;
        if (!df.format(incomeDiff).contains("-"))
            incomeDiffStr = "+" + df.format(incomeDiff);
        String expensesDiffStr = null;
        if (!df.format(incomeDiff).contains("-"))
            expensesDiffStr = "+" + df.format(expensesDiff);

        //find most income and most expense day
        int mostIncomeIndex = -1;
        int mostExpenseIndex = -1;
        double tempMostIncome = 0;
        double tempMostExpense = 0;
        for (int i = 0; i < 7; i++) {
            if (Double.parseDouble(dailyIncomes.get(i)) > tempMostIncome) {
                mostIncomeIndex = i;
                tempMostIncome = Double.parseDouble(dailyIncomes.get(i));
            }

            if (Double.parseDouble(dailyExpenses.get(i)) > tempMostExpense) {
                mostExpenseIndex = i;
                tempMostExpense = Double.parseDouble(dailyExpenses.get(i));
            }
        }

        String mostIncomeDay = null;
        String mostExpenseDay = null;
        if (mostIncomeIndex != -1) {
            mostIncomeDay = LocalDate.now().with(TemporalAdjusters.previousOrSame(weekFields.getFirstDayOfWeek()))
                    .plusDays(mostIncomeIndex).getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
        }
        if (mostExpenseIndex != -1) {
            mostExpenseDay = LocalDate.now().with(TemporalAdjusters.previousOrSame(weekFields.getFirstDayOfWeek()))
                    .plusDays(mostExpenseIndex).getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
        }
        //capitalize
        if (mostIncomeDay != null)
            mostIncomeDay = mostIncomeDay.substring(0, 1).toUpperCase() + mostIncomeDay.substring(1).toLowerCase();
        if (mostExpenseDay != null)
            mostExpenseDay = mostExpenseDay.substring(0, 1).toUpperCase() + mostExpenseDay.substring(1).toLowerCase();

        WeeklyReport weeklyReport = new WeeklyReport(weekTitle,
                new Amount(getActivity(), income).getAmountString(),
                new Amount(getActivity(), expenses).getAmountString(),
                new Amount(getActivity(), budget).getAmountString(),
                new Amount(getActivity(), budgetLeft).getAmountString(),
                new Amount(getActivity(), highestExpense).getAmountString(), highestItem,
                "(" + incomeDiffStr + ")",
                "(" + expensesDiffStr + ")",
                dailyIncomes, dailyExpenses,
                mostIncomeDay + " (" + new Amount(getActivity(), tempMostIncome).getAmountString() + ")",
                mostExpenseDay + " (" + new Amount(getActivity(), tempMostExpense).getAmountString() + ")");

        //to load next cards
        weekCount++;
        if (this.week == 1) {
            yearCount++;
            this.year = this.year - yearCount;
        }
        if (pickedWeek != 0)
            this.week = LocalDate.now().with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, pickedWeek).minusWeeks(weekCount)
                    .get(weekFields.weekOfWeekBasedYear());
        else
            this.week = LocalDate.now().minusYears(yearCount).minusWeeks(weekCount)
                    .get(weekFields.weekOfWeekBasedYear());
        weeklyReportList.add(weeklyReport);
        adapter.submitList(weeklyReportList);
        adapter.notifyItemInserted(adapter.getItemCount() + 1);
    }

    public void showUpdatedReports() {
        weeklyReportList = new ArrayList<>();
        adapter.submitList(weeklyReportList);
        week = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
        pickedYear = LocalDate.now().getYear();
        calculateWeeklyReport(week, pickedYear);
    }

    static class WeeklyReport {
        private final String week;
        private final String income;
        private final String expenses;
        private final String budget;
        private final String budgetLeft;
        private final String highestExpense;
        private final String highestItem;
        private final String incomeDiff;
        private final String expensesDiff;
        private final List<String> dailyIncomes;
        private final List<String> dailyExpenses;
        private final String mostIncomeDay;
        private final String mostExpenseDay;

        public WeeklyReport(String week, String income, String expenses, String budget, String budgetLeft,
                            String highestExpense, String highestItem, String incomeDiff, String expensesDiff,
                            List<String> dailyIncomes, List<String> dailyExpenses, String mostIncomeDay, String mostExpenseDay) {
            this.income = income;
            this.expenses = expenses;
            this.budget = budget;
            this.budgetLeft = budgetLeft;
            this.highestExpense = highestExpense;
            this.highestItem = highestItem;
            this.incomeDiff = incomeDiff;
            this.expensesDiff = expensesDiff;
            this.week = week;
            this.dailyIncomes = dailyIncomes;
            this.dailyExpenses = dailyExpenses;
            this.mostIncomeDay = mostIncomeDay;
            this.mostExpenseDay = mostExpenseDay;
        }

        public String getWeek() {
            return week;
        }

        public String getIncome() {
            return income;
        }

        public String getExpenses() {
            return expenses;
        }

        public String getBudget() {
            return budget;
        }

        public String getBudgetLeft() {
            return budgetLeft;
        }

        public String getHighestExpense() {
            return highestExpense;
        }

        public String getHighestItem() {
            return highestItem;
        }

        public String getIncomeDiff() {
            return incomeDiff;
        }

        public String getExpensesDiff() {
            return expensesDiff;
        }

        public List<String> getDailyIncomes() {
            return dailyIncomes;
        }

        public List<String> getDailyExpenses() {
            return dailyExpenses;
        }

        public String getMostIncomeDay() {
            return mostIncomeDay;
        }

        public String getMostExpenseDay() {
            return mostExpenseDay;
        }

    }

    public void purchaseProForThis() {
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.pro_feature)
                .setMessage(R.string.buy_the_pro_version_to_see_more_into_the_history)
                .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), UpgradeToProActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}