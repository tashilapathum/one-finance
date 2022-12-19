package com.tantalum.onefinance.categories;

import static com.tantalum.onefinance.Constants.SHARED_PREF;

import android.content.Context;
import android.content.SharedPreferences;

import com.tantalum.onefinance.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoriesManager {
    private final SharedPreferences pref;

    public CategoriesManager(Context context) {
        pref = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
    }

    public List<String> getCategoryItems() {
        String allCategories = pref.getString(Constants.SP_CATEGORIES, null);
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
            pref.edit().putString(Constants.SP_CATEGORIES, allCategoriesBuilder.toString()).apply();
        } else
            categories.addAll(Arrays.asList(allCategories.split("~~~")));

        return categories;
    }

    public List<String> getCategoryNames() {
        List<String> categoryItems = getCategoryItems();
        List<String> categoryNames = new ArrayList<>();
        for (String category : categoryItems)
            categoryNames.add(category.split("###")[0]);

        return categoryNames;
    }

}
