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

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.format.TextStyle;

import java.text.DecimalFormat;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_reports, container, false);
        AndroidThreeTen.init(getActivity());
        monthlyReportList = new ArrayList<>();
        month = LocalDate.now().getMonthValue();
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MonthlyReportsAdapter();
        recyclerView.setLayoutAnimation(new AnimationHandler().getSlideUpController());
        recyclerView.setAdapter(adapter);
        calculateMonthlyReport(month);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1))
                    if (sharedPref.getBoolean("MyWalletPro", false))
                        calculateMonthlyReport(month);
                    else
                        purchaseProForThis();
            }
        });
        return view;
    }

    private void calculateMonthlyReport(int month) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(getActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        List<TransactionItem> transactionsList = transactionsViewModel.getTransactionsList();

        //card title
        String monthTitle = Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());

        //daily data
        double income = 0;
        double expenses = 0;
        double highestExpense = 0;
        String highestItem = null;
        List<TransactionItem> transactionsOfMonth = new ArrayList<>(); //to find the highest and lowest day
        for (int i = 0; i < transactionsList.size(); i++) {
            TransactionItem currentTransaction = transactionsList.get(i);
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getUserDate());
            int transactionMonth = dateTimeHandler.getMonthValue();
            if (transactionMonth == month) {
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
        int monthLength = LocalDate.of(LocalDate.now().getYear(), month, 1).lengthOfMonth();
        double averageIncome = income / monthLength;
        double averageExpenses = expenses / monthLength;

        //to calculate differences
        double incomeOLD = 0;
        double expensesOLD = 0;
        for (int i = 0; i < transactionsList.size(); i++) {
            TransactionItem currentTransaction = transactionsList.get(i);
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getUserDate());
            int transactionMonth = dateTimeHandler.getMonthValue();
            if (transactionMonth == Month.of(month - 1).getValue()) {
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
            if (currentTransaction.getPrefix().equals("+") && currentTransaction.getAmountValue() > tempMostIncome) {
                mostIncomeTransaction = currentTransaction;
                tempMostIncome = currentTransaction.getAmountValue();
            }

            if (currentTransaction.getPrefix().equals("-") && currentTransaction.getAmountValue() > tempMostExpense) {
                mostExpenseTransaction = currentTransaction;
                tempMostExpense = currentTransaction.getAmountValue();
            }
        }
        String mostIncomeDay = null;
        String mostExpenseDay = null;
        if (mostIncomeTransaction != null) {
            DateTimeHandler dateTimeHandler = new DateTimeHandler(mostIncomeTransaction.getUserDate());
            mostIncomeDay = ""+dateTimeHandler.getDayOfMonth();
            String lastDigit = mostIncomeDay.substring(mostIncomeDay.length() - 1);
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
                default: mostIncomeDay = mostIncomeDay + getString(R.string.th);
            }
            mostIncomeDay = mostIncomeDay
                    + " (" + currency + mostIncomeTransaction.getAmount()
                    + " - " + mostIncomeTransaction.getDescription() + ")";
        }
        if (mostExpenseTransaction != null){
            DateTimeHandler dateTimeHandler = new DateTimeHandler(mostExpenseTransaction.getUserDate());
            mostExpenseDay = ""+dateTimeHandler.getDayOfMonth();
            String lastDigit = mostExpenseDay.substring(mostExpenseDay.length() - 1);
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
                default: mostExpenseDay = mostExpenseDay + getString(R.string.th);
            }
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
        if (month == 1) this.month = 12;
        else this.month = month - 1;

        monthlyReportList.add(monthlyReport);
        adapter.submitList(monthlyReportList);
        adapter.notifyItemInserted(adapter.getItemCount() + 1);
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