package com.tashila.mywalletfree;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogMultiInterests extends DialogFragment {
    private SharedPreferences sharedPref;
    private View v;
    private int accNo;
    private AlertDialog dialog;

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

        //that "same logic" mentioned in NewAccount
        for (int i = 1; i <= 20; i++) {
            if (!sharedPref.getBoolean("isAccountSlot" + i + "Taken", false))
                accNo = i; //when creating a new account
            if (i == 20)
                accNo = sharedPref.getInt("manageAccNo", 0); //when coming from edit account
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        prepareForEditing();
        dialog = builder.create();
        return dialog;
    }

    private void saveData() {
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

            //for account details section
            String interestStr = sharedPref.getString("annualInterestStr", "");
            interestStr = interestStr + minAmount + " - " + maxAmount + " -> " + interest + "\n";
            sharedPref.edit().putString("annualInterestStr", interestStr).apply();

            //save
            sharedPref.edit().putString("account" + accNo + "MinAmount" + i, minAmount).apply();
            sharedPref.edit().putString("account" + accNo + "MaxAmount" + i, maxAmount).apply();
            sharedPref.edit().putString("account" + accNo + "Interest" + i, interest).apply();
            sharedPref.edit().putInt("noOfInterestRanges"+accNo, i).apply();

            sharedPref.edit().putBoolean("hasMultiInterests"+i, true).apply();
            sharedPref.edit().putBoolean("addedMultiInterests", true).apply();
            Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();

            //save values temporarily in case of reopening dialog
            sharedPref.edit().putString("tempMinAmount" + i, minAmount).apply();
            sharedPref.edit().putString("tempMaxAmount" + i, maxAmount).apply();
            sharedPref.edit().putString("tempInterest" + i, interest).apply();
            sharedPref.edit().putBoolean("isTempMultiAvailable", true).apply();
        }
    }

    private void prepareForEditing() {
        AccountHandler accountHandler = new AccountHandler(getActivity());
        for (int i = 1; i <= 5; i++) {
            EditText etMinAmount = v.findViewById(getActivity().getResources()
                    .getIdentifier("min" + i, "id", getActivity().getPackageName()));
            EditText etMaxAmount = v.findViewById(getActivity().getResources()
                    .getIdentifier("max" + i, "id", getActivity().getPackageName()));
            EditText etInterest = v.findViewById(getActivity().getResources()
                    .getIdentifier("i" + i, "id", getActivity().getPackageName()));

            //when editing a previously created account
            if (sharedPref.getBoolean("reqEditing", true)) {
                accountHandler.setDetail(etMinAmount, "Account" + accNo + "minAmount" + i, true);
                accountHandler.setDetail(etMaxAmount, "Account" + accNo + "maxAmount" + i, true);
                accountHandler.setDetail(etInterest, "Account" + accNo + "interest" + i, true);
            }

            //when reopening the dialog of new account
            if (sharedPref.getBoolean("isTempMultiAvailable", false)) {
                accountHandler.setDetail(etMinAmount, "tempMinAmount"+i, true);
                accountHandler.setDetail(etMaxAmount, "tempMaxAmount"+i, true);
                accountHandler.setDetail(etInterest, "tempInterest"+i, true);
            }
        }
    }

    private void clearMultiInterests() {
        sharedPref.edit().putBoolean("addedMultiInterests", false).apply();
        Toast.makeText(getActivity(), R.string.cleared, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
}
