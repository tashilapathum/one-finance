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
import com.tantalum.onefinance.Constants;
import com.tantalum.onefinance.R;

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

        DecimalFormat df = new DecimalFormat("#.00");
        if (validateItem() & validatePrice()) {
            dialog.dismiss();
            price = df.format(Double.parseDouble(price));
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
        String allCategories = getActivity().getSharedPreferences("myPref", MODE_PRIVATE)
                .getString(Constants.SP_CATEGORIES, null);
        List<String> categories = new ArrayList<>();
        if (allCategories == null) { //for first time loading
            //assign colors
            categories.add("Food###" + (int) (Math.random() * 1000000000));
            categories.add("Transport###" + (int) (Math.random() * 1000000000));
            categories.add("Clothes###" + (int) (Math.random() * 1000000000));
            categories.add("Education###" + (int) (Math.random() * 1000000000));
            categories.add("Other###" + (int) (Math.random() * 1000000000));

            //save
            StringBuilder allCategoriesBuilder = new StringBuilder();
            for (String category : categories)
                allCategoriesBuilder.append(category).append("~~~");
            getActivity().getSharedPreferences("myPref", MODE_PRIVATE).edit()
                    .putString(Constants.SP_CATEGORIES, allCategoriesBuilder.toString()).apply();
        } else
            categories.addAll(Arrays.asList(allCategories.split("~~~")));

        chipGroup = view.findViewById(R.id.chipGroup);
        for (String categoryItem : categories) {
            Chip chip = new Chip(getActivity());
            chip.setText(categoryItem.split("###")[0]);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Integer.parseInt(categoryItem.split("###")[1])));
            chip.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> chip.setChecked(isChecked));
            chipGroup.addView(chip);
        }
    }

}
