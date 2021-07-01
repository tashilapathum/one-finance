package com.tantalum.financejournal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.resources.TextAppearanceConfig;
import com.larswerkman.holocolorpicker.ColorPicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CategoriesActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        //sharedPref.edit().remove(Constants.SP_CATEGORIES).apply();

        //language
        String language = sharedPref.getString("language", "english");
        if (language.equals("සිංහල")) {
            Locale locale = new Locale("si");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        //theme
        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_categories);
            View layout = findViewById(R.id.rootLayout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_categories);
        }

        loadCategoryChips();
    }

    public void loadCategoryChips() {
        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        chipGroup.removeAllViews();

        String allCategories = sharedPref.getString(Constants.SP_CATEGORIES, null);
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
            getSharedPreferences("myPref", MODE_PRIVATE).edit()
                    .putString(Constants.SP_CATEGORIES, allCategoriesBuilder.toString()).apply();
        } else
            categories.addAll(Arrays.asList(allCategories.split("~~~")));

        for (String categoryItem : categories) {
            Chip chip = new Chip(this);
            chip.setText(categoryItem.split("###")[0]);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Integer.parseInt(categoryItem.split("###")[1])));
            chip.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialAlertDialogBuilder(CategoriesActivity.this)
                            .setTitle(getString(R.string.confirm))
                            .setMessage(getString(R.string.confirm_item_delete))
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //remove item from list
                                    StringBuilder allCategories = new StringBuilder();
                                    String removingCat = ((Chip) v).getText().toString();
                                    for (int i = 0; i < categories.size(); i++)
                                        if (!categories.get(i).contains(removingCat))
                                            allCategories.append(categories.get(i)).append("~~~");

                                    //save list as the string
                                    getSharedPreferences("myPref", MODE_PRIVATE).edit()
                                            .putString(Constants.SP_CATEGORIES, allCategories.toString()).apply();
                                    loadCategoryChips();
                                    Toast.makeText(CategoriesActivity.this,
                                            getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            });
            chipGroup.addView(chip);
        }

        //add category button
        Chip chip = new Chip(this);
        chip.setText(getString(R.string.add));
        chip.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
        chip.setTextColor(getResources().getColor(R.color.colorAccent));
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogAddCategory(categories).show(getSupportFragmentManager(), "add category dialog");
            }
        });
        chipGroup.addView(chip);
    }

    public void goBack(View view) {
        finish();
    }

}