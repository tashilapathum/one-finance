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
import com.tantalum.onefinance.transactions.TransactionItem;
import com.tantalum.onefinance.transactions.TransactionsViewModel;
import com.tantalum.onefinance.pro.UpgradeToProActivity;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonthlyReportsFragment extends Fragment {
    public static final String TAG = "MonthlyReportsFragment";
    private SharedPreferences sharedPref;
    private String currency;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MonthlyReportsAdapter adapter;
    private List<MonthlyReport> monthlyReportList;
    private int month;
    private int monthCount;
    private int year;
    private int yearCount;
    private int pickedMonth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_reports, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        Bundle bundle = this.getArguments();
        boolean isFromWallet = false;
        if (bundle != null)
            isFromWallet = bundle.getBoolean("fromWallet");

        monthlyReportList = new ArrayList<>();
        pickedMonth = sharedPref.getInt("reports_month", 0);
        if (pickedMonth != 0)
            month = pickedMonth;
        else
            month = LocalDate.now().getMonthValue();
        monthCount = 0;
        int pickedYear = sharedPref.getInt("reports_year", 0);
        if (pickedYear != 0)
            year = pickedYear;
        else
            year = LocalDate.now().getYear();
        yearCount = 0;
        if (pickedYear != 0)
            yearCount = year - pickedYear;
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MonthlyReportsAdapter(isFromWallet);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutAnimation(new AnimationHandler().getSlideUpController());
        recyclerView.setAdapter(adapter);
        calculateMonthlyReport(month, year);
        if (!isFromWallet) {
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
        calculateMonthlyReport(month, year);
        calculateMonthlyReport(month, year);
        calculateMonthlyReport(month, year);
        calculateMonthlyReport(month, year);
        calculateMonthlyReport(month, year);
        calculateMonthlyReport(month, year);
        calculateMonthlyReport(month, year);
        calculateMonthlyReport(month, year);
        calculateMonthlyReport(month, year);
        calculateMonthlyReport(month, year);
    }

    private void calculateMonthlyReport(int month, int year) {
        currency = sharedPref.getString("currency", "");
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(getActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        List<TransactionItem> transactionsList = transactionsViewModel.getTransactionsList();

        //card title
        String monthTitle = Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault());

        //daily data
        double income = 0;
        double expenses = 0;
        double highestExpense = 0;
        String highestItem = null;
        List<TransactionItem> transactionsOfMonth = new ArrayList<>(); //to find the highest and lowest day
        for (int i = 0; i < transactionsList.size(); i++) {
            TransactionItem currentTransaction = transactionsList.get(i);
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getTimeInMillis());
            int transactionMonth = dateTimeHandler.getMonthValue();
            int transactionYear = dateTimeHandler.getYear();
            if (transactionMonth == month && transactionYear == year) {
                if (currentTransaction.getPrefix().equals("+"))
                    income = income + Double.parseDouble(currentTransaction.getAmount());
                else {
                    if (currentTransaction.getAmountValue() > highestExpense) {
                        highestExpense = currentTransaction.getAmountValue();
                        highestItem = currentTransaction.getDescription();
                    }
                    expenses = expenses + Double.parseDouble(currentTransaction.getAmount());
                }
                transactionsOfMonth.add(currentTransaction);
            }
        }

        //averages
        int monthLength = LocalDate.of(LocalDate.ofYearDay(year, LocalDate.now().getDayOfYear()).getYear(), month, 1).lengthOfMonth();
        double averageIncome = income / monthLength;
        double averageExpenses = expenses / monthLength;

        //to calculate differences
        double incomeOLD = 0;
        double expensesOLD = 0;
        for (int i = 0; i < transactionsList.size(); i++) {
            TransactionItem currentTransaction = transactionsList.get(i);
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getTimeInMillis());
            int transactionMonth = dateTimeHandler.getMonthValue();
            int transactionYear = dateTimeHandler.getYear();
            if (transactionMonth == LocalDate.now().minusMonths(monthCount + 1 /*to get prev month*/).getMonthValue()
                    && transactionYear == year) {
                if (currentTransaction.getPrefix().equals("+"))
                    incomeOLD = incomeOLD + Double.parseDouble(currentTransaction.getAmount());
                if (currentTransaction.getPrefix().equals("-"))
                    expensesOLD = expensesOLD + Double.parseDouble(currentTransaction.getAmount());
            }
        }
        double incomeDiff = income - incomeOLD;
        double expensesDiff = expenses - expensesOLD;

        //budget
        String monthlyBudgetStr = sharedPref.getString("monthlyBudget", "0");
        DecimalFormat df = new DecimalFormat("#.00");
        double budget = Double.parseDouble(monthlyBudgetStr);
        double budgetLeft = budget - expenses;
        //to show (+) for positive values
        String incomeDiffStr = null;
        if (!df.format(incomeDiff).contains("-"))
            incomeDiffStr = "+" + df.format(incomeDiff);
        String expensesDiffStr = null;
        if (!df.format(incomeDiff).contains("-"))
            expensesDiffStr = "+" + df.format(expensesDiff);

        //find most income and most expense day
        TransactionItem mostIncomeTransaction = null;
        TransactionItem mostExpenseTransaction = null;
        double tempMostIncome = 0;
        double tempMostExpense = 0;
        for (int i = 0; i < transactionsOfMonth.size(); i++) {
            TransactionItem currentTransaction = transactionsOfMonth.get(i);
            if (new DateTimeHandler(currentTransaction.getTimeInMillis()).getYear() == year) {
                if (currentTransaction.getPrefix().equals("+") && currentTransaction.getAmountValue() > tempMostIncome) {
                    mostIncomeTransaction = currentTransaction;
                    tempMostIncome = currentTransaction.getAmountValue();
                }

                if (currentTransaction.getPrefix().equals("-") && currentTransaction.getAmountValue() > tempMostExpense) {
                    mostExpenseTransaction = currentTransaction;
                    tempMostExpense = currentTransaction.getAmountValue();
                }
            }
        }
        String mostIncomeDay = null;
        String mostExpenseDay = null;
        if (mostIncomeTransaction != null) {
            DateTimeHandler dateTimeHandler = new DateTimeHandler(mostIncomeTransaction.getTimeInMillis());
            mostIncomeDay = "" + dateTimeHandler.getDayOfMonth();
            String lastDigit = mostIncomeDay.substring(mostIncomeDay.length() - 1);
            if (dateTimeHandler.getDayOfMonth() != 11 && dateTimeHandler.getDayOfMonth() != 12 && dateTimeHandler.getDayOfMonth() != 13) {
                switch (lastDigit) {
                    case "1": {
                        mostIncomeDay = mostIncomeDay + getString(R.string.st);
                        break;
                    }
                    case "2": {
                        mostIncomeDay = mostIncomeDay + getString(R.string.nd);
                        break;
                    }
                    case "3": {
                        mostIncomeDay = mostIncomeDay + getString(R.string.rd);
                        break;
                    }
                    default:
                        mostIncomeDay = mostIncomeDay + getString(R.string.th);
                }
            } else
                mostIncomeDay = mostIncomeDay + getString(R.string.th);
            mostIncomeDay = mostIncomeDay
                    + " (" + currency + mostIncomeTransaction.getAmount()
                    + " - " + mostIncomeTransaction.getDescription() + ")";
        }
        if (mostExpenseTransaction != null) {
            DateTimeHandler dateTimeHandler = new DateTimeHandler(mostExpenseTransaction.getTimeInMillis());
            mostExpenseDay = "" + dateTimeHandler.getDayOfMonth();
            String lastDigit = mostExpenseDay.substring(mostExpenseDay.length() - 1);
            if (dateTimeHandler.getDayOfMonth() != 11 && dateTimeHandler.getDayOfMonth() != 12 && dateTimeHandler.getDayOfMonth() != 13) {
                switch (lastDigit) {
                    case "1": {
                        mostExpenseDay = mostExpenseDay + getString(R.string.st);
                        break;
                    }
                    case "2": {
                        mostExpenseDay = mostExpenseDay + getString(R.string.nd);
                        break;
                    }
                    case "3": {
                        mostExpenseDay = mostExpenseDay + getString(R.string.rd);
                        break;
                    }
                    default:
                        mostExpenseDay = mostExpenseDay + getString(R.string.th);
                }
            } else
                mostExpenseDay = mostExpenseDay + getString(R.string.th);
            mostExpenseDay = mostExpenseDay
                    + " (" + currency + mostExpenseTransaction.getAmount()
                    + " - " + mostExpenseTransaction.getDescription() + ")";
        }

        //for daily data chart
        List<String> incomesOfMonth = new ArrayList<>();
        List<String> expensesOfMonth = new ArrayList<>();
        for (int i = 0; i < transactionsOfMonth.size(); i++) {
            TransactionItem currentTransaction = transactionsOfMonth.get(i);
            if (currentTransaction.getPrefix().equals("+"))
                incomesOfMonth.add(currentTransaction.getAmount());
            else
                expensesOfMonth.add(currentTransaction.getAmount());
        }

        MonthlyReport monthlyReport = new MonthlyReport(monthTitle,
                new Amount(getActivity(), income).getAmountString(),
                new Amount(getActivity(), expenses).getAmountString(),
                new Amount(getActivity(), budget).getAmountString(),
                new Amount(getActivity(), budgetLeft).getAmountString(),
                new Amount(getActivity(), highestExpense).getAmountString(), highestItem,
                "(" + incomeDiffStr + ")",
                "(" + expensesDiffStr + ")",
                new Amount(getActivity(), averageIncome).getAmountString(),
                new Amount(getActivity(), averageExpenses).getAmountString(),
                mostIncomeDay, mostExpenseDay,
                incomesOfMonth, expensesOfMonth);

        //to load next cards
        monthCount++;
        if (pickedMonth != 0)
            this.month = Month.of(pickedMonth).minus(monthCount).getValue();
        else
            this.month = LocalDate.now().minusMonths(monthCount).getMonthValue();
        if (monthCount % 12 == 0) yearCount++;
        this.year = LocalDate.now().minusYears(yearCount).getYear();

        monthlyReportList.add(monthlyReport);
        adapter.submitList(monthlyReportList);
        adapter.notifyItemInserted(adapter.getItemCount() + 1);
    }

    public void showUpdatedReports() {
        monthlyReportList = new ArrayList<>();
        adapter.submitList(monthlyReportList);
        month = LocalDate.now().getMonthValue();
        year = LocalDate.now().getYear();
        calculateMonthlyReport(month, year);
    }

    static class MonthlyReport {
        private String month;
        private String income;
        private String expenses;
        private String budget;
        private String budgetLeft;
        private String highestExpense;
        private String highestItem;
        private String incomeDiff;
        private String expensesDiff;
        private String averageIncome;
        private String averageExpenses;
        private String mostIncomeDay;
        private String mostExpenseDay;
        private List<String> incomesOfMonth;
        private List<String> expensesOfMonth;

        public MonthlyReport(String month, String income, String expenses, String budget, String budgetLeft,
                             String highestExpense, String highestItem, String incomeDiff, String expensesDiff,
                             String averageIncome, String averageExpenses, String mostIncomeDay, String mostExpenseDay,
                             List<String> incomesOfMonth, List<String> expensesOfMonth) {
            this.income = income;
            this.expenses = expenses;
            this.budget = budget;
            this.budgetLeft = budgetLeft;
            this.highestExpense = highestExpense;
            this.highestItem = highestItem;
            this.incomeDiff = incomeDiff;
            this.expensesDiff = expensesDiff;
            this.month = month;
            this.averageIncome = averageIncome;
            this.averageExpenses = averageExpenses;
            this.mostIncomeDay = mostIncomeDay;
            this.mostExpenseDay = mostExpenseDay;
            this.incomesOfMonth = incomesOfMonth;
            this.expensesOfMonth = expensesOfMonth;
        }

        public String getMonth() {
            return month;
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

        public String getMostIncomeDay() {
            return mostIncomeDay;
        }

        public String getMostExpenseDay() {
            return mostExpenseDay;
        }

        public String getAverageIncome() {
            return averageIncome;
        }

        public String getAverageExpenses() {
            return averageExpenses;
        }

        public List<String> getIncomesOfMonth() {
            return incomesOfMonth;
        }

        public List<String> getExpensesOfMonth() {
            return expensesOfMonth;
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