package com.tantalum.onefinance.reports;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.shreyaspatil.material.navigationview.MaterialNavigationView;
import com.tantalum.onefinance.AboutActivity;
import com.tantalum.onefinance.CategoriesActivity;
import com.tantalum.onefinance.DatePickerFragment;
import com.tantalum.onefinance.MainActivity;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.settings.SettingsActivity;
import com.tantalum.onefinance.transactions.TransactionsActivity;
import com.tantalum.onefinance.UpgradeToProActivity;

import java.util.ArrayList;
import java.util.List;

public class ReportsActivity extends AppCompatActivity implements MaterialNavigationView.OnNavigationItemSelectedListener {
    SharedPreferences sharedPref;
    private DrawerLayout drawer;
    public static final String TAG = "Reports";
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private MaterialNavigationView navigationView;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*------------------------------Essential for every activity------------------------------*/
        Toolbar toolbar;
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            setTheme(R.style.AppThemeDark);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_reports);
            View layout = findViewById(R.id.drawer_layout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
            toolbar = findViewById(R.id.toolbar);
            toolbar.setBackground(getDrawable(R.color.colorToolbarDark));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_reports);
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

        viewPager = findViewById(R.id.reports_view_pager);
        TabLayout tabLayout = findViewById(R.id.reports_tab_layout);

        DailyReportsFragment dailyReportsFragment = new DailyReportsFragment();
        WeeklyReportsFragment weeklyReportsFragment = new WeeklyReportsFragment();
        MonthlyReportsFragment monthlyReportsFragment = new MonthlyReportsFragment();
        ReportsOverviewFragment reportsOverviewFragment = new ReportsOverviewFragment();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(dailyReportsFragment, getString(R.string.daily));
        viewPagerAdapter.addFragment(weeklyReportsFragment, getString(R.string.weekly));
        viewPagerAdapter.addFragment(monthlyReportsFragment, getString(R.string.monthly));
        viewPagerAdapter.addFragment(reportsOverviewFragment, getString(R.string.overview));
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reports_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.pickDate)
            pickDate();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_reports);
        if (sharedPref.getBoolean("exit", false))
            finishAndRemoveTask();

        //reset picked date
        sharedPref.edit().putInt("reports_year", 0).apply();
        sharedPref.edit().putInt("reports_month", 0).apply();
        sharedPref.edit().putInt("reports_week", 0).apply();
        sharedPref.edit().putInt("reports_day", 0).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.edit().putBoolean("exit", false).apply();
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

    private void pickDate() {
        DialogFragment datePicker = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("pickDate", "fromReports");
        datePicker.setArguments(bundle);
        datePicker.show(getSupportFragmentManager(), "reports date picker");

        //date is saved to sharedPref from DatePickerFragment
    }

    public void applyFilter(int year, int month, int week, int dayOfYear, int dayOfMonth) {
        sharedPref.edit().putInt("reports_year", year).apply();
        sharedPref.edit().putInt("reports_month", month).apply();
        sharedPref.edit().putInt("reports_week", week).apply();
        sharedPref.edit().putInt("reports_day", dayOfYear).apply();
        viewPagerAdapter.notifyDataSetChanged();

        MaterialCardView filtersCard = findViewById(R.id.filtersCard);
        filtersCard.setVisibility(View.VISIBLE);
        TextView tvShowingData = findViewById(R.id.showingData);
        tvShowingData.setText("Showing data from: " + " Year " + year + " | Month " + month + " | Week " + week + " | Day " + dayOfMonth);
        ImageButton ibClearFilter = findViewById(R.id.clearFilter);
        ibClearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtersCard.setVisibility(View.GONE);
                sharedPref.edit().putInt("reports_year", 0).apply();
                sharedPref.edit().putInt("reports_month", 0).apply();
                sharedPref.edit().putInt("reports_week", 0).apply();
                sharedPref.edit().putInt("reports_day", 0).apply();
                viewPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> fragmentTitlesList = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitlesList.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitlesList.get(position);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }
}




















