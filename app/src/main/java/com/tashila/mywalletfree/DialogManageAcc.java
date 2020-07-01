package com.tashila.mywalletfree;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogManageAcc extends DialogFragment {
    private View v;
    private Button btnDetails;
    private Button btnEdit;
    private Button btnDelete;
    private SharedPreferences sharedPref;
    public static final String TAG = "DialogManageAcc";
    AlertDialog dialog;

    private DialogInterface.OnDismissListener onDismissListener;

    void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null)
            onDismissListener.onDismiss(dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        v = inflater.inflate(R.layout.dialog_manage_account, null);
        builder.setView(v);

        btnDetails = v.findViewById(R.id.details);
        btnEdit = v.findViewById(R.id.edit);
        btnDelete = v.findViewById(R.id.delete);
        btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDetails();
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEdit();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDelete();
            }
        });

        dialog = builder.create();
        return dialog;
    }

    private void onClickDetails() {
        startActivity(new Intent(getActivity(), AccountDetails.class));
    }

    private void onClickEdit() {
        sharedPref.edit().putBoolean("reqEditing", true).apply();
        startActivity(new Intent(getActivity(), NewAccount.class));
    }

    private void onClickDelete() {
        int manageAccNo = sharedPref.getInt("manageAccNo", 0);
        int selectedAccNo = sharedPref.getInt("selectedAccNo", 0);
        if (manageAccNo == selectedAccNo) {
            Toast.makeText(getActivity(), R.string.acc_in_use, Toast.LENGTH_LONG).show();
        }
        else {
            String accountName = sharedPref.getString("accountName" + manageAccNo, null);
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.confirm)
                    .setMessage(getString(R.string.confirm_part1) + accountName + getString(R.string.confirm_part2))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            delete();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

    @SuppressLint("RestrictedApi")
    private void delete() {
        int manageAccNo = sharedPref.getInt("manageAccNo", 0);
        sharedPref.edit().putBoolean("isAccountSlot" + manageAccNo + "Taken", false).apply(); //clear deleted slot
        sharedPref.edit().putBoolean("isAccountSlot" + manageAccNo + "Deleted", true).apply(); //mark slot as deleted
        new AccountHandler(getActivity()).minusAccount();
        Toast.makeText(getActivity(), R.string.deleted, Toast.LENGTH_SHORT).show();

        dialog.dismiss();
    }
}
