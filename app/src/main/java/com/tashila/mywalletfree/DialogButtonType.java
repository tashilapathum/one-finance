package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogButtonType extends DialogFragment {
    private View view;
    private RadioGroup radioGroup;
    private SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_button_type, null);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        radioGroup = view.findViewById(R.id.radioGroup);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.select_type)
                .setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickSave();
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        String buttonType = sharedPref.getString("buttonType", "labelOnly");
        if (buttonType.equals("iconAndLabel"))
            radioGroup.check(radioGroup.getChildAt(0).getId());
        else if (buttonType.equals("iconOnly"))
            radioGroup.check(radioGroup.getChildAt(1).getId());

        return builder.create();
    }

    private void onClickSave() {
        int checkedId = radioGroup.getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.iconAndLabel: {
                sharedPref.edit().putString("buttonType", "iconAndLabel").apply();
                break;
            }
            case R.id.iconOnly: {
                sharedPref.edit().putString("buttonType", "iconOnly").apply();
                break;
            }
            case R.id.labelOnly: {
                sharedPref.edit().putString("buttonType", "labelOnly").apply();
                break;
            }
        }
        Toast.makeText(getActivity(), getString(R.string.saved), Toast.LENGTH_SHORT).show();
    }

}
