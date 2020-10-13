package com.tashila.mywalletfree;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private String currency;
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
        dailyReportList = new ArrayList<>();
        pickedDay = sharedPref.getInt("reports_day", 0);
        pickedYear = sharedPref.getInt("reports_year", 0);
        if (pickedDay != 0)
            day = pickedDay;
        else
            day = LocalDate.now().getDayOfYear();
        dayCount = 0;
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DailyReportsAdapter();
        recyclerView.setLayoutAnimation(new AnimationHandler().getSlideUpController());
        recyclerView.setAdapter(adapter);
        calculateDailyReport(day);
        calculateDailyReport(day);
        calculateDailyReport(day);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1))
                    if (sharedPref.getBoolean("MyWalletPro", false))
                        calculateDailyReport(day);
                    else
                        purchaseProForThis();
            }
        });
        return view;
    }

    private void calculateDailyReport(int day) {
        currency = sharedPref.getString("currency", "");
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(getActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        List<TransactionItem> transactionsList = transactionsViewModel.getTransactionsList();
        String date;

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
        date = formatter.format(LocalDate.ofYearDay(LocalDate.now().getYear(), day));
        if (day == LocalDate.now().getDayOfYear())
            date = getString(R.string.re_today) + date + ")";
        if (day == LocalDate.now().getDayOfYear() - 1)
            date = getString(R.string.re_yesterday) + date + ")";
        double income = 0;
        double expenses = 0;
        double highestExpense = 0;
        String highestItem = null;
        for (int i = 0; i < transactionsList.size(); i++) {
            TransactionItem currentTransaction = transactionsList.get(i);
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getUserDate());
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
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getUserDate());
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
        if (monthlyBudgetStr != null && !monthlyBudgetStr.equals("N/A")) {
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
        dayCount++;
        if (pickedDay != 0)
            this.day = LocalDate.ofYearDay(pickedYear, pickedDay).minusDays(dayCount).getDayOfYear();
        else
            this.day = LocalDate.now().minusDays(dayCount).getDayOfYear();

        dailyReportList.add(dailyReport);
        adapter.submitList(dailyReportList);
        adapter.notifyItemInserted(adapter.getItemCount() + 1);
    }

    static class DailyReport {
        private String date;
        private String income;
        private String expenses;
        private String budget;
        private String budgetLeft;
        private String highestExpense;
        private String highestItem;
        private String incomeDiff;
        private String expensesDiff;

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

    public void purchaseProForThis() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pro_feature)
                .setMessage(R.string.buy_the_pro_version_to_see_more_into_the_history)
                .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), UpgradeToPro.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}