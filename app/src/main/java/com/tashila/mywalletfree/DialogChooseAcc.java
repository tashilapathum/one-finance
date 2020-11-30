package com.tashila.mywalletfree;

import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogChooseAcc extends DialogFragment {
    public static final String TAG = "DialogChooseAcc";
    private View view;
    private SharedPreferences sharedPref;
    AlertDialog thisDialog;
    private AccountsViewModel accountsViewModel;

    private DialogInterface.OnDismissListener onDismissListener;

    void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null)
            onDismissListener.onDismiss(dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_choose_acc, null);
        builder.setView(view)
                .setTitle(R.string.choose_acc)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        thisDialog = builder.create();

        accountsViewModel = new AccountsViewModel(getActivity().getApplication());
        showAccountsList();

        return thisDialog;
    }

    private void showAccountsList() {
        String currency = sharedPref.getString("currency", "");
        final boolean calledFromWallet = sharedPref.getBoolean("chooseAccFromWallet", false);
        final boolean transferFromBank = sharedPref.getBoolean("transferFromBank", false);

        final List<Account> accountsList = accountsViewModel.getAllAccounts();
        for (int i = 0; i < accountsList.size(); i++) {
            ViewGroup baseLayout = (ViewGroup) view;
            View sampleSwitchLayout = LayoutInflater.from(getActivity()).inflate(R.layout.sample_acc_switch, null);
            TextView tvAccountName = sampleSwitchLayout.findViewById(R.id.accountName);
            TextView tvAccountBalance = sampleSwitchLayout.findViewById(R.id.accountBalance);
            TextView tvSelectedDot = sampleSwitchLayout.findViewById(R.id.selectedDot);

            tvAccountName.setText(accountsList.get(i).getAccName());
            tvAccountBalance.setText(currency + accountsList.get(i).getAccBalance());
            if (accountsList.get(i).isSelected()) {
                tvSelectedDot.setVisibility(View.VISIBLE);
            }
            final int finalI = i;
            sampleSwitchLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //transfer between accounts (before setting the tapped account as selected)
                    if (transferFromBank) {
                        sharedPref.edit().putBoolean("transferFromBank", false).apply();
                        double amount = getArguments().getDouble("amount");

                        //to deduct money
                        Account currentAccount = null;
                        for (int j = 0; j < accountsList.size(); j++)
                            if (accountsList.get(j).isSelected())
                                currentAccount = accountsList.get(j);

                        Account targetAccount = accountsList.get(finalI); //to add money

                        //update current account
                        String newCurrentBalance = String.valueOf(Double.parseDouble(currentAccount.getAccBalance()) - amount);
                        currentAccount.setAccBalance(newCurrentBalance);
                        List<String> activities = currentAccount.getActivities();
                        activities.add(getString(R.string.transferred_space) + new Amount(getActivity(), amount).getAmountString()
                                + getString(R.string.to) + targetAccount.getAccName() + getString(R.string.transfer_to_suffix)
                                + "###" + new DateTimeHandler().getTimestamp());
                        List<String> balanceHistory = currentAccount.getBalanceHistory();
                        balanceHistory.add(String.valueOf(Double.parseDouble(currentAccount.getAccBalance()) - amount));
                        accountsViewModel.update(currentAccount);

                        //update target account
                        String newTargetBalance = String.valueOf(Double.parseDouble(targetAccount.getAccBalance()) + amount);
                        targetAccount.setAccBalance(newTargetBalance); //this one gets updated below
                        List<String> activities2 = targetAccount.getActivities();
                        activities2.add(getString(R.string.transferred_space) + new Amount(getActivity(), amount).getAmountString()
                                + getString(R.string.from) + currentAccount.getAccName() + getString(R.string.transfer_from_suffix)
                                + "###" + new DateTimeHandler().getTimestamp());
                        List<String> balanceHistory2 = targetAccount.getBalanceHistory();
                        balanceHistory2.add(String.valueOf(Double.parseDouble(targetAccount.getAccBalance()) + amount));

                        Toast.makeText(getActivity(), getString(R.string.transferred), Toast.LENGTH_SHORT).show();
                    }

                    //select the tapped account
                    accountsList.get(finalI).setSelected(true);
                    accountsViewModel.update(accountsList.get(finalI));
                    //deselect all other accounts
                    int selectedAccID = accountsList.get(finalI).getId();
                    for (int i = 0; i < accountsList.size(); i++) {
                        if (accountsList.get(i).getId() != selectedAccID) {
                            accountsList.get(i).setSelected(false);
                            accountsViewModel.update(accountsList.get(i));
                        }
                    }
                    if (calledFromWallet) {
                        //transfer from wallet to account
                        WalletFragment walletFragment = (WalletFragment) getActivity().getSupportFragmentManager().findFragmentByTag("WalletFragment");
                        walletFragment.doBankStuff(accountsList.get(finalI));
                        walletFragment.continueLongClickProcess();
                    } else
                        Toast.makeText(getActivity(), R.string.switched, Toast.LENGTH_SHORT).show();

                    thisDialog.dismiss();
                }
            });
            baseLayout.addView(sampleSwitchLayout);
        }

        if (!calledFromWallet && !transferFromBank) {
            MaterialButton btnAddAccount = new MaterialButton(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 8, 0, 0);
            btnAddAccount.setLayoutParams(params);
            btnAddAccount.setText(R.string.manage_acc);
            btnAddAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AccountManager.class);
                    startActivity(intent);
                    thisDialog.dismiss();
                }
            });
            LinearLayout baseLayout = view.findViewById(R.id.dialogChooseAcc);
            baseLayout.addView(btnAddAccount);
        }
    }
}
