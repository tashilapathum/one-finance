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
import org.threeten.bp.temporal.WeekFields;

import java.text.DecimalFormat;
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
    private WeekFields weekFields;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly_reports, container, false);
        AndroidThreeTen.init(getActivity());
        weeklyReportList = new ArrayList<>();
        weekFields = WeekFields.of(Locale.getDefault());
        week = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new WeeklyReportsAdapter();
        recyclerView.setLayoutAnimation(new AnimationHandler().getSlideUpController());
        recyclerView.setAdapter(adapter);
        calculateWeeklyReport(week);
        calculateWeeklyReport(week);
        calculateWeeklyReport(week);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1))
                    if (sharedPref.getBoolean("MyWalletPro", false))
                        calculateWeeklyReport(week);
                    else
                        purchaseProForThis();
            }
        });
        return view;
    }

    private void calculateWeeklyReport(int week) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(getActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        List<TransactionItem> transactionsList = transactionsViewModel.getTransactionsList();
        String weekNo;

        weekNo = getString(R.string.week) + week;
        if (week == LocalDate.now().get(weekFields.weekOfWeekBasedYear()))
            weekNo = getString(R.string.re_this_week) + weekNo + ")";
        if (week == LocalDate.now().get(weekFields.weekOfWeekBasedYear()) - 1)
            weekNo = getString(R.string.re_last_week) + weekNo + ")";
        double income = 0;
        double expenses = 0;
        double highestExpense = 0;
        String highestItem = null;
        List<String> dailyIncomes = new ArrayList<>(7);
        List<String> dailyExpenses = new ArrayList<>(7);
        for (int x = 0; x < 7; x++) {
            dailyIncomes.add("0");
            dailyExpenses.add("0");
        }
        for (int i = 0; i < transactionsList.size(); i++) {
            TransactionItem currentTransaction = transactionsList.get(i);
            DateTimeHandler dateTimeHandler = new DateTimeHandler(currentTransaction.getUserDate());
            int transactionWeek = dateTimeHandler.getWeek();
            int transactionDay = dateTimeHandler.getDayOfWeek();
            if (transactionWeek == week) {
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
                    if (d == transactionDay) {
                        double dailyTotal = Double.parseDouble(dailyIncomes.get(d)) + currentTransaction.getAmountValue();
                        if (currentTransaction.getPrefix().equals("+")) {
                            dailyIncomes.add(d, ""+dailyTotal);
                        }
                        else {
                            dailyExpenses.add(d, ""+dailyTotal);
                        }
                    }
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
            if (transactionDate == week - 1) {
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
        double budget = monthlyBudget / 4;
        double budgetLeft = budget - expenses;
        //to show (+) for positive values
        String incomeDiffStr = null;
        if (!df.format(incomeDiff).contains("-"))
            incomeDiffStr = "+" + df.format(incomeDiff);
        String expensesDiffStr = null;
        if (!df.format(incomeDiff).contains("-"))
            expensesDiffStr = "+" + df.format(expensesDiff);

        WeeklyReport weeklyReport = new WeeklyReport(weekNo,
                new Amount(getActivity(), income).getAmountString(),
                new Amount(getActivity(), expenses).getAmountString(),
                new Amount(getActivity(), budget).getAmountString(),
                new Amount(getActivity(), budgetLeft).getAmountString(),
                new Amount(getActivity(), highestExpense).getAmountString(), highestItem,
                "(" + incomeDiffStr + ")",
                "(" + expensesDiffStr + ")",
                dailyIncomes, dailyExpenses);

        this.week = week - 1; //to load next cards
        weeklyReportList.add(weeklyReport);
        adapter.submitList(weeklyReportList);
        adapter.notifyItemInserted(adapter.getItemCount() + 1);
    }

    static class WeeklyReport {
        private String week;
        private String income;
        private String expenses;
        private String budget;
        private String budgetLeft;
        private String highestExpense;
        private String highestItem;
        private String incomeDiff;
        private String expensesDiff;
        private List<String> dailyIncomes;
        private List<String> dailyExpenses;

        public WeeklyReport(String week, String income, String expenses, String budget, String budgetLeft,
                            String highestExpense, String highestItem, String incomeDiff, String expensesDiff,
                            List<String> dailyIncomes, List<String> dailyExpenses) {
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