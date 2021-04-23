package com.tashila.mywalletfree.cart;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tashila.mywalletfree.R;
import com.tashila.mywalletfree.cart.CartFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogNewCartItem extends BottomSheetDialogFragment {
    private BottomSheetDialog dialog = null;
    private EditText etItemName;
    private EditText etItemPrice;
    private EditText etQuantity;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_cart_item, null);
        etItemName = view.findViewById(R.id.itemName);
        etItemPrice = view.findViewById(R.id.itemPrice);
        etQuantity = view.findViewById(R.id.quantity);
        Button btnAdd = view.findViewById(R.id.addCartItem);
        Button btnCancel = view.findViewById(R.id.cancel);
        dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(view);

        if (getActivity().getSupportFragmentManager().findFragmentByTag("edit cart item dialog") == null) {
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addItem();
                }
            });
        }
        else {
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editItem();
                }
            });
            btnAdd.setText(R.string.save);
            fillDetails();
        }
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        return dialog;
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
        dialog.cancel();
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
        dialog.cancel();
    }
}
