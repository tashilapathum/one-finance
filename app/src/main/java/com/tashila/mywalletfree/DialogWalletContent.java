package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.checkbox.MaterialCheckBox;

public class DialogWalletContent extends DialogFragment {
    public static final String TAG = "DialogWalletContent";
    private SharedPreferences sharedPref;
    private String walletContent;
    private MaterialCheckBox cbQuickList;
    private MaterialCheckBox cbThisMonth;
    private MaterialCheckBox cbToday;
    private MaterialCheckBox cbThisWeek;
    private View view;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_content, null);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        walletContent = sharedPref.getString("walletContent", null);

        cbQuickList = view.findViewById(R.id.quickList);
        cbToday = view.findViewById(R.id.todayReport);
        cbThisWeek = view.findViewById(R.id.thisWeekReport);
        cbThisMonth = view.findViewById(R.id.thisMonthReport);

        //first time only
        if (!sharedPref.getBoolean("walletContentCustomized", false))
            cbQuickList.setChecked(true);

        cbQuickList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleSelection(buttonView, isChecked);
            }
        });
        cbToday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleSelection(buttonView, isChecked);
            }
        });
        cbThisWeek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleSelection(buttonView, isChecked);
            }
        });
        cbThisMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleSelection(buttonView, isChecked);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select content")
                .setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveData(cbQuickList, cbToday, cbThisWeek, cbThisMonth);
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        loadSavedData();

        return builder.create();
    }

    private void saveData(MaterialCheckBox... checkBoxes) {
        walletContent = "";
        for (MaterialCheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked())
                walletContent = walletContent + checkBox.getId() + "~";
        }
        sharedPref.edit().putString("walletContent", walletContent).apply();
        sharedPref.edit().putBoolean("walletContentCustomized", true).apply(); //does not change later

        Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_SHORT).show();
    }

    private void loadSavedData() {
        if (walletContent != null) {
            String[] checkBoxIdsStrings = walletContent.split("~");
            int[] checkBoxIds = new int[checkBoxIdsStrings.length];
            for (int i = 0; i < checkBoxIdsStrings.length; i++)
                checkBoxIds[i] = Integer.parseInt(checkBoxIdsStrings[i]);
            for (int checkBoxId : checkBoxIds)
                toggleMatchingCheckBox(checkBoxId, cbQuickList, cbToday, cbThisWeek, cbThisMonth);
        }
    }

    private void toggleMatchingCheckBox(int checkBoxId, MaterialCheckBox... checkBoxes) {
        for (MaterialCheckBox checkBox : checkBoxes)
            if (checkBoxId == checkBox.getId())
                checkBox.setChecked(true);
    }

    private void handleSelection(CompoundButton checkBox, boolean isChecked) {
        boolean isMyWalletPro = sharedPref.getBoolean("MyWalletPro", false);
        if (isChecked && !isMyWalletPro)
            disableOthers(checkBox.getId(), cbToday, cbQuickList, cbThisWeek, cbThisMonth);
        else
            enableAll(cbToday, cbQuickList, cbThisWeek, cbThisMonth);
    }

    private void disableOthers(int checkedId, MaterialCheckBox... checkBoxes) {
        for (MaterialCheckBox checkBox : checkBoxes)
            if (checkBox.getId() != checkedId) {
                checkBox.setChecked(false);
                checkBox.setClickable(false);
                checkBox.setFocusable(false);
                checkBox.setAlpha(0.5f);
            }
        TextView proNotice = view.findViewById(R.id.proNotice);
        proNotice.setVisibility(View.VISIBLE);
    }

    private void enableAll(MaterialCheckBox... checkBoxes) {
        for (MaterialCheckBox checkBox : checkBoxes) {
            checkBox.setClickable(true);
            checkBox.setFocusable(true);
            checkBox.setAlpha(1f);
        }
        TextView proNotice = view.findViewById(R.id.proNotice);
        proNotice.setVisibility(View.GONE);
    }
}
