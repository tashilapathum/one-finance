package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import static android.content.Context.MODE_PRIVATE;

public class DialogChooseHome extends AppCompatDialogFragment {
    private RadioGroup radioGroup;
    private View view1;
    private SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view1 = inflater.inflate(R.layout.dialog_choose_home, null);
        sharedPref = getActivity().getSharedPreferences("myPref", MODE_PRIVATE);

        radioGroup = view1.findViewById(R.id.radioGroup);
        builder
                .setView(view1)
                .setTitle(R.string.set_home_screen)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveHomeRadio();
                    }
                });
        String homeScreen = sharedPref.getString("homeScreen", "wallet");
        if (homeScreen.equals("Wallet")) radioGroup.check(R.id.homeWallet);
        if (homeScreen.equals("Bank")) radioGroup.check(R.id.homeBank);
        if (homeScreen.equals("Cart")) radioGroup.check(R.id.homeCart);
        if (homeScreen.equals("Bills")) radioGroup.check(R.id.homeBills);
        return builder.create();
    }

    private void saveHomeRadio() {
        View radioButton = view1.findViewById(radioGroup.getCheckedRadioButtonId());
        int radioId = radioGroup.indexOfChild(radioButton);
        RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
        String homeScreen = btn.getText().toString();
        if (homeScreen.equals("පසුම්බිය")) homeScreen = "Wallet";
        if (homeScreen.equals("බැංකුව")) homeScreen = "Bank";
        if (homeScreen.equals("ලැයිස්තු")) homeScreen = "Cart";
        if (homeScreen.equals("බිල්පත්")) homeScreen = "Bills";
        sharedPref.edit().putString("homeScreen", homeScreen).apply();
        Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
    }
}
