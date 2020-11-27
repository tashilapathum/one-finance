package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import static android.content.Context.MODE_PRIVATE;

public class DialogTheme extends DialogFragment {
    private static final String TAG = "DialogTheme";
    private RadioGroup radioGroup;
    private View view1;
    private SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view1 = inflater.inflate(R.layout.dialog_theme, null);
        sharedPref = getActivity().getSharedPreferences("myPref", MODE_PRIVATE);

        radioGroup = view1.findViewById(R.id.radioGroup);
        builder
                .setView(view1)
                .setTitle(R.string.select_theme)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveThemeRadio();
                    }
                });
        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) radioGroup.check(R.id.dark);
        return builder.create();
    }

    private void saveThemeRadio() {
        View radioButton = view1.findViewById(radioGroup.getCheckedRadioButtonId());
        int radioId = radioGroup.indexOfChild(radioButton);
        RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
        String theme = btn.getText().toString();
        if (theme.equals("අඳුරු")) theme = "dark";
        sharedPref.edit().putString("theme", theme).apply();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("showPinScreen", false);
        startActivity(intent);
    }
}
