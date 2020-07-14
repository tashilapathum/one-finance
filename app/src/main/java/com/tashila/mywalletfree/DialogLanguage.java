package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.DialogInterface;
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

public class DialogLanguage extends DialogFragment {
    private static final String TAG = "DialogLanguage";
    private RadioGroup radioGroup;
    View view1;
    SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view1 = inflater.inflate(R.layout.dialog_language, null);
        sharedPref = getActivity().getSharedPreferences("myPref", MODE_PRIVATE);

        radioGroup = view1.findViewById(R.id.radioGroup);
        builder
                .setView(view1)
                .setTitle(R.string.select_lang)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveLangRadio();
                    }
                });
        String lang = sharedPref.getString("language", "english");
        Log.i(TAG, "lang: " + lang);
        if (lang.equals("සිංහල")) radioGroup.check(R.id.sinhala);
        return builder.create();
    }

    private void saveLangRadio() {
        View radioButton = view1.findViewById(radioGroup.getCheckedRadioButtonId());
        int radioId = radioGroup.indexOfChild(radioButton);
        RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
        String language = btn.getText().toString();

        sharedPref.edit().putString("language", language).apply();
        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.restart_for_changes), Toast.LENGTH_SHORT).show();
    }
}
