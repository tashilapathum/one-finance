package com.tantalum.onefinance.reports;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.tantalum.onefinance.DatePickerFragment;
import com.tantalum.onefinance.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    public static final String TAG = "Reports";
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_reports);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        /*----------------------------------------------------------------------------------------*/

        viewPager = findViewById(R.id.reports_view_pager);
        TabLayout tabLayout = findViewById(R.id.reports_tab_layout);

        DailyReportsFragment dailyReportsFragment = new DailyReportsFragment();
        WeeklyReportsFragment weeklyReportsFragment = new WeeklyReportsFragment();
        MonthlyReportsFragment monthlyReportsFragment = new MonthlyReportsFragment();
        //ReportsOverviewFragment reportsOverviewFragment = new ReportsOverviewFragment();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(dailyReportsFragment, getString(R.string.daily));
        viewPagerAdapter.addFragment(weeklyReportsFragment, getString(R.string.weekly));
        viewPagerAdapter.addFragment(monthlyReportsFragment, getString(R.string.monthly));
        //viewPagerAdapter.addFragment(reportsOverviewFragment, getString(R.string.overview));
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
        ibClearFilter.setOnClickListener(v -> {
            filtersCard.setVisibility(View.GONE);
            sharedPref.edit().putInt("reports_year", 0).apply();
            sharedPref.edit().putInt("reports_month", 0).apply();
            sharedPref.edit().putInt("reports_week", 0).apply();
            sharedPref.edit().putInt("reports_day", 0).apply();
            viewPagerAdapter.notifyDataSetChanged();
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




















