package com.tantalum.financejournal;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.larswerkman.holocolorpicker.ColorPicker;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DialogAddCategory extends DialogFragment {
    private View view;
    private List<String> items;

    public DialogAddCategory(List<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_category, null);

        EditText etColor = view.findViewById(R.id.etColor);
        ColorPicker colorPicker = view.findViewById(R.id.colorPicker);
        colorPicker.setColor((int) (Math.random() * 1000000000));
        colorPicker.setOldCenterColor(colorPicker.getColor());
        etColor.setText("#" + Integer.toHexString(colorPicker.getColor())); //initial color

        colorPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                etColor.setText("#" + Integer.toHexString(colorPicker.getColor()));
            }
        });

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getActivity())
                .setTitle("New category")
                .setView(view)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String categoryName = ((EditText) view.findViewById(R.id.etCategory)).getText().toString();
                        if (!categoryName.isEmpty()) {
                            int categoryColor = colorPicker.getColor();
                            String categoryItem = categoryName + "###" + categoryColor;
                            items.add(categoryItem);
                            StringBuilder allCategories = new StringBuilder();
                            for (String item : items)
                                allCategories.append(item).append("~~~");
                            getActivity().getSharedPreferences("myPref", MODE_PRIVATE).edit()
                                    .putString(Constants.SP_CATEGORIES, allCategories.toString()).apply();
                            ((CategoriesActivity) getActivity()).loadCategoryChips();
                            Toast.makeText(getActivity(), getString(R.string.added), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        return dialogBuilder.create();
    }
}
