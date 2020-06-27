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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DialogCurrency extends AppCompatDialogFragment {
    private View view2;
    private EditText editCurrency;
    SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view2 = inflater.inflate(R.layout.dialog_currency, null);
        editCurrency = view2.findViewById(R.id.editCurrency);
        builder
                .setView(view2)
                .setTitle(R.string.add_currency)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveCurrency();
                    }
                });

        //load saved data
        String currency = sharedPref.getString("currency", "");
        editCurrency.setText(currency);
        return builder.create();
    }

    private void saveCurrency() {
        sharedPref.edit().putString("currency", editCurrency.getText().toString()).apply();
        Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
    }
}
