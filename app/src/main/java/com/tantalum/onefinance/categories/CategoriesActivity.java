package com.tantalum.onefinance.categories;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tantalum.onefinance.Amount;
import com.tantalum.onefinance.Constants;
import com.tantalum.onefinance.DialogAddCategory;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.pro.UpgradeHandler;
import com.tantalum.onefinance.pro.UpgradeToProActivity;
import com.tantalum.onefinance.transactions.TransactionItem;
import com.tantalum.onefinance.transactions.TransactionsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoriesActivity extends AppCompatActivity {
    public static final String TAG = "CategoriesActivity";
    private SharedPreferences sharedPref;
    private List<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        /*------------------------------Essential for every activity------------------------------*/
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        //language
        String language = sharedPref.getString("language", "english");
        Locale locale;
        if (language.equals("සිංහල"))
            locale = new Locale("si");
        else
            locale = new Locale("en");

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        /*----------------------------------------------------------------------------------------*/

        loadCategoryChips();
        drawChart();

    }

    public void loadCategoryChips() {
        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        chipGroup.removeAllViews();

        categories = new CategoriesManager(this).getCategoryItems();

        for (String categoryItem : categories) {
                Chip chip = new Chip(this);
                chip.setText(categoryItem.split("###")[0]);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Integer.parseInt(categoryItem.split("###")[1])));
                chip.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
                chip.setCloseIconVisible(true);
                chip.setChipStrokeWidth(0f);
                chip.setOnCloseIconClickListener(v -> new MaterialAlertDialogBuilder(CategoriesActivity.this)
                        .setTitle(getString(R.string.confirm))
                        .setMessage(getString(R.string.confirm_item_delete))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            //remove item from list
                            StringBuilder allCategories1 = new StringBuilder();
                            String removingCat = ((Chip) v).getText().toString();
                            for (int i = 0; i < categories.size(); i++)
                                if (!categories.get(i).contains(removingCat))
                                    allCategories1.append(categories.get(i)).append("~~~");

                            //nullify if no categories
                            String finalCats;
                            if (allCategories1.toString().replace("~~~", "").isEmpty())
                                finalCats = null;
                            else
                                finalCats = allCategories1.toString();

                            //save list as the string
                            getSharedPreferences("myPref", MODE_PRIVATE).edit()
                                    .putString(Constants.SP_CATEGORIES, finalCats).apply();
                            loadCategoryChips();
                            Toast.makeText(CategoriesActivity.this,
                                    getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.no, null)
                        .show());
                chipGroup.addView(chip);
        }

        //add category button
        Chip chip = new Chip(this);
        chip.setText(getString(R.string.add));
        chip.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
        chip.setTextColor(getResources().getColor(R.color.colorAccent));
        chip.setChipStrokeWidth(0f);
        chip.setOnClickListener(v -> {
            if (UpgradeHandler.isProActive(this) || categories.size() < 5)
                new DialogAddCategory(categories).show(getSupportFragmentManager(), "add category dialog");
            else
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.reached_categories_limit)
                        .setMessage(R.string.remove_categories_or_upgrade)
                        .setPositiveButton(R.string.upgrade_to_pro, (dialog, which) -> {
                            Intent intent = new Intent(this, UpgradeToProActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
        });
        chipGroup.addView(chip);
    }

    private void drawChart() {
        //prepare chart
        PieChart pieChart = findViewById(R.id.chart);
        pieChart.setUsePercentValues(true);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterTextSizePixels(48);
        pieChart.setDrawEntryLabels(false);
        pieChart.setHoleColor(getResources().getColor(R.color.colorWhiteTransparent));
        pieChart.setCenterTextColor(getResources().getColor(R.color.colorDivider));
        pieChart.animateY(1000, Easing.EaseOutCirc);

        //prepare data
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(TransactionsViewModel.class);
        List<TransactionItem> transactionsList = transactionsViewModel.getTransactionsList();

        if (transactionsList != null && ! transactionsList.isEmpty()) {
            //derive category names from categories
            String[] categoryNames = new String[categories.size()];
            for (int i = 0; i < categoryNames.length; i++)
                categoryNames[i] = categories.get(i).split("###")[0];

            //derive colors list from categories
            int[] categoryColors = new int[categories.size()];
            for (int i = 0; i < categoryColors.length; i++)
                categoryColors[i] = Integer.parseInt(categories.get(i).split("###")[1]);

            //add each transaction to respective category
            float[] categoryAmounts = new float[categories.size()];
            for (int i = 0; i < categoryAmounts.length; i++)
                for (TransactionItem transaction : transactionsList)
                    if (transaction.getPrefix().equals("-") && transaction.getCategory().split("###")[0].equals(categoryNames[i]))
                        categoryAmounts[i] = (float) (categoryAmounts[i] + transaction.getAmountValue());

            //calculate total
            float total = 0;
            for (float categoryAmount : categoryAmounts)
                total = total + categoryAmount;

            //calculate percentages
            float[] amountPercents = new float[categoryAmounts.length];
            for (int i = 0; i < amountPercents.length; i++)
                amountPercents[i] = categoryAmounts[i] / total * 100;

            //set data to chart
            ArrayList<PieEntry> yValues = new ArrayList<>();
            for (int i = 0; i < categories.size(); i++)
                yValues.add(new PieEntry(amountPercents[i], categoryNames[i], i));
            PieDataSet dataSet = new PieDataSet(yValues, "Categories");
            dataSet.setColors(categoryColors);
            dataSet.setDrawValues(false);
            PieData data = new PieData(dataSet);
            pieChart.setData(data);

            pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    int i = (int) e.getData();
                    String category = categoryNames[i];
                    String amount = new Amount(CategoriesActivity.this, categoryAmounts[i]).getAmountString();
                    pieChart.setCenterText(category + "\n" + amount);
                }

                @Override
                public void onNothingSelected() {
                    pieChart.setCenterText(null);
                }
            });
        }
        else
            pieChart.setNoDataText(getString(R.string.add_cat_trans_to_see_them_in_chart));
    }

    @Override //so the language change works with dark mode
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPref.getBoolean("exit", false))
            finishAndRemoveTask();
    }
}