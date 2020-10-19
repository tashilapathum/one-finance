package com.tashila.mywalletfree;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class DialogWalletInput extends BottomSheetDialogFragment {
    private BottomSheetDialog dialog;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDescr;
    private EditText etAmount;
    private EditText etDescription;
    private String amount;
    private String description;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.wallet_input, null);
        dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(view);

        tilAmount = view.findViewById(R.id.editAmnt);
        etAmount = tilAmount.getEditText();
        amount = "";
        tilDescr = view.findViewById(R.id.editDescr);
        etDescription = tilDescr.getEditText();
        description = "";

        WalletFragment walletFragment = WalletFragment.getInstance();
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                amount = s.toString();

            }
        });
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                description = s.toString();
                walletFragment.setTexts(amount, description);
            }
        });

        MaterialButton btnEarned = view.findViewById(R.id.btnEarned);
        MaterialButton btnSpent = view.findViewById(R.id.btnSpent);
        MaterialButton btnToBank = view.findViewById(R.id.btnTransfer);
        btnEarned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        btnSpent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        btnToBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        return dialog;
    }
}
