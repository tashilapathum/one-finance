package com.tantalum.financejournal;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DialogWalletInput extends BottomSheetDialogFragment {
    private View view;
    private int transactionType;

    public DialogWalletInput(int transactionType) {
        this.transactionType = transactionType;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_input, null);

        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());

        //handle views
        switch (transactionType) {
            case Constants.EXPENSE: {
                break;
            }
            case Constants.INCOME: {
                break;
            }
            case Constants.TRANSFER: {
                break;
            }
        }

        dialog.setContentView(view);

        return dialog;

    }
}
