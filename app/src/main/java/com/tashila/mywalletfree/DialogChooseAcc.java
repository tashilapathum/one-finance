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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogChooseAcc extends DialogFragment {
    public static final String TAG = "DialogChooseAcc";
    private View view;
    private SharedPreferences sharedPref;
    private View v;
    AlertDialog thisDialog;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

        v = inflater.inflate(R.layout.sample_acc_switch, null);
        createAccSwitches();

        return thisDialog;
    }

    private void createAccSwitches() {
        String accountName, currency, accountBalance, selectedAccName;
        currency = sharedPref.getString("currency", null);
        final boolean calledFromWallet = sharedPref.getBoolean("chooseAccFromWallet", false);
        for (int i = 1; i <= 20; i++) {
            Log.i(TAG, "from for loop (i): "+i);
            if (sharedPref.getBoolean("isAccountSlot"+i+"Taken", false)) {
                //prepare layouts
                ViewGroup baseLayout = (ViewGroup) view;
                View sampleSwitchLayout = LayoutInflater.from(getActivity()).inflate(R.layout.sample_acc_switch, null);
                sampleSwitchLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chooseAccount(v);
                        if (calledFromWallet) {
                            WalletFragment walletFragment = (WalletFragment) getActivity().getSupportFragmentManager().findFragmentByTag("WalletFragment");
                            walletFragment.doBankStuff();
                            walletFragment.continueLongClickProcess();
                        }
                    }
                });
                if (!calledFromWallet) {
                    sampleSwitchLayout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            manageAccount(v);
                            return true;
                        }
                    });
                }
                TextView tvAccountName = sampleSwitchLayout.findViewById(R.id.accountName);
                TextView tvAccountBalance = sampleSwitchLayout.findViewById(R.id.accountBalance);
                TextView tvSelectedDot = sampleSwitchLayout.findViewById(R.id.selectedDot);

                //assign values
                accountName = sharedPref.getString("accountName" + i, null);
                accountBalance = sharedPref.getString("accountBalance" + i, null);
                selectedAccName = sharedPref.getString("selectedAccName", "n/a");
                tvAccountName.setText(accountName);
                tvAccountBalance.setText(currency + accountBalance);
                if (selectedAccName.equals(accountName)) tvSelectedDot.setVisibility(View.VISIBLE);
                else tvSelectedDot.setVisibility(View.INVISIBLE);
                sampleSwitchLayout.setTag("account" + i);
                baseLayout.addView(sampleSwitchLayout);
            }
        }

        //add account button
        if (!sharedPref.getBoolean("chooseAccFromWallet", false)) {
            Button btnAddAccount = new Button(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 8, 0, 0);
            btnAddAccount.setLayoutParams(params);
            btnAddAccount.setText(R.string.add_acc);
            btnAddAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addAccount();
                }
            });
            LinearLayout baseLayout = view.findViewById(R.id.dialogChooseAcc);
            baseLayout.addView(btnAddAccount);
        }
    }

    private void addAccount() {
        int accLimit;
        if (sharedPref.getBoolean("MyWalletPro", false)) accLimit = 50;
        else accLimit = 4;

        if (((ViewGroup) view).getChildCount() < accLimit) {
            Intent intent = new Intent(getActivity(), NewAccount.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reached_acc_limit)
                    .setMessage(R.string.r_a_l_des)
                    .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), UpgradeToPro.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create()
                    .show();
        }
    }

    private void chooseAccount(View v) {
        int accountNo = findAccountNo(v);
        String accountName = sharedPref.getString("accountName" + accountNo, null);
        String accountBalance = sharedPref.getString("accountBalance" + accountNo, null);
        sharedPref.edit().putString("selectedAccName", accountName).apply();
        sharedPref.edit().putString("selectedAccBalance", accountBalance).apply();
        sharedPref.edit().putInt("selectedAccNo", accountNo).apply(); //for future use
        TextView tvSelectedDot = v.findViewById(R.id.selectedDot);
        tvSelectedDot.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "Switched", Toast.LENGTH_SHORT).show();
        thisDialog.dismiss();
    }

    //details, edit, delete operations
    private void manageAccount(View v) {
        int accountNo = findAccountNo(v);
        Log.i(TAG, "accountNo: "+accountNo);
        sharedPref.edit().putInt("manageAccNo", accountNo).apply(); //to use for other operations
        DialogManageAcc dialogManageAcc = new DialogManageAcc();
        dialogManageAcc.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                thisDialog.dismiss();
            }
        });
        dialogManageAcc.show(getActivity().getSupportFragmentManager(), "manage account dialog");
    }

    private int findAccountNo(View v) {
        String selectedAccount = v.getTag().toString();
        int accountNo = 0;
        for (int i = 1; i <= 20; i++) {
            if (selectedAccount.contains(String.valueOf(i))) {
                accountNo = i;
                break;
            }
        }
        return accountNo;
    }
}
