package com.tantalum.financejournal.wallet;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tantalum.financejournal.R;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogUpdateBalance extends DialogFragment {
    private Context context;
    private SharedPreferences sharedPref;
    private String newBalance;
    public static final String TAG = "DialogUpdateBalance";


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_update_balance, null);
        sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        final EditText etBalance = view.findViewById(R.id.editBalance);
        etBalance.setText(sharedPref.getString("balance", "0.00"));
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setView(view)
                .setTitle(R.string.edit_balance)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        newBalance = etBalance.getText().toString().replace(",", ".");
                        Log.i(TAG, "newBalance.isEmpty(): " + newBalance.isEmpty());
                        if (!newBalance.isEmpty()) {
                            DecimalFormat df = new DecimalFormat("#.00");
                            newBalance = df.format(Double.parseDouble(newBalance));
                            WalletFragmentNEW.getInstance().setNewBalance(newBalance);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    public DialogUpdateBalance(Context context) {
        this.context = context;
    }
}
