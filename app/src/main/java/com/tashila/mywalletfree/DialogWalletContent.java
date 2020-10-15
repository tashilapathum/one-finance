package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_content, null);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        walletContent = sharedPref.getString("walletContent", null);

        cbQuickList = view.findViewById(R.id.quickList);
        cbToday = view.findViewById(R.id.todayReport);
        cbThisWeek = view.findViewById(R.id.thisWeekReport);
        cbThisMonth = view.findViewById(R.id.thisMonthReport);

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
        sharedPref.edit().putBoolean("walletContentChanged", true).apply(); //to refresh, changes later
        Log.i(TAG, "walletContent string: " + walletContent);
    }

    private void loadSavedData() {
        if (walletContent != null) {
            String[] checkBoxIdsStrings = walletContent.split("~");
            int[] checkBoxIds = new int[checkBoxIdsStrings.length];
            for (int i = 0; i < checkBoxIdsStrings.length; i++)
                checkBoxIds[i] = Integer.parseInt(checkBoxIdsStrings[i]);
            for (int checkBoxId : checkBoxIds) {
                Log.i(TAG, "chID: "+checkBoxId);
                toggleMatchingCheckBox(checkBoxId, cbQuickList, cbToday, cbThisWeek, cbThisMonth);
            }
        }
    }

    private void toggleMatchingCheckBox(int checkBoxId, MaterialCheckBox... checkBoxes) {
        for (MaterialCheckBox checkBox : checkBoxes) {
            Log.i(TAG, "liveID: " + checkBox.getId() + "passedID: " + checkBoxId);
            if (checkBoxId == checkBox.getId())
                checkBox.setChecked(true);
        }
    }
}
