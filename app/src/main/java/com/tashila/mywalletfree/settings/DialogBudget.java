package com.tashila.mywalletfree.settings;

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
import com.tashila.mywalletfree.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogBudget extends DialogFragment {
    private View view3;
    private EditText editBudget;
    SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view3 = inflater.inflate(R.layout.dialog_budget, null);
        editBudget = view3.findViewById(R.id.editBudget);
        builder
                .setView(view3)
                .setTitle(R.string.edit_monthly_budget)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveBudget();
                    }
                });

        //load saved data
        editBudget.setText(sharedPref.getString("monthlyBudget", ""));
        return builder.create();
    }

    private void saveBudget() {
        String monthlyBudget = editBudget.getText().toString().replace(",", ".");
        if (!monthlyBudget.isEmpty())
            sharedPref.edit().putString("monthlyBudget", monthlyBudget).apply();
        else
            sharedPref.edit().putString("monthlyBudget", "0.00").apply();

        Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_SHORT).show();
    }
}
