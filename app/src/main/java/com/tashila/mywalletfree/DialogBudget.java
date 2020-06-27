package com.tashila.mywalletfree;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.YearMonth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DialogBudget extends AppCompatDialogFragment {
    private View view3;
    private EditText editBudget;
    SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AndroidThreeTen.init(getActivity());
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        String monthlyBudget = editBudget.getText().toString();
        if (!monthlyBudget.isEmpty()) {
            String weeklyBudget = String.valueOf(Double.parseDouble(monthlyBudget) / 4);
            String dailyBudget = String.valueOf(Double.parseDouble(monthlyBudget) / YearMonth.now().lengthOfMonth());

            sharedPref.edit().putString("monthlyBudget", monthlyBudget).apply();
            sharedPref.edit().putString("weeklyBudget", weeklyBudget).apply();
            sharedPref.edit().putString("dailyBudget", dailyBudget).apply();
        }
        else {
            sharedPref.edit().putString("monthlyBudget", "").apply();
        }
            Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
    }
}
