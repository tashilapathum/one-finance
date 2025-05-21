package com.tantalum.onefinance.quicklist;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.tantalum.onefinance.Amount;
import com.tantalum.onefinance.Constants;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.categories.CategoriesManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import static android.content.Context.MODE_PRIVATE;

public class DialogAddQuickItem extends DialogFragment {
    private TextInputLayout tilItem;
    private TextInputLayout tilPrice;
    private EditText etItem;
    private EditText etPrice;
    private String item;
    private String price;
    private AlertDialog dialog;
    private Bundle bundle;
    private View view;
    private ChipGroup chipGroup;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_quickitem, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        bundle = this.getArguments();
        if (bundle == null) {
            builder.setTitle(R.string.add_q_item)
                    .setView(view)
                    .setPositiveButton(R.string.add, (dialogInterface, i) -> {})
                    .setNegativeButton(R.string.cancel, null);
        } else {
            builder.setTitle(R.string.edit_q_item)
                    .setView(view)
                    .setPositiveButton(R.string.save, (dialogInterface, i) -> {})
                    .setNegativeButton(R.string.cancel, null);
        }

        tilItem = view.findViewById(R.id.etItem);
        etItem = tilItem.getEditText();
        tilPrice = view.findViewById(R.id.etPrice);
        etPrice = tilPrice.getEditText();
        chipGroup = view.findViewById(R.id.chipGroup);

        loadCategoryChips();
        fillDetails();

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        dialog = (AlertDialog) getDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> onClickAdd());
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

    private void onClickAdd() {
        item = etItem.getText().toString();
        price = etPrice.getText().toString();
        String category = null;
        if (!chipGroup.getCheckedChipIds().isEmpty()) {
            Chip chip = chipGroup.findViewById(chipGroup.getCheckedChipId());
            category = chip.getText().toString() + "###" + chip.getChipBackgroundColor().getDefaultColor();
        }
        else {
            Chip chip = new Chip(getActivity());
            category = getString(R.string.uncategorized) + "###" + chip.getChipBackgroundColor().getDefaultColor();
        }

        if (validateItem() & validatePrice()) {
            dialog.dismiss();
            price = new Amount(requireActivity(), price).getAmountString();
            QuickItem quickItem = new QuickItem(item, price, category);
            if (bundle == null)
                ((QuickListActivity) getActivity()).addItemNEW(quickItem);
            else {
                quickItem.setId((Integer) bundle.get("itemID"));
                ((QuickListActivity) getActivity()).saveItem(quickItem);
            }
        }
    }

    private void fillDetails() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            etItem.setText(bundle.getString("itemName"));
            etPrice.setText(bundle.getString("itemPrice"));
            String category = bundle.getString("itemCategory");
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (category.contains(chip.getText().toString()))
                    chip.setChecked(true);
            }
        }
    }

    public void loadCategoryChips() {
        List<Chip> categories = new CategoriesManager(requireActivity()).getCategoryChips();
        chipGroup = view.findViewById(R.id.chipGroup);
        for (Chip chip : categories) {
            chipGroup.addView(chip);
        }
    }

}
