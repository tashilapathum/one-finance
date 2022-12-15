package com.tantalum.onefinance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import com.tantalum.onefinance.reports.ReportsActivity;
import com.tantalum.onefinance.settings.SettingsActivity;
import com.tantalum.onefinance.transactions.TransactionItem;
import com.tantalum.onefinance.transactions.TransactionsActivity;
import com.tantalum.onefinance.transactions.TransactionsViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "CategoriesActivity";
    private SharedPreferences sharedPref;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private List<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        /*------------------------------Essential for every activity------------------------------*/
        Toolbar toolbar;
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            setTheme(R.style.AppThemeDark);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_categories);
            View layout = findViewById(R.id.drawer_layout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
            toolbar = findViewById(R.id.toolbar);
            toolbar.setBackground(getDrawable(R.color.colorToolbarDark));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_categories);
            toolbar = findViewById(R.id.toolbar);
        }

        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (sharedPref.getBoolean("MyWalletPro", false)) {
            View navHeader = navigationView.getHeaderView(0);
            TextView tvAppName = navHeader.findViewById(R.id.appName);
            tvAppName.setText(R.string.my_wallet_pro);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /*----------------------------------------------------------------------------------------*/

        loadCategoryChips();
        drawChart();

    }

    public void loadCategoryChips() {
        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        chipGroup.removeAllViews();

        String allCategories = sharedPref.getString(Constants.SP_CATEGORIES, null);
        categories = new ArrayList<>();
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
        chip.setOnClickListener(v -> new DialogAddCategory(categories).show(getSupportFragmentManager(), "add category dialog"));
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_home)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("showPinScreen", false);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_recent_trans: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_recent_trans)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, TransactionsActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_categories: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_categories)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, CategoriesActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_reports: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_reports)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, ReportsActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_settings: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_settings)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_pro: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_pro)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, UpgradeToProActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_share: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_share)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Share One Finance");
                    intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.tantalum.onefinance");
                    startActivity(Intent.createChooser(intent, "Share One Finance"));
                }
                break;
            }
            case R.id.nav_about: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_about)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, AboutActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_exit: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_exit)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    sharedPref.edit().putBoolean("exit", true).apply();
                    finishAndRemoveTask();
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_categories);
        if (sharedPref.getBoolean("exit", false))
            finishAndRemoveTask();
    }
}