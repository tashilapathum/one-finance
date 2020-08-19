package com.tashila.mywalletfree;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        AndroidThreeTen.init(this);

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
        if (theme.equalsIgnoreCase("dark")) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_new_account);
            View layout = findViewById(R.id.rootLayout);
            //layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
            new Essentials(this).invertDrawable(findViewById(R.id.multiInterests));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_new_account);
        }

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

        if (sharedPref.getBoolean("reqEditing", false)) prepareForEditing();
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
        Button button = (Button) view;
        if (validateAccName() && validateAccBalance()) {
            if (button.getText().equals("Add") || button.getText().equals("එකතු කරන්න"))
                createAccount();
            if (button.getText().equals("Save") || button.getText().equals("සුරකින්න"))
                updateAccount();

            //go back to bank fragment
            finish();
        }
    }

    private void createAccount() {
        ////start of account creation logic////
        /*
        - Account slots are used to track the number of accounts
        - 3 account slots for Free version and 20 slots for Pro version
        - code checks if a slot is taken or not
        - if NOT taken, assigns that number as the account slot, marks the slot as taken in sharedPref and breaks the loop
        - if taken, skips the number
        - "isAccountSlot2Taken" is updated to false when removing the account from Choose Account Dialog
        - multi interests are managed using the same logic in that dialog
        - IMPORTANT: account iterators should always start from 1
         */
        int accLimit = 20; //directly assigned 20 because the limit is checked when clicking the add button
        String accountName = null, accountBalance = null, accountNumber = null, additionalInfo = null, annualInterest = null,
                createdDate = null;
        int i;
        for (i = 1; i <= accLimit; i++) {
            boolean isSlotTaken = sharedPref.getBoolean("isAccountSlot" + i + "Taken", false);
            boolean isSlotDeleted = sharedPref.getBoolean("isAccountSlot" + i + "Deleted", false);
            if (!isSlotTaken && !isSlotDeleted) {
                accountName = "accountName" + i;
                accountBalance = "accountBalance" + i;
                accountNumber = "accountNumber" + i;
                additionalInfo = "additionalInfo" + i;
                annualInterest = "annualInterestStr" + i;
                createdDate = "createdDate" + i;
                sharedPref.edit().putBoolean("isAccountSlot" + i + "Taken", true).apply();
                break;
            }
        }
        ////end of account creation logic////

        //save data of the created account
        saveString(etAccountName, accountName);
        saveString(etCurrentBalance, accountBalance);
        saveString(etAccountNumber, accountNumber);
        saveString(etAdditional, additionalInfo);
        //save interest in a different way because of multi interests
        String annualInterestValue = etAnnualInterest.getText().toString();
        if (!sharedPref.getBoolean("hasMultiInterests" + i, false))
            sharedPref.edit().putString(annualInterest, annualInterestValue).apply(); //assigned in the same way as other data
        //to show on Bank screen
        sharedPref.edit().putString("selectedAccName", etAccountName.getText().toString()).apply();
        sharedPref.edit().putString("selectedAccBalance", etCurrentBalance.getText().toString()).apply();

        //save created date
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        String date = LocalDate.now().format(formatter);
        sharedPref.edit().putString(createdDate, date).apply();

        //add first activity
        String activity;
        if (language.equalsIgnoreCase("සිංහල"))
            activity = "\"" + etAccountName.getText().toString() + "\"" + " ගිණුම සාදන ලදී" + "###" + date;
        else
            activity = "Added the account " + "\"" + etAccountName.getText().toString() + "\"###" + date;

        String activities = sharedPref.getString("activities" + i, null);
        activities = activities + "~~~" + activity;
        sharedPref.edit().putString("activities" + i, activities).apply();

        sharedPref.edit().putBoolean("haveNoAccounts", false).apply(); //when the first account was created
        sharedPref.edit().putBoolean("isTempMultiAvailable", false).apply(); //reset temp data
        new AccountHandler(this).plusAccount();
        Toast.makeText(this, R.string.acc_added, Toast.LENGTH_SHORT).show();
    }

    private void saveString(EditText editText, String stringName) {
        String editTextValue = editText.getText().toString();
        sharedPref.edit().putString(stringName, editTextValue).apply();
    }

    private void onClickMultiInterests() {
        DialogMultiInterests dialogMultiInterests = new DialogMultiInterests();
        dialogMultiInterests.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (sharedPref.getBoolean("addedMultiInterests", false)) {
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
                    sharedPref.edit().putBoolean("addedMultiInterests", true).apply();
                } else {
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

    public void onClickCancel(View view) {
        sharedPref.edit().putBoolean("isTempMultiAvailable", false).apply(); //reset temp data
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
    }

    private void prepareForEditing() {
        //auto fill fields
        int i = sharedPref.getInt("manageAccNo", 1);
        AccountHandler accountHandler = new AccountHandler(this);
        accountHandler.setDetail(etAccountName, "accountName" + i, true);
        accountHandler.setDetail(etCurrentBalance, "accountBalance" + i, true);
        accountHandler.setDetail(etAccountNumber, "accountNumber" + i, true);
        accountHandler.setDetail(etAdditional, "additionalInfo" + i, true);
        accountHandler.setDetail(etAnnualInterest, "annualInterest" + i, true);

        //change texts
        TextView title = findViewById(R.id.title);
        Button add = findViewById(R.id.add);
        TextView bottomNote = findViewById(R.id.bottomNote);
        title.setText(R.string.edit_account); //instead of new account
        add.setText(getString(R.string.save));
        tilAccountName.setHelperText(null);
        tilAnnualInterest.setHelperText(getString(R.string.new_interest_note));
        bottomNote.setText(null);
    }

    private void updateAccount() {
        int i = sharedPref.getInt("manageAccNo", 1);
        AccountHandler accountHandler = new AccountHandler(this);
        accountHandler.saveDetail(etAccountName, "accountName" + i);
        accountHandler.saveDetail(etCurrentBalance, "accountBalance" + i);
        accountHandler.saveDetail(etAccountNumber, "accountNumber" + i);
        accountHandler.saveDetail(etAdditional, "additionalInfo" + i);
        accountHandler.saveDetail(etAnnualInterest, "annualInterest" + i);
        sharedPref.edit().putBoolean("reqEditing", false).apply();
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.edit().putBoolean("reqEditing", false).apply();
        sharedPref.edit().putBoolean("exit", false).apply();
    }





    //--------------------------------------------------------------------------------------------//

    private void createAccountNEW() {

    }
}