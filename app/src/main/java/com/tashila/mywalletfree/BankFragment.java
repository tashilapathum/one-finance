package com.tashila.mywalletfree;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

public class BankFragment extends Fragment {
    public static final String TAG = "BankFragment";
    private SharedPreferences sharedPref;
    private TextView tvAccountName;
    private TextView tvCurrency;
    private TextView tvAccountBalance;
    private TextView tv1;
    private TextView tv2;
    private String accountName;
    private String currency;
    private String accountBalance;
    private Button btnDeposit;
    private Button btnWithdraw;
    private ImageButton btnSwitch;
    private TextInputLayout tilAmount;
    private EditText etAmount;
    private View view;
    private boolean sinhala;
    LinearLayout linearRecent;
    boolean haveAccounts;
    private AccountsViewModel accountsViewModel;
    private TransactionsViewModel transactionsViewModel;
    private Account selectedAccount;
    private DecimalFormat df;
    private Snackbar snackbar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        view = inflater.inflate(R.layout.frag_bank, container, false);

        accountsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AccountsViewModel.class);
        transactionsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        List<Account> accountList = accountsViewModel.getAllAccounts();
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).isSelected())
                selectedAccount = accountList.get(i);
        }

        tvAccountName = view.findViewById(R.id.accountName);
        tvCurrency = view.findViewById(R.id.currency);
        tvAccountBalance = view.findViewById(R.id.balance);
        tv1 = view.findViewById(R.id.tv1);
        tv2 = view.findViewById(R.id.tv2);
        btnDeposit = view.findViewById(R.id.deposit);
        btnWithdraw = view.findViewById(R.id.withdraw);
        btnSwitch = view.findViewById(R.id.switchAcc);
        tilAmount = view.findViewById(R.id.editAmount);
        etAmount = tilAmount.getEditText();
        currency = sharedPref.getString("currency", null);
        setShadows(tv1, tv2, tvAccountName, tvAccountBalance, tvCurrency);
        String language = sharedPref.getString("language", "english");
        if (language.equalsIgnoreCase("සිංහල")) sinhala = true;

        btnDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate())
                    onClickDepositOrWithdraw(v);
            }
        });
        btnWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate())
                    onClickDepositOrWithdraw(v);
            }
        });
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        etAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view1, boolean b) {
                showInstructions(view.findViewById(R.id.withdraw));
            }
        });
        tvAccountName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountDetails.class);
                intent.putExtra("neededAccount", selectedAccount);
                startActivity(intent);
            }
        });

        haveAccounts = sharedPref.getBoolean("haveAccounts", false);
        if (haveAccounts) {
            loadDetails();
            loadActivities();
            calculateInterests();
        }

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!sharedPref.getBoolean("bankFragAlreadyReloaded", false)) {
            reloadFragment();
            sharedPref.edit().putBoolean("bankFragAlreadyReloaded", true).apply();
        }
        if (!haveAccounts) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.bank_welcome_title)
                    .setMessage(R.string.bank_welcome_message)
                    .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), NewAccount.class);
                            intent.putExtra("isNewAccount", true);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(getActivity(), MainActivity.class));
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        }
        else {
            int bankOpenCount = sharedPref.getInt("bankOpenCount", 0);
            if (bankOpenCount > 2)
                showInstructions(tvAccountName);
            if (bankOpenCount > 3)
                showInstructions(btnSwitch);
            sharedPref.edit().putInt("bankOpenCount", bankOpenCount + 1).apply();
        }
    }

    private void showDialog() {
        DialogChooseAcc dialogChooseAcc = new DialogChooseAcc();
        dialogChooseAcc.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                loadDetails();
                loadActivities();
                reloadFragment();
            }
        });
        dialogChooseAcc.show(getActivity().getSupportFragmentManager(), "choose account dialog");
    }

    private void loadDetails() {
        List<Account> accountList = accountsViewModel.getAllAccounts();
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).isSelected())
                selectedAccount = accountList.get(i);
        }
        accountName = selectedAccount.getAccName();
        accountBalance = selectedAccount.getAccBalance();

        tvAccountName.setText(accountName);
        tvCurrency.setText(currency);
        DecimalFormat df = new DecimalFormat("#.00");
        accountBalance = df.format(Double.parseDouble(accountBalance));
        tvAccountBalance.setText(accountBalance);
    }

    private void onClickDepositOrWithdraw(View v) {
        //get data
        double newBalance = 0;
        //final int i = selectedAccNo;
        String activity = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        df = new DecimalFormat("#.00");
        final String timeStamp = LocalDateTime.now().format(formatter);
        String savedCurrentBalance = selectedAccount.getAccBalance();
        double currentBalance = Double.parseDouble(savedCurrentBalance);
        final double inputAmount = Double.parseDouble(etAmount.getText().toString());

        boolean isDepositId = false;
        boolean isWithdrawId = false;
        if (v.getId() == btnDeposit.getId()) isDepositId = true;
        if (v.getId() == btnWithdraw.getId()) isWithdrawId = true;
        if ((inputAmount <= currentBalance) || isDepositId) {
            //calculate
            if (isDepositId) {
                newBalance = currentBalance + inputAmount;
                if (sinhala)
                    activity = currency + df.format(inputAmount) + "ක් තැන්පත් කරන ලදී" + "###" + timeStamp;
                else
                    activity = "Deposited " + currency + df.format(inputAmount) + "###" + timeStamp;
            }
            if (isWithdrawId) {
                newBalance = currentBalance - inputAmount;
                if (sinhala)
                    activity = currency + df.format(inputAmount) + "ක් ආපසු ගන්නා ලදී" + "###" + timeStamp;
                else
                    activity = "Withdrew " + currency + df.format(inputAmount) + "###" + timeStamp;

                //update wallet
                double walletBalance = Double.parseDouble(sharedPref.getString("balance", "0"));
                double newWalletBalance = walletBalance + inputAmount;
                String balanceStr = df.format(newWalletBalance);
                sharedPref.edit().putString("balance", balanceStr).apply();
            }

            //show
            final String newBalanceStr = df.format(newBalance);
            tvAccountBalance.setText(newBalanceStr); //update balance on screen
            etAmount.setText("");
            //balances
            selectedAccount.setAccBalance(newBalanceStr);
            List<String> balanceHistory = selectedAccount.getBalanceHistory();
            balanceHistory.add(newBalanceStr);
            Log.i(TAG, "balanceHistorySize"+balanceHistory.size());
            selectedAccount.setBalanceHistory(balanceHistory);
            //activities
            List<String> activities = selectedAccount.getActivities();
            activities.add(activity);
            selectedAccount.setActivities(activities);
            loadActivities();

            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            snackbar = Snackbar.make(bottomNav, R.string.updated, Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(bottomNav);
            final boolean finalIsDepositId = isDepositId;
            final boolean finalIsWithdrawId = isWithdrawId;
            snackbar.setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //update screen
                    double undoBalance = 0;
                    undoBalance = Double.parseDouble(selectedAccount.getAccBalance());
                    if (finalIsDepositId) undoBalance = undoBalance - inputAmount;
                    if (finalIsWithdrawId) undoBalance = undoBalance + inputAmount;
                    tvAccountBalance.setText(String.valueOf(undoBalance));
                    //update wallet
                    double walletBalance = Double.parseDouble(sharedPref.getString("balance", null));
                    double newWalletBalance = walletBalance - inputAmount;
                    sharedPref.edit().putString("balance", String.valueOf(newWalletBalance)).apply();
                    reloadFragment();
                }
            });
            final String transactionDescription;
            if (sinhala)
                transactionDescription = accountName + " ගිණුමෙන් ආපසු ගත්";
            else
                transactionDescription = "Withdrawal from " + accountName;
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        accountsViewModel.update(selectedAccount); //update for real
                        //add transaction
                        if (finalIsWithdrawId) {
                            TransactionItem transaction = new TransactionItem(
                                    sharedPref.getString("balance", "0"), "+",
                                    df.format(inputAmount), transactionDescription,
                                    String.valueOf(System.currentTimeMillis()), null, true);
                            transactionsViewModel.insert(transaction);
                        }
                        if (BankFragment.this.isVisible())
                            loadActivities();
                    }
                }
            });
            snackbar.show();

        } else
            Toast.makeText(getActivity(), R.string.spend_more_than_have, Toast.LENGTH_LONG).show();
    }

    private void showInstructions(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.withdraw: {
                boolean alreadyShown = sharedPref.getBoolean("insWithdrawnWhere", false);
                if (!alreadyShown & haveAccounts) {
                    new BubbleShowCaseBuilder(getActivity())
                            .title(getString(R.string.withdrawn_money))
                            .description(getString(R.string.withdrawn_money_des))
                            .targetView(v)
                            .show();
                    sharedPref.edit().putBoolean("insWithdrawnWhere", true).apply();
                }
                break;
            }
            case R.id.switchAcc: {
                boolean alreadyShown = sharedPref.getBoolean("insSwitchAcc", false);
                if (!alreadyShown & haveAccounts) {
                    new BubbleShowCaseBuilder(getActivity())
                            .title(getString(R.string.switch_acc))
                            .description(getString(R.string.acc_switch_des))
                            .targetView(v)
                            .show();
                    sharedPref.edit().putBoolean("insSwitchAcc", true).apply();
                }
                break;
            }
            case R.id.accountName: {
                boolean alreadyShown = sharedPref.getBoolean("insAccName", false);
                if (!alreadyShown & haveAccounts) {
                    new BubbleShowCaseBuilder(getActivity())
                            .title(getString(R.string.acc_details))
                            .description(getString(R.string.acc_details_descr))
                            .targetView(v)
                            .show();
                    sharedPref.edit().putBoolean("insAccName", true).apply();
                }
                break;
            }
        }
    }

    private boolean validate() {
        if (etAmount.getText().toString().isEmpty()) {
            tilAmount.setError(getString(R.string.required));
            return false;
        } else return true;
    }

    private void loadActivities() {
        List<String> activities = selectedAccount.getActivities();
        ViewGroup parent = (ViewGroup) view;
        linearRecent = parent.findViewById(R.id.linearRecent);
        linearRecent.removeAllViews();
        for (int j = activities.size() - 1; j >= 0; j--) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            ViewGroup parent2 = (ViewGroup) view;
            linearRecent = parent2.findViewById(R.id.linearRecent);
            View sampleRecentActivity = inflater.inflate(R.layout.sample_recent_activity, null);
            TextView tvActivity = sampleRecentActivity.findViewById(R.id.activity);
            TextView tvDate = sampleRecentActivity.findViewById(R.id.date);
            String[] arr = activities.get(j).split("###"); //to separate the timestamp
            String activityPart = arr[0];
            String datePart = arr[1];
            tvActivity.setText(activityPart);
            tvDate.setText(datePart);

            linearRecent.addView(sampleRecentActivity);

            //empty textView
            TextView textView = new TextView(getActivity());
            textView.setText(" ");
            linearRecent.addView(textView);
        }
    }

    private void reloadFragment() {
        Fragment bankFrag = getActivity().getSupportFragmentManager().findFragmentByTag("BankFragment");
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(bankFrag).attach(bankFrag).commit();
    }

    private void calculateInterests() {
        String language = sharedPref.getString("language", "english");
        String currency = sharedPref.getString("currency", null);
        List<Account> accountList = accountsViewModel.getAllAccounts();
        for (int i = 0; i < accountList.size(); i++) {
            Account account = accountList.get(i);
            if (account.getInterestRate() != null) {
                if (!account.getInterestRate().isEmpty()) {
                    int lastMonth = LocalDate.now().minusMonths(1).getMonthValue();

                    if (account.getInterestLastCalcMonth() != lastMonth) {
                        double balance = Double.parseDouble(account.getAccBalance());
                        double interestRate = 0;
                        double interest;
                        if (account.isMultiInterest()) {
                            //assign multi ranges and interests
                            String[] multiInterests = account.getInterestRate().split("~~~");
                            for (String multiInterest : multiInterests) {
                                double min = Double.parseDouble(multiInterest.split("~")[0]);
                                double max = Double.parseDouble(multiInterest.split("~")[1]);
                                double MI = Double.parseDouble(multiInterest.split("~")[2]);
                                if (balance >= min && balance <= max) {
                                    interestRate = MI / 12;
                                    break;
                                }
                            }
                        } else {
                            interestRate = Double.parseDouble(account.getInterestRate()) / 12;
                        }

                        //prepare data
                        interest = balance * interestRate / 100;
                        double roundedInterest = BigDecimal.valueOf(interest).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        String monthName = LocalDate.now().minusMonths(1).getMonth().toString();
                        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
                        String timeStamp = formatter.format(LocalDateTime.now());

                        //save the activity
                        String activity;
                        if (language.equalsIgnoreCase("english"))
                            activity = "Added " + currency + roundedInterest + " as the interest of " + monthName + "###" + timeStamp;
                        else {
                            switch (lastMonth) {
                                case 1: {
                                    monthName = "ජනවාරි";
                                    break;
                                }
                                case 2: {
                                    monthName = "පෙබරවාරි";
                                    break;
                                }
                                case 3: {
                                    monthName = "මාර්තු";
                                    break;
                                }
                                case 4: {
                                    monthName = "අප්‍රේල්";
                                    break;
                                }
                                case 5: {
                                    monthName = "මැයි";
                                    break;
                                }
                                case 6: {
                                    monthName = "ජූනි";
                                    break;
                                }
                                case 7: {
                                    monthName = "ජූලි";
                                    break;
                                }
                                case 8: {
                                    monthName = "අගෝස්තු";
                                    break;
                                }
                                case 9: {
                                    monthName = "සැප්තැම්බර්";
                                    break;
                                }
                                case 10: {
                                    monthName = "ඔක්තෝබර්";
                                    break;
                                }
                                case 11: {
                                    monthName = "නොවැම්බර්";
                                    break;
                                }
                                case 12: {
                                    monthName = "දෙසැම්බර්";
                                    break;
                                }
                            }
                            activity = monthName + " මස පොලිය ලෙස " + currency + roundedInterest + " ක් එකතු කරන ලදී###" + timeStamp;
                        }
                        List<String> activities = account.getActivities();
                        activities.add(activity);
                        account.setActivities(activities);

                        //update balance
                        balance = balance + roundedInterest;
                        account.setAccBalance(String.valueOf(balance));

                        //finalize
                        account.setInterestLastCalcMonth(lastMonth);
                        accountsViewModel.update(account);
                        Log.i(TAG, "Calculate interest complete!");
                    }
                }
            }
        }
    }

    private void setShadows(TextView... views) {
        String theme = sharedPref.getString("theme", "light");
        for (TextView view : views) {
            if (theme.equalsIgnoreCase("dark"))
                view.setShadowLayer(3, 1, 1, R.color.colorShadowDark);
            else
                view.setShadowLayer(3, 1, 1, R.color.colorShadow);
        }
    }
}
