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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tantalum.onefinance.Amount;
import com.tantalum.onefinance.AnimationHandler;
import com.tantalum.onefinance.DateTimeHandler;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.pro.UpgradeToProActivity;
import com.tantalum.onefinance.transactions.TransactionItem;
import com.tantalum.onefinance.transactions.TransactionsViewModel;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class DailyReportsFragment extends Fragment {
    public static final String TAG = "DailyReportsFragment";
    private SharedPreferences sharedPref;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DailyReportsAdapter adapter;
    private List<DailyReport> dailyReportList;
    private int day;
    private int dayCount;
    private int pickedDay;
    private int pickedYear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_reports, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        Bundle bundle = this.getArguments();
        boolean isFromWallet = false;
        if (bundle != null)
            isFromWallet = bundle.getBoolean("fromWallet");

        dailyReportList = new ArrayList<>();
        pickedDay = sharedPref.getInt("reports_day", 0);
        pickedYear = sharedPref.getInt("reports_year", 0);
        if (pickedDay != 0)
            day = pickedDay;
        else {
            day = LocalDate.now().getDayOfYear();
            pickedYear = LocalDate.now().getYear();
        }
        dayCount = 0;

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DailyReportsAdapter(isFromWallet);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutAnimation(new AnimationHandler().getSlideUpController());
        recyclerView.setAdapter(adapter);

        calculateDailyReport(day); //show only 1 if from wallet
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
        calculateDailyReport(day);
        calculateDailyReport(day);
        calculateDailyReport(day);
        calculateDailyReport(day);
        calculateDailyReport(day);
        calculateDailyReport(day);
        calculateDailyReport(day);
        calculateDailyReport(day);
        calculateDailyReport(day);
        calculateDailyReport(day);
    }

    private void calculateDailyReport(int day) {
        //for new year
        if (LocalDate.now().isLeapYear()) {
            if (day > 366) {
                day = 1;
                pickedYear--;
            }
        }
        else if (day > 365) {
            day = 1;
            pickedYear--;
        }
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(getActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        List<TransactionItem> transactionsList = transactionsViewModel.getTransactionsList();

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
        String date = formatter.format(LocalDate.ofYearDay(pickedYear, day));
        if (day == LocalDate.now().getDayOfYear())
            date = getString(R.string.today) + "\n" + date;
        if (day == LocalDate.now().getDayOfYear() - 1)
            date = getString(R.string.yesterday) + "\n" + date;
        double income = 0;
        double expenses = 0;
        double highestExpense = 0;
        String highestItem = null;
        for (int i = 0; i < transactionsList.size(); i++) {
            TransactionItem currentTransaction = transactionsList.get(i);
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getTimeInMillis());
            int transactionDate = dateTimeHandler.getDayOfYear();
            if (transactionDate == day) {
                if (currentTransaction.getPrefix().equals("+"))
                    income = income + Double.parseDouble(currentTransaction.getAmount());
                else {
                    if (currentTransaction.getAmountValue() > highestExpense) {
                        highestExpense = currentTransaction.getAmountValue();
                        highestItem = currentTransaction.getDescription();
                    }
                    expenses = expenses + Double.parseDouble(currentTransaction.getAmount());
                }
            }
        }
        //to calculate differences
        double incomeOLD = 0;
        double expensesOLD = 0;
        for (int i = 0; i < transactionsList.size(); i++) {
            TransactionItem currentTransaction = transactionsList.get(i);
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getTimeInMillis());
            int transactionDate = dateTimeHandler.getDayOfYear();
            if (transactionDate == LocalDate.now().minusDays(dayCount + 1).getDayOfYear()) {
                if (currentTransaction.getPrefix().equals("+"))
                    incomeOLD = incomeOLD + Double.parseDouble(currentTransaction.getAmount());
                if (currentTransaction.getPrefix().equals("-"))
                    expensesOLD = expensesOLD + Double.parseDouble(currentTransaction.getAmount());
            }
        }
        double incomeDiff = income - incomeOLD;
        double expensesDiff = expenses - expensesOLD;

        String monthlyBudgetStr = sharedPref.getString("monthlyBudget", "N/A");
        DecimalFormat df = new DecimalFormat("#.00");
        double monthlyBudget = 0;
        if (!monthlyBudgetStr.equals("N/A")) {
            monthlyBudget = Double.parseDouble(monthlyBudgetStr);
        }
        double budget = monthlyBudget / YearMonth.now().lengthOfMonth();
        double budgetLeft = budget - expenses;
        //to show (+) for positive values
        String incomeDiffStr = null;
        if (!df.format(incomeDiff).contains("-"))
            incomeDiffStr = "+" + df.format(incomeDiff);
        String expensesDiffStr = null;
        if (!df.format(incomeDiff).contains("-"))
            expensesDiffStr = "+" + df.format(expensesDiff);

        DailyReport dailyReport = new DailyReport(date,
                new Amount(getActivity(), income).getAmountString(),
                new Amount(getActivity(), expenses).getAmountString(),
                new Amount(getActivity(), budget).getAmountString(),
                new Amount(getActivity(), budgetLeft).getAmountString(),
                new Amount(getActivity(), highestExpense).getAmountString(), highestItem,
                "(" + incomeDiffStr + ")",
                "(" + expensesDiffStr + ")");

        //to load next cards
        //for new year
        if (LocalDate.now().isLeapYear()) {
            if (dayCount < 366)
                dayCount++;
            else {
                day = 1;
            }
        }
        else {
            if (dayCount < 365)
                dayCount++;
            else {
                day = 1;
            }
        }

        if (pickedDay != 0)
            this.day = LocalDate.ofYearDay(pickedYear, pickedDay).minusDays(dayCount).getDayOfYear();
        else
            this.day = LocalDate.now().minusDays(dayCount).getDayOfYear();

        dailyReportList.add(dailyReport);
        adapter.submitList(dailyReportList);
        adapter.notifyItemInserted(adapter.getItemCount() + 1);
    }

    public void showUpdatedReports() {
        dailyReportList = new ArrayList<>();
        adapter.submitList(dailyReportList);
        day = LocalDate.now().getDayOfYear();
        pickedYear = LocalDate.now().getYear();
        calculateDailyReport(day);
    }

    static class DailyReport {
        final private String date;
        final private String income;
        final private String expenses;
        final private String budget;
        final private String budgetLeft;
        final private String highestExpense;
        final private String highestItem;
        final private String incomeDiff;
        final private String expensesDiff;

        public DailyReport(String date, String income, String expenses, String budget, String budgetLeft,
                           String highestExpense, String highestItem, String incomeDiff, String expensesDiff) {
            this.income = income;
            this.expenses = expenses;
            this.budget = budget;
            this.budgetLeft = budgetLeft;
            this.highestExpense = highestExpense;
            this.highestItem = highestItem;
            this.incomeDiff = incomeDiff;
            this.expensesDiff = expensesDiff;
            this.date = date;
        }

        public String getDate() {
            return date;
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
    }
}