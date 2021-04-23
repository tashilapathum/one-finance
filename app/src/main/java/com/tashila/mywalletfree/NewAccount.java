package com.tashila.mywalletfree;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.tashila.mywalletfree.accounts.Account;
import com.tashila.mywalletfree.accounts.AccountsViewModel;

import java.time.LocalDate;
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
    TextInputLayout tilAnnualInterest;
    TextInputLayout tilAccountNo;
    TextInputLayout tilAdditional;
    EditText etAccountName;
    EditText etCurrentBalance;
    EditText etAnnualInterest;
    EditText etAccountNumber;
    EditText etAdditional;
    public static final String TAG = "NewAccount";
    private String language;
    private AccountsViewModel accountsViewModel;
    private boolean isNewAccount;
    private Account account;
    private Account updatingAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        ImageButton multiInterests = findViewById(R.id.multiInterests);
        multiInterests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMultiInterests();
            }
        });

        tilAccountName = findViewById(R.id.accName);
        tilCurrentBalance = findViewById(R.id.currentBalance);
        tilAnnualInterest = findViewById(R.id.annualInterest);
        tilAccountNo = findViewById(R.id.accNumber);
        tilAdditional = findViewById(R.id.additional);
        etAccountName = tilAccountName.getEditText();
        etCurrentBalance = tilCurrentBalance.getEditText();
        etAnnualInterest = tilAnnualInterest.getEditText();
        etAccountNumber = tilAccountNo.getEditText();
        etAdditional = tilAdditional.getEditText();

        accountsViewModel = new AccountsViewModel(getApplication());

        isNewAccount = getIntent().getBooleanExtra("isNewAccount", false);
        updatingAccount = (Account) getIntent().getSerializableExtra("updatingAccount");
        if (updatingAccount != null) prepareForEditing(updatingAccount);
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
        if (accNameInput.isEmpty()) {
            tilAccountName.setError(getString(R.string.required));
            return false;
        } else {
            tilAccountName.setError(null);
            return true;
        }
    }

    private boolean validateAccBalance() {
        String accBalanceInput = tilCurrentBalance.getEditText().getText().toString();
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

            sharedPref.edit().putBoolean("addedMultiInterests", false).apply();

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
        //interest
        boolean isMultiInterest = false;
        String interestRate;
        if (sharedPref.getBoolean("addedMultiInterests", false)) {
            interestRate = sharedPref.getString("multiInterests", null);
            isMultiInterest = true;
            sharedPref.edit().putBoolean("addedMultiInterests", false).apply();
        } else
            interestRate = etAnnualInterest.getText().toString();

        String accountNumber = etAccountNumber.getText().toString();
        String additionalInfo = etAdditional.getText().toString();
        //created account activity
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        String date = LocalDate.now().format(formatter);
        String activity;
        if (language.equalsIgnoreCase("සිංහල"))
            activity = "\"" + accountName + "\"" + " ගිණුම සාදන ලදී" + "###" + date;
        else
            activity = "Created the account " + "\"" + accountName + "\"###" + date;
        List<String> activities = new ArrayList<>();
        activities.add(activity);

        //deselect all other accounts
        List<Account> allAccounts = accountsViewModel.getAllAccounts();
        for (int i = 0; i < allAccounts.size(); i++) {
            allAccounts.get(i).setSelected(false);
            accountsViewModel.update(allAccounts.get(i));
        }
        //save
        Account account = new Account(accountName, accountBalance, balanceHistory, interestRate,
                isMultiInterest, 0, accountNumber, additionalInfo, activities, true);

        accountsViewModel.insert(account);
        this.account = account;
        sharedPref.edit().putBoolean("haveAccounts", true).apply(); //when the first account was created
        Toast.makeText(this, R.string.acc_added, Toast.LENGTH_SHORT).show();
    }

    private void updateAccount(Account account) {
        String accountName = etAccountName.getText().toString();
        //balance
        String accountBalance = etCurrentBalance.getText().toString();
        List<String> balanceHistory = account.getBalanceHistory();
        balanceHistory.add(accountBalance);
        //interest
        boolean isMultiInterest = false;
        String interestRate;
        if (sharedPref.getString("multiInterests", null) != null) {
            interestRate = sharedPref.getString("multiInterests", null);
            isMultiInterest = true;
            sharedPref.edit().putBoolean("addedMultiInterests", false).apply();
        } else
            interestRate = etAnnualInterest.getText().toString();

        String accountNumber = etAccountNumber.getText().toString();
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
        account.setMultiInterest(isMultiInterest);
        account.setInterestRate(interestRate);
        account.setAccNumber(accountNumber);
        account.setMoreDetails(additionalInfo);
        account.setActivities(activities);
        accountsViewModel.update(account);
        sharedPref.edit().putBoolean("reqEditing", false).apply();
        Toast.makeText(this, R.string.updated, Toast.LENGTH_SHORT).show();
    }

    private void onClickMultiInterests() {
        DialogMultiInterests dialogMultiInterests;
        if (isNewAccount)
            dialogMultiInterests = new DialogMultiInterests(account);
        else
            dialogMultiInterests = new DialogMultiInterests(updatingAccount);
        dialogMultiInterests.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (sharedPref.getBoolean("addedMultiInterests", false))
                    setAsMultiple();
                else {
                    etAnnualInterest.setText(null);
                    etAnnualInterest.setClickable(true);
                    etAnnualInterest.setFocusable(true);
                    etAnnualInterest.setFocusableInTouchMode(true);
                    etAnnualInterest.setLongClickable(true);
                    etAnnualInterest.setAlpha(1f);
                    etAnnualInterest.setOnTouchListener(null);
                }
            }
        });
        dialogMultiInterests.show(getSupportFragmentManager(), "multi interests dialog");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setAsMultiple() {
        etAnnualInterest.setText(R.string.multiple);
        etAnnualInterest.setClickable(false);
        etAnnualInterest.setFocusable(false);
        etAnnualInterest.setAlpha(0.5f);
        etAnnualInterest.setLongClickable(false);
        etAnnualInterest.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(NewAccount.this, R.string.clear_muliti_thing, Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    public void onClickCancel(View view) {
        sharedPref.edit().putBoolean("isTempMultiAvailable", false).apply(); //reset temp data
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
    }

    private void prepareForEditing(Account account) {
        //auto fill fields
        etAccountName.setText(account.getAccName());
        etCurrentBalance.setText(account.getAccBalance());
        if (account.isMultiInterest()) setAsMultiple();
        else etAnnualInterest.setText(account.getInterestRate());
        etAccountNumber.setText(account.getAccNumber());
        etAdditional.setText(account.getMoreDetails());

        //change texts
        TextView title = findViewById(R.id.title);
        Button add = findViewById(R.id.add);
        TextView bottomNote = findViewById(R.id.bottomNote);
        title.setText(R.string.edit_account);
        add.setText(getString(R.string.save));
        tilAccountName.setHelperText(null);
        tilAnnualInterest.setHelperText(getString(R.string.new_interest_note));
        bottomNote.setText(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.edit().putBoolean("reqEditing", false).apply();
        sharedPref.edit().putBoolean("exit", false).apply();
    }
}