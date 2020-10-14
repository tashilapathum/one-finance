package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogWalletContent extends DialogFragment {
    private View view;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_content, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select content")
                .setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }
}
