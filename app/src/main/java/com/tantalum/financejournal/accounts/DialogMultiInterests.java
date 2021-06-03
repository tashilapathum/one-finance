package com.tantalum.financejournal.accounts;

import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tantalum.financejournal.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogMultiInterests extends DialogFragment {
    private SharedPreferences sharedPref;
    private View v;
    private AlertDialog dialog;
    private Account account;
    private AccountsViewModel accountsViewModel;

    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null)
            onDismissListener.onDismiss(dialog);
    }

    public DialogMultiInterests(Account account) {
        this.account = account;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        accountsViewModel = new AccountsViewModel(getActivity().getApplication());
        account = getSelectedAccount();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        v = inflater.inflate(R.layout.dialog_multi_interests, null);
        builder.setView(v)
                .setTitle(R.string.multi_interests)
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveData();
                    }
                })
                .setNegativeButton(R.string.clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearMultiInterests();
                    }
                })
                .setNeutralButton(R.string.cancel, null);

        prepareForEditing();
        dialog = builder.create();
        return dialog;
    }

    private void saveData() {
        String multiInterests = null;
        for (int i = 1; i <= 5; i++) {
            EditText etMinAmount = v.findViewById(getActivity().getResources()
                    .getIdentifier("min" + i, "id", getActivity().getPackageName()));
            EditText etMaxAmount = v.findViewById(getActivity().getResources()
                    .getIdentifier("max" + i, "id", getActivity().getPackageName()));
            EditText etInterest = v.findViewById(getActivity().getResources()
                    .getIdentifier("i" + i, "id", getActivity().getPackageName()));

            String minAmount = etMinAmount.getText().toString();
            String maxAmount = etMaxAmount.getText().toString();
            String interest = etInterest.getText().toString();

            //validate
            if (minAmount.isEmpty() && maxAmount.isEmpty() && interest.isEmpty()) break;
            if (minAmount.equals("0") && maxAmount.equals("0") && interest.equals("0")) break;
            if (minAmount.isEmpty() || maxAmount.isEmpty() || interest.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill all 3 fields of each line", Toast.LENGTH_LONG).show();
                break;
            }

            multiInterests = account.getInterestRate() + minAmount + "~" + maxAmount + "~" + interest + "~~~";
            account.setMultiInterest(true);
            multiInterests = multiInterests.replace("null", "");
            account.setInterestRate(multiInterests);
            accountsViewModel.update(account);

            sharedPref.edit().putBoolean("addedMultiInterests", true).apply();
            Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();

            //save values temporarily in case of reopening dialog
            sharedPref.edit().putString("tempMinAmount" + i, minAmount).apply();
            sharedPref.edit().putString("tempMaxAmount" + i, maxAmount).apply();
            sharedPref.edit().putString("tempInterest" + i, interest).apply();
            sharedPref.edit().putBoolean("isTempMultiAvailable", true).apply();
        }
        sharedPref.edit().putString("multiInterests", multiInterests).apply();
    }

    private void prepareForEditing() {
        for (int i = 1; i <= 5; i++) {
            EditText etMinAmount = v.findViewById(getActivity().getResources()
                    .getIdentifier("min" + i, "id", getActivity().getPackageName()));
            EditText etMaxAmount = v.findViewById(getActivity().getResources()
                    .getIdentifier("max" + i, "id", getActivity().getPackageName()));
            EditText etInterest = v.findViewById(getActivity().getResources()
                    .getIdentifier("i" + i, "id", getActivity().getPackageName()));

            //when editing a previously created account
            if (account != null && account.isMultiInterest()) {
                String[] interestList = account.getInterestRate().split("~~~");
                if (sharedPref.getBoolean("reqEditing", true)) {
                    etMinAmount.setText(interestList[i].split("~")[0]);
                    etMaxAmount.setText(interestList[i].split("~")[1]);
                    etInterest.setText(interestList[i].split("~")[2]);
                }
            }

            //when reopening the dialog of new account
            if (sharedPref.getBoolean("isTempMultiAvailable", false)) {
                setDetail(etMinAmount, "tempMinAmount" + i);
                setDetail(etMaxAmount, "tempMaxAmount" + i);
                setDetail(etInterest, "tempInterest" + i);
            }
        }
    }

    private void clearMultiInterests() {
        sharedPref.edit().putBoolean("addedMultiInterests", false).apply();
        Toast.makeText(getActivity(), R.string.cleared, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    private Account getSelectedAccount() {
        AccountsViewModel accountsViewModel = new AccountsViewModel(getActivity().getApplication());
        Account account = null;
        List<Account> accountList = accountsViewModel.getAllAccounts();
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).isSelected())
                account = accountList.get(i);
        }
        if (account != null)
            return account;
        else
            return new Account(null, null, null, null,
                    false, 0, null, null, null, false);
    }

    private void setDetail(View view, String stringKey) {
        String detail = sharedPref.getString(stringKey, null);
        EditText editText = (EditText) view;
        editText.setText(detail);
    }
}
