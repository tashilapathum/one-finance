package com.tashila.mywalletfree;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elconfidencial.bubbleshowcase.BubbleShowCase;
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.elconfidencial.bubbleshowcase.BubbleShowCaseListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class BankFrag extends Fragment {
    public static final String TAG = "BankFrag";
    private SharedPreferences sharedPref;
    private TextView tvAccountName;
    private TextView tvCurrency;
    private TextView tvAccountBalance;
    private String accountName;
    private String currency;
    private String accountBalance;
    private Button btnDeposit;
    private Button btnWithdraw;
    private ImageButton btnSwitch;
    private TextInputLayout tilAmount;
    private EditText etAmount;
    private int selectedAccNo;
    private View view;
    private boolean sinhala;
    LinearLayout linearRecent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        view = inflater.inflate(R.layout.frag_bank, container, false);

        tvAccountName = view.findViewById(R.id.accountName);
        tvCurrency = view.findViewById(R.id.currency);
        tvAccountBalance = view.findViewById(R.id.balance);
        btnDeposit = view.findViewById(R.id.deposit);
        btnWithdraw = view.findViewById(R.id.withdraw);
        btnSwitch = view.findViewById(R.id.switchAcc);
        tilAmount = view.findViewById(R.id.editAmount);
        etAmount = tilAmount.getEditText();
        selectedAccNo = sharedPref.getInt("selectedAccNo", 1);
        accountBalance = sharedPref.getString("selectedAccBalance", null);
        accountName = sharedPref.getString("selectedAccName", null);
        currency = sharedPref.getString("currency", null);
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

        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark"))
            new Essentials(getActivity()).invertDrawable(view.findViewById(R.id.switchAcc));


        boolean haveNoAccounts = sharedPref.getBoolean("haveNoAccounts", true);
        if (haveNoAccounts) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.bank_welcome_title)
                    .setMessage(R.string.bank_welcome_message)
                    .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(getActivity(), NewAccount.class));
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
        } else {
            loadDetails();
            loadActivities();
        }

        showInstructions(view.findViewById(R.id.switchAcc));

        return view;
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
        accountName = sharedPref.getString("selectedAccName", null);
        currency = sharedPref.getString("currency", null);
        accountBalance = sharedPref.getString("selectedAccBalance", null);
        selectedAccNo = sharedPref.getInt("selectedAccNo", 1);

        tvAccountName.setText(accountName);
        tvCurrency.setText(currency);
        tvAccountBalance.setText(accountBalance);
    }

    private void onClickDepositOrWithdraw(View v) {
        //get data
        double newBalance = 0;
        final int i = selectedAccNo;
        String activity = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        String timeStamp = LocalDateTime.now().format(formatter);
        String savedCurrentBalance = sharedPref.getString("selectedAccBalance", null);
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
                if (sinhala) activity = currency + inputAmount + "ක් " + accountName + " ගිණුමෙහි තැන්පත් කරන ලදී" + "###" + timeStamp;
                else activity = "Deposited " + currency + inputAmount + " to " + accountName + "###" + timeStamp;
            }
            if (isWithdrawId) {
                newBalance = currentBalance - inputAmount;
                if (sinhala) activity = currency + inputAmount + "ක් " + accountName + " ගිණුමෙන් ආපසු ගන්නා ලදී" + "###" + timeStamp;
                else activity = "Withdrew " + currency + inputAmount + " from " + accountName + "###" + timeStamp;

                //update wallet
                double walletBalance = Double.parseDouble(sharedPref.getString("balance", null));
                double newWalletBalance = walletBalance + inputAmount;
                sharedPref.edit().putString("balance", String.valueOf(newWalletBalance)).apply();
            }

            //save
            String newBalanceStr = String.valueOf(newBalance);
            sharedPref.edit().putString("accountBalance" + i, newBalanceStr).apply();
            sharedPref.edit().putString("selectedAccBalance", newBalanceStr).apply();
            tvAccountBalance.setText(newBalanceStr); //update balance on screen

            String activities = sharedPref.getString("activities" + i, null);
            activities = activities + "~~~" + activity;
            sharedPref.edit().putString("activities" + i, activities).apply();

            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            Snackbar snackbar = Snackbar.make(bottomNav, R.string.updated, Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(bottomNav);
            final boolean finalIsDepositId = isDepositId;
            final String finalActivity = activity;
            final boolean finalIsWithdrawId = isWithdrawId;
            snackbar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String undoActivities = sharedPref.getString("activities" + i, null);
                    undoActivities = undoActivities.replace("~~~" + finalActivity, "");
                    sharedPref.edit().putString("activities" + i, undoActivities).apply();
                    double undoBalance = 0;
                    undoBalance = Double.parseDouble(sharedPref.getString("selectedAccBalance", null));
                    if (finalIsDepositId) undoBalance = undoBalance - inputAmount;
                    if (finalIsWithdrawId) undoBalance = undoBalance + inputAmount;
                    sharedPref.edit().putString("accountBalance" + i, String.valueOf(undoBalance)).apply();
                    sharedPref.edit().putString("selectedAccBalance", String.valueOf(undoBalance)).apply();
                    tvAccountBalance.setText(String.valueOf(undoBalance));
                    //update wallet
                    double walletBalance = Double.parseDouble(sharedPref.getString("balance", null));
                    double newWalletBalance = walletBalance - inputAmount;
                    sharedPref.edit().putString("balance", String.valueOf(newWalletBalance)).apply();
                    reloadFragment();
                }
            });
            snackbar.show();

            //show
            etAmount.setText("");
            loadActivities();
            reloadFragment();

        } else
            Toast.makeText(getActivity(), R.string.spend_more_than_have, Toast.LENGTH_LONG).show();
    }

    private void showInstructions(View v) {
        int viewId = v.getId();
        boolean haveNoAccounts = sharedPref.getBoolean("haveNoAccounts", true);
        switch (viewId) {
            case R.id.withdraw: {
                boolean alreadyShown = sharedPref.getBoolean("insWithdrawnWhere", false);
                if (!alreadyShown & !haveNoAccounts) {
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
                if (!alreadyShown & !haveNoAccounts) {
                    new BubbleShowCaseBuilder(getActivity())
                            .title(getString(R.string.switch_acc))
                            .description(getString(R.string.acc_switch_des))
                            .targetView(v)
                            .show();
                    sharedPref.edit().putBoolean("insSwitchAcc", true).apply();
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
        int i = sharedPref.getInt("selectedAccNo", 1);
        String activities = sharedPref.getString("activities" + i, null);
        String[] activitiesArr = activities.split("~~~");
        for (int j = activitiesArr.length - 1; j >= 0; j--) {
            if (!activitiesArr[j].equals("null")) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                ViewGroup parent = (ViewGroup) view;
                linearRecent = parent.findViewById(R.id.linearRecent);
                View sampleRecentActivity = inflater.inflate(R.layout.sample_recent_activity, null);
                TextView tvActivity = sampleRecentActivity.findViewById(R.id.activity);
                TextView tvDate = sampleRecentActivity.findViewById(R.id.date);
                String[] arr = activitiesArr[j].split("###"); //to separate the timestamp
                String activityPart = arr[0];
                String datePart = arr[1];
                tvActivity.setText(activityPart);
                tvDate.setText(datePart);

                //remove all views once
                if (!sharedPref.getBoolean("removedViewsOnceB", false)) {
                    linearRecent.removeAllViews();            //B -> Bank
                    sharedPref.edit().putBoolean("removedViewsOnceB", true).apply();
                }

                linearRecent.addView(sampleRecentActivity);

                //empty textView
                TextView textView = new TextView(getActivity());
                textView.setText(" ");
                linearRecent.addView(textView);
            }
        }
    }

    private void reloadFragment() {
        Fragment bankFrag = getActivity().getSupportFragmentManager().findFragmentByTag("BankFrag");
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(bankFrag).attach(bankFrag).commit();
    }
}
