package com.tantalum.financejournal.investments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.tantalum.financejournal.R;

public class InputTextDialog extends BottomSheetDialogFragment {
    private String hint;
    private int inputType;
    private final OnClickAddListener onClickAddListener;

    public InputTextDialog(String hint, int inputType, OnClickAddListener onClickAddListener) {
        this.hint = hint;
        this.inputType = inputType;
        this.onClickAddListener = onClickAddListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_investment_note, null);

        //dialog
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(view);

        //input field
        TextInputLayout textInputLayout = view.findViewById(R.id.textInputLayout);
        textInputLayout.setHint(hint);
        EditText editText = textInputLayout.getEditText();
        editText.setInputType(inputType);

        //onClick
        view.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddListener.onClickAdd(editText.getText().toString());
                dialog.dismiss();
            }
        });

        return dialog;
    }

    interface OnClickAddListener {
        void onClickAdd(String inputText);
    }

}
