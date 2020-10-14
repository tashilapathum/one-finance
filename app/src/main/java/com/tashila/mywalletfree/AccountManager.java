package com.tashila.mywalletfree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.shreyaspatil.material.navigationview.MaterialNavigationView;

import java.util.List;
import java.util.Locale;

public class AccountManager extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences sharedPref;
    private DrawerLayout drawer;
    private MaterialNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*------------------------------Essential for every activity------------------------------*/
        Toolbar toolbar;
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            setTheme(R.style.AppThemeDark);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_account_manager);
            View layout = findViewById(R.id.drawer_layout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
            toolbar = findViewById(R.id.toolbar);
            toolbar.setBackground(getDrawable(R.color.colorToolbarDark));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_account_manager);
            toolbar = findViewById(R.id.toolbar);
        }

        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_open);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /*----------------------------------------------------------------------------------------*/

        //Button button;
        FloatingActionButton actionButton = findViewById(R.id.actionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickFAB();
            }
        });
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
        navigationView.setCheckedItem(R.id.nav_accounts);
        if (sharedPref.getBoolean("exit", false)) {
            finishAndRemoveTask();
        }

        final AccountsAdapter accountsAdapter = new AccountsAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutAnimation(new AnimationHandler().getSlideUpController());
        recyclerView.setAdapter(accountsAdapter);

        AccountsViewModel accountsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(AccountsViewModel.class);
        accountsViewModel.getAllAccountsLive().observe(this, new Observer<List<Account>>() {
            @Override
            public void onChanged(List<Account> accounts) {
                accountsAdapter.submitList(accounts);
            }
        });
    }

    private void onClickFAB() {
        if (sharedPref.getBoolean("MyWalletPro", false)) {
            Intent intent = new Intent(this, NewAccount.class);
            intent.putExtra("isNewAccount", true);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.reached_acc_limit)
                    .setMessage(R.string.r_a_l_des)
                    .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(AccountManager.this, UpgradeToPro.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        }
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_home)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_recent_trans: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_recent_trans)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, TransactionHistory.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_reports: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_reports)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, Reports.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_accounts: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_accounts)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, AccountManager.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_settings: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_settings)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, Settings.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_get_pro: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_get_pro)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, UpgradeToPro.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.nav_about: {
                if (navigationView.getCheckedItem().getItemId() == R.id.nav_about)
                    drawer.closeDrawer(GravityCompat.START);
                else {
                    Intent intent = new Intent(this, About.class);
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
}