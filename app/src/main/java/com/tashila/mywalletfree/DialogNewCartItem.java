package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogNewCartItem extends DialogFragment {
    View view;
    EditText etItemName;
    EditText etItemPrice;
    EditText etQuantity;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_cart_item, null);
        etItemName = view.findViewById(R.id.itemName);
        etItemPrice = view.findViewById(R.id.itemPrice);
        etQuantity = view.findViewById(R.id.quantity);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (getActivity().getSupportFragmentManager().findFragmentByTag("edit cart item dialog") == null) {
            builder.setView(view)
                    .setTitle(R.string.add_new_item)
                    .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            addItem();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
        }
        else {
            builder.setView(view)
                    .setTitle(R.string.edit_item)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editItem();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            fillDetails();
        }

        return builder.create();
    }

    private void addItem() {
        String itemName = etItemName.getText().toString();
        String itemPrice = etItemPrice.getText().toString();
        String strQuantity = etQuantity.getText().toString();
        int quantity = 1;
        if (!strQuantity.isEmpty())
            quantity = Integer.parseInt(strQuantity);
        if (itemPrice.isEmpty())
            itemPrice = "0.00";
        if (!itemName.isEmpty()) {
            CartFragment.getInstance().addItem(itemName, itemPrice, quantity);
        }
    }

    private void fillDetails() {
        Bundle bundle = this.getArguments();
        String itemName = bundle.getString("cart itemName");
        String itemPrice = bundle.getString("cart itemPrice");
        int quantity = bundle.getInt("cart quantity");

        etItemName.setText(itemName);
        etItemPrice.setText(itemPrice);
        etQuantity.setText(String.valueOf(quantity));
    }

    private void editItem() {
        Bundle bundle = this.getArguments();
        int dbID = bundle.getInt("cart dbID");
        String oldItemPrice = bundle.getString("cart itemPrice");
        int oldQuantity = bundle.getInt("cart quantity");
        boolean isChecked = bundle.getBoolean("cart isChecked");
        String itemName = etItemName.getText().toString();
        String newItemPrice = etItemPrice.getText().toString();
        String strQuantity = etQuantity.getText().toString();
        int newQuantity;
        if (!strQuantity.isEmpty())
            newQuantity = Integer.parseInt(strQuantity);
        else
            newQuantity = 1;
        if (newItemPrice.isEmpty())
            newItemPrice = "0.00";
        if (!itemName.isEmpty()) {
            CartFragment.getInstance().updateItem(dbID, itemName, oldItemPrice, newItemPrice, oldQuantity, newQuantity, isChecked);
        }
    }
}
