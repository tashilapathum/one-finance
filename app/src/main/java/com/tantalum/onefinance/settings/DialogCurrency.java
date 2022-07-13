package com.tantalum.onefinance.settings;

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
import com.mynameismidori.currencypicker.CurrencyPicker;
import com.mynameismidori.currencypicker.CurrencyPickerListener;
import com.tantalum.onefinance.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogCurrency extends DialogFragment {
    private View view2;
    private EditText editCurrency;
    SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
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

        view2.findViewById(R.id.pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrencyPicker picker = CurrencyPicker.newInstance("Select currency");  // dialog title
                picker.setListener(new CurrencyPickerListener() {
                    @Override
                    public void onSelectCurrency(String name, String code, String symbol, int flagDrawableResID) {
                        editCurrency.setText(symbol);
                        picker.dismiss();
                    }
                });
                picker.show(getChildFragmentManager(), "CURRENCY_PICKER");
            }
        });
        return builder.create();
    }

    private void saveCurrency() {
        sharedPref.edit().putString("currency", editCurrency.getText().toString()).apply();
        Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
    }
}
