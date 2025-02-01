package com.tantalum.onefinance.accounts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tantalum.onefinance.Constants;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.pro.UpgradeHandler;
import com.tantalum.onefinance.pro.UpgradeToProActivity;

import java.util.List;
import java.util.Locale;

public class AccountManager extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

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
        if (!theme.equalsIgnoreCase("dark")) {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manager);

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
        if (sharedPref.getBoolean("exit", false)) {
            finishAndRemoveTask();
        }

        final AccountsAdapter accountsAdapter = new AccountsAdapter(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(accountsAdapter);

        AccountsViewModel accountsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(AccountsViewModel.class);
        accountsViewModel.getAllAccountsLive().observe(this, new Observer<List<Account>>() {
            @Override
            public void onChanged(List<Account> accounts) {
                recyclerView.scheduleLayoutAnimation();
                accountsAdapter.submitList(accounts);
            }
        });
    }

    private void onClickFAB() {
        Intent intent = new Intent(this, NewAccount.class);
        intent.putExtra("isNewAccount", true);
        if (UpgradeHandler.isProActive(this))
            startActivity(intent);
        else {
            if (recyclerView.getChildCount() < Constants.FREE_BANK_ACCOUNTS_LIMIT)
                startActivity(intent);
            else
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.reached_acc_limit)
                        .setMessage(R.string.r_a_l_des)
                        .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(AccountManager.this, UpgradeToProActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create()
                        .show();
        }
    }

    public void goBack(View view) {
        finish();
    }

}