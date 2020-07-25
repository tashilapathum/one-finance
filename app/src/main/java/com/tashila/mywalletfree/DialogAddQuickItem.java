package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogAddQuickItem extends DialogFragment {
    private View view;
    private TextInputLayout tilItem;
    private TextInputLayout tilPrice;
    private EditText etItem;
    private EditText etPrice;
    private String item;
    private String price;
    private AlertDialog dialog;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_quickitem, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_quick_list_item)
                .setView(view)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton(R.string.cancel, null);

        tilItem = view.findViewById(R.id.etItem);
        etItem = tilItem.getEditText();
        tilPrice = view.findViewById(R.id.etPrice);
        etPrice = tilPrice.getEditText();

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        dialog = (AlertDialog) getDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAdd();
            }
        });
    }

    private boolean validateItem() {
        if (item.isEmpty()) {
            tilItem.setError(getActivity().getResources().getString(R.string.required));
            return false;
        } else {
            tilItem.setError(null);
            return true;
        }
    }

    private boolean validatePrice() {
        if (price.isEmpty()) {
            tilPrice.setError(getActivity().getResources().getString(R.string.required));
            return false;
        } else {
            tilPrice.setError(null);
            return true;
        }
    }

    public void onClickAdd() {
        item = etItem.getText().toString();
        price = etPrice.getText().toString();
        if (validateItem() && validatePrice()) {
            dialog.dismiss();
            ((EditQuickList) getActivity()).addItem(item, price, false);
        }
    }

}
