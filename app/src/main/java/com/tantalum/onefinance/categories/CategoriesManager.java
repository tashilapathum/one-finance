package com.tantalum.onefinance.categories;

import static com.tantalum.onefinance.Constants.SHARED_PREF;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;

import com.google.android.material.chip.Chip;
import com.tantalum.onefinance.Constants;
import com.tantalum.onefinance.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoriesManager {
    private final SharedPreferences pref;
    @androidx.annotation.NonNull
    private final Context context;

    public CategoriesManager(Context context) {
        pref = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        this.context = context;
    }

    public List<String> getCategoryItems() {
        String allCategories = pref.getString(Constants.SP_CATEGORIES, null);
        List<String> categories = new ArrayList<>();
        if (allCategories == null) { //for first time loading
            //assign colors
            categories.add("Food###" + generateRandomColor());
            categories.add("Transport###" + generateRandomColor());
            categories.add("Clothes###" + generateRandomColor());
            categories.add("Education###" + generateRandomColor());
            categories.add("Other###" + generateRandomColor());

            //save
            StringBuilder allCategoriesBuilder = new StringBuilder();
            for (String category : categories)
                allCategoriesBuilder.append(category).append("~~~");
            pref.edit().putString(Constants.SP_CATEGORIES, allCategoriesBuilder.toString()).apply();
        } else
            categories.addAll(Arrays.asList(allCategories.split("~~~")));

        return categories;
    }

    public static int generateRandomColor() {
        return ((0x77 << 24) | (int) (Math.random() * 0xFFFFFF));
    }

    public List<String> getCategoryNames() {
        List<String> categoryItems = getCategoryItems();
        List<String> categoryNames = new ArrayList<>();
        for (String category : categoryItems)
            categoryNames.add(category.split("###")[0]);

        return categoryNames;
    }

    public List<Chip> getCategoryChips() {
        List<Chip> categories = new ArrayList<>();
        for (String categoryItem : getCategoryItems()) {
            Chip chip = new Chip(context);
            chip.setText(categoryItem.split("###")[0]);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Integer.parseInt(categoryItem.split("###")[1])));
            chip.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> chip.setChecked(isChecked));
            categories.add(chip);
        }

        return categories;
    }

}
