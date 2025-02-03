package com.tantalum.onefinance.accounts;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.tantalum.onefinance.MainActivity;
import com.tantalum.onefinance.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewAccount extends AppCompatActivity {
    SharedPreferences sharedPref;
    TextInputLayout tilAccountName;
    TextInputLayout tilCurrentBalance;
    TextInputLayout tilAdditional;
    EditText etAccountName;
    EditText etCurrentBalance;
    EditText etAdditional;
    public static final String TAG = "NewAccount";
    private String language;
    private AccountsViewModel accountsViewModel;
    private boolean isNewAccount;
    private Account updatingAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        accountsViewModel = new AccountsViewModel(getApplication());
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        //language
        language = sharedPref.getString("language", "english");
        if (language.equals("සිංහල")) {
            Locale locale = new Locale("si");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        //theme
        String theme = sharedPref.getString("theme", "light");
        if (!theme.equalsIgnoreCase("dark"))
            setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        tilAccountName = findViewById(R.id.accName);
        tilCurrentBalance = findViewById(R.id.currentBalance);
        tilAdditional = findViewById(R.id.additional);
        etAccountName = tilAccountName.getEditText();
        etCurrentBalance = tilCurrentBalance.getEditText();
        etAdditional = tilAdditional.getEditText();

        isNewAccount = getIntent().getBooleanExtra("isNewAccount", false);
        String updatingAccountName = getIntent().getStringExtra("neededAccountName");
        if (updatingAccountName != null) {
            updatingAccount = getSelectedAccount(updatingAccountName);
            prepareForEditing(updatingAccount);
        }
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
    }

    private boolean validateAccName() {
        String accNameInput = tilAccountName.getEditText().getText().toString();
        List<Account> accountList = accountsViewModel.getAllAccounts();
        if (updatingAccount == null) //applicable only when creating new accounts
            for (Account account : accountList)
                if (account.getAccName().equals(accNameInput)) {
                    tilAccountName.setError(getString(R.string.acc_already_exists));
                    return false;
                }

        if (accNameInput.isEmpty()) {
            tilAccountName.setError(getString(R.string.required));
            return false;
        } else {
            tilAccountName.setError(null);
            return true;
        }
    }

    private boolean validateAccBalance() {
        String accBalanceInput = tilCurrentBalance.getEditText().getText().toString().replace(",", ".");
        if (accBalanceInput.isEmpty()) {
            tilCurrentBalance.setError(getString(R.string.required));
            return false;
        } else {
            tilCurrentBalance.setError(null);
            return true;
        }
    }

    public void onClickAdd(View view) {
        if (validateAccName() & validateAccBalance()) {
            if (isNewAccount)
                createAccount();
            else
                updateAccount(updatingAccount);

            //go back to bank fragment
            sharedPref.edit().putBoolean("reqOpenBank", true).apply();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void createAccount() {
        String accountName = etAccountName.getText().toString();
        //balance
        String accountBalance = etCurrentBalance.getText().toString();
        List<String> balanceHistory = new ArrayList<>();
        balanceHistory.add(accountBalance);

        String additionalInfo = etAdditional.getText().toString();

        //created account activity
        String date = String.valueOf(System.currentTimeMillis());
        String activity;
        if (language.equalsIgnoreCase("සිංහල"))
            activity = "\"" + accountName + "\"" + " ගිණුම සාදන ලදී" + "###" + date;
        else
            activity = "Created the account " + "\"" + accountName + "\"###" + date;
        List<String> activities = new ArrayList<>();
        activities.add(activity);

        //save
        Account account = new Account(accountName, accountBalance, balanceHistory, additionalInfo, activities);

        accountsViewModel.insert(account);
        sharedPref.edit().putBoolean("haveAccounts", true).apply(); //when the first account was created
        Toast.makeText(this, R.string.acc_added, Toast.LENGTH_SHORT).show();
    }

    private void updateAccount(Account account) {
        String accountName = etAccountName.getText().toString();
        //balance
        String accountBalance = etCurrentBalance.getText().toString();
        List<String> balanceHistory = account.getBalanceHistory();
        balanceHistory.add(accountBalance);

        String additionalInfo = etAdditional.getText().toString();

        //updated account activity
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        String timeStamp = formatter.format(LocalDateTime.now());
        String activity = getString(R.string.updated_acc_details) + "###" + timeStamp;
        List<String> activities = account.getActivities();
        activities.add(activity);

        //update
        account.setAccName(accountName);
        account.setAccBalance(accountBalance);
        account.setBalanceHistory(balanceHistory);
        account.setMoreDetails(additionalInfo);
        account.setActivities(activities);
        accountsViewModel.update(account);
        sharedPref.edit().putBoolean("reqEditing", false).apply();
        Toast.makeText(this, R.string.updated, Toast.LENGTH_SHORT).show();
    }

    public void onClickCancel(View view) {
        finish();
    }

    private void prepareForEditing(Account account) {
        //auto fill fields
        etAccountName.setText(account.getAccName());
        etCurrentBalance.setText(account.getAccBalance());
        etAdditional.setText(account.getMoreDetails());

        //change texts
        getSupportActionBar().setTitle(R.string.edit_account);
        Button add = findViewById(R.id.add);
        TextView bottomNote = findViewById(R.id.bottomNote);
        add.setText(getString(R.string.save));
        tilAccountName.setHelperText(null);
        bottomNote.setText(null);
    }

    private Account getSelectedAccount(String accName) {
        Account selectedAccount = null;
        List<Account> accountList = accountsViewModel.getAllAccounts();
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getAccName().equals(accName))
                selectedAccount = accountList.get(i);
        }
        return selectedAccount;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.edit().putBoolean("reqEditing", false).apply();
        sharedPref.edit().putBoolean("exit", false).apply();
    }
}