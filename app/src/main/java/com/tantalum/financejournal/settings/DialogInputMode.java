package com.tantalum.financejournal.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tantalum.financejournal.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogInputMode extends DialogFragment {
    private View view;
    private RadioGroup radioGroup;
    private SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_input_mode, null);
        radioGroup = view.findViewById(R.id.rgInputMode);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity())
                .setView(view)
                .setTitle(R.string.select_input_mode)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickSave();
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        //load saved data
        String inputMode = sharedPref.getString("inputMode", null);
        if (inputMode != null)
            if (inputMode.equals("floating"))
                radioGroup.check(radioGroup.getChildAt(1).getId());

        return builder.create();
    }

    private void onClickSave() {
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.classic)
            sharedPref.edit().putString("inputMode", "classic").apply();
        else
            sharedPref.edit().putString("inputMode", "floating").apply();
        Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_SHORT).show();
    }
}
