package com.tantalum.financejournal.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.tantalum.financejournal.R;
import com.tantalum.financejournal.wallet.WalletFragment;

public class DialogWalletInput extends BottomSheetDialogFragment {
    private BottomSheetDialog dialog;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDescr;
    private EditText etAmount;
    private EditText etDescription;
    private String amount;
    private String description;
    private SharedPreferences sharedPref;
    private MaterialButton btnEarned;
    private MaterialButton btnSpent;
    private MaterialButton btnTransfer;
    private WalletFragment walletFragment;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.wallet_input, null);
        view.setPadding(24, 24, 24, 24);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(view);

        tilAmount = view.findViewById(R.id.editAmnt);
        etAmount = tilAmount.getEditText();
        tilDescr = view.findViewById(R.id.editDescr);
        etDescription = tilDescr.getEditText();

        walletFragment = ((WalletFragment) getParentFragment());

        btnEarned = view.findViewById(R.id.btnEarned);
        btnSpent = view.findViewById(R.id.btnSpent);
        btnTransfer = view.findViewById(R.id.btnTransfer);
        setupButtons();

        btnEarned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(v);
            }
        });
        btnSpent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(v);
            }
        });
        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(v);
            }
        });

        btnEarned.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleLongClick(v);
                return true;
            }
        });
        btnSpent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleLongClick(v);
                return true;
            }
        });
        btnTransfer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleLongClick(v);
                return true;
            }
        });

        return dialog;
    }

    private void handleClick(View button) {
        if (button.getId() == R.id.btnTransfer) {
            if (validateAmount()) {
                amount = etAmount.getText().toString();
                description = etDescription.getText().toString();
                walletFragment.setTexts(amount, description);
                walletFragment.clickButton((MaterialButton) button);
                if (sharedPref.getBoolean("tapToHideEnabled", false))
                    clearInputs();
                else
                    dialog.cancel();
            }
        } else if (validateAmount() & validateDescr()) {
            amount = etAmount.getText().toString();
            description = etDescription.getText().toString();
            walletFragment.setTexts(amount, description);
            walletFragment.clickButton((MaterialButton) button);
            if (sharedPref.getBoolean("tapToHideEnabled", false))
                clearInputs();
            else
                dialog.cancel();
        }
    }

    private void handleLongClick(View button) {
        if (button.getId() == R.id.btnTransfer) {
            if (validateAmount()) {
                amount = etAmount.getText().toString();
                description = etDescription.getText().toString();
                walletFragment.setTexts(amount, description);
                walletFragment.longClickButton((MaterialButton) button);
                if (sharedPref.getBoolean("tapToHideEnabled", false))
                    clearInputs();
                else
                    dialog.cancel();
            }
        } else if (validateAmount() & validateDescr()) {
            amount = etAmount.getText().toString();
            description = etDescription.getText().toString();
            walletFragment.setTexts(amount, description);
            walletFragment.longClickButton((MaterialButton) button);
            if (sharedPref.getBoolean("tapToHideEnabled", false))
                clearInputs();
            else
                dialog.cancel();
        }
    }

    private boolean validateAmount() {
        if (etAmount.getText().toString().isEmpty()) {
            tilAmount.setError(getString(R.string.required));
            return false;
        } else {
            tilAmount.setError(null);
            return true;
        }
    }

    private boolean validateDescr() {
        String text = etDescription.getText().toString();
        if (text.isEmpty()) {
            tilDescr.setError(getString(R.string.required));
            return false;
        } else {
            if (text.contains("~~~") || text.contains(",,,")) {
                tilDescr.setError("~~~ and ,,, are not allowed");
                return false;
            } else {
                tilDescr.setError(null);
                return true;
            }
        }
    }

    private void setupButtons() {
        String buttonType = sharedPref.getString("buttonType", "labelOnly");
        if (buttonType.equals("labelOnly")) {
            btnEarned.setIcon(null);
            btnSpent.setIcon(null);
            btnTransfer.setIcon(null);
        } else if (buttonType.equals("iconOnly")) {
            btnEarned.setText(null);
            btnEarned.setIconPadding(0);
            btnEarned.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            btnSpent.setText(null);
            btnSpent.setIconPadding(0);
            btnSpent.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            btnTransfer.setText(null);
            btnTransfer.setIconPadding(0);
            btnTransfer.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        }
    }

    private void clearInputs() {
        etAmount.setText("");
        etDescription.setText("");
        etAmount.requestFocus();
    }
}
