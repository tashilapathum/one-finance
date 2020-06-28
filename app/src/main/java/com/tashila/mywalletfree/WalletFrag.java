package com.tashila.mywalletfree;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;
import org.threeten.bp.temporal.TemporalField;
import org.threeten.bp.temporal.WeekFields;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class WalletFrag extends Fragment {
    private static final String TAG = "WalletFrag";
    private Context context;
    private SharedPreferences sharedPref;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDescr;
    private EditText etAmount;
    private EditText etDescr;
    private Button btnEarned;
    private Button btnSpent;
    private Button btnToBank;
    private TextView tvBalance;
    private TextView tvCurrency;
    private View v;
    private String currency;
    private int viewId; //to differentiate spent, earned, and toBank
    private String activity;
    private String language;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = container.getContext();
        v = inflater.inflate(R.layout.frag_wallet, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        AndroidThreeTen.init(context);
        currency = sharedPref.getString("currency", "");

        //get data
        tvCurrency = v.findViewById(R.id.currency);
        tvBalance = v.findViewById(R.id.balance);
        tilAmount = v.findViewById(R.id.editAmnt);
        etAmount = tilAmount.getEditText();
        tilDescr = v.findViewById(R.id.editDescr);
        etDescr = tilDescr.getEditText();
        btnEarned = v.findViewById(R.id.btnEarned);
        btnSpent = v.findViewById(R.id.btnSpent);
        btnToBank = v.findViewById(R.id.btnToBank);
        language = sharedPref.getString("language", "english");

        btnEarned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickThreeButtons(view);
                showInstructions(view);
            }
        });
        btnSpent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickThreeButtons(view);
                showInstructions(view);
            }
        });
        btnEarned.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                viewId = v.getId();
                onLongClickThreeButtons(viewId);
                return true;
            }
        });
        btnSpent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                viewId = v.getId();
                onLongClickThreeButtons(viewId);
                return true;
            }
        });
        btnToBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPref.getBoolean("haveNoAccounts", false))
                    Toast.makeText(getActivity(), R.string.acc_first, Toast.LENGTH_LONG).show();
                else {
                    onClickThreeButtons(v);
                    showInstructions(v);
                }
            }
        });
        btnToBank.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                viewId = v.getId();
                onLongClickThreeButtons(viewId);
                return true;
            }
        });

        loadQuickList();
        createReports(0);
        return v;
    }

    public void onStart() {
        super.onStart();
        //load data
        if (sharedPref.contains("currency"))
            tvCurrency.setText(sharedPref.getString("currency", null));
        if (sharedPref.contains("balance"))
            tvBalance.setText(sharedPref.getString("balance", "0.00"));
    }

    private void showInstructions(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btnEarned:
            case R.id.btnSpent: {
                boolean alreadyShown = sharedPref.getBoolean("insLongClickEarnedSpent", false);
                if (!alreadyShown) {
                    new BubbleShowCaseBuilder(getActivity())
                            .title(getString(R.string.predate))
                            .description(getString(R.string.predate_description))
                            .targetView(view)
                            .show();
                    sharedPref.edit().putBoolean("insLongClickEarnedSpent", true).apply();
                }
                break;
            }
            case R.id.btnToBank: {
                boolean alreadyShown = sharedPref.getBoolean("insLongClickToBank", false);
                if (!alreadyShown) {
                    new BubbleShowCaseBuilder(getActivity())
                            .title(getString(R.string.deposit_to_bank))
                            .description(getString(R.string.deposit_to_bank_descr))
                            .targetView(view)
                            .show();
                    sharedPref.edit().putBoolean("insLongClickToBank", true).apply();
                }
                break;
            }
        }
    }

    private boolean validateAmount() {
        if (etAmount.getText().toString().isEmpty()) {
            tilAmount.setError(getString(R.string.required));
            return false;
        } else {
            tilAmount.setError(null);
            return true;
        }
    }

    private boolean validateDescr() {
        if (etDescr.getText().toString().isEmpty()) {
            tilDescr.setError(getString(R.string.required));
            return false;
        } else {
            tilDescr.setError(null);
            return true;
        }
    }

    private void onClickThreeButtons(View view) { //onClick earned, spent or quick list
        if (view.getId() == R.id.btnEarned || view.getId() == R.id.btnSpent) {
            if (validateAmount() & validateDescr()) {
                viewId = view.getId();
                handleData(viewId);
            }

        } else if (view.getId() == R.id.btnToBank) {
            if (!sharedPref.getBoolean("hasNoAccounts", true))
                Toast.makeText(getActivity(), R.string.add_acc_first, Toast.LENGTH_LONG).show();
            else if (validateAmount()) {
                String accountName = sharedPref.getString("selectedAccName", null);
                if (etDescr.getText().toString().isEmpty()) {
                    if (language.equals("සිංහල"))
                        etDescr.setText(accountName + getString(R.string.deposited_to));
                    else
                        etDescr.setText(getString(R.string.deposited_to) + accountName);
                }
                doBankStuff();
                viewId = R.id.btnToBank;
                handleData(viewId);
            }

        } else { //for quick list items
            Button quickButton = (Button) view;
            view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.onclick_effect));
            String quickItem = quickButton.getText().toString();
            String[] amountAndDescr = quickItem.replace(currency, "").split("\n");
            String descr = amountAndDescr[0];
            String amount = amountAndDescr[1];
            etAmount.setText(amount);
            etDescr.setText(descr);
            viewId = R.id.btnSpent; //because they always work as spent
            handleData(viewId);
            etAmount.clearFocus();

        }

    }

    private void onLongClickThreeButtons(int viewId) {
        if (viewId == R.id.btnEarned || viewId == R.id.btnSpent) {
            if (validateAmount() | validateDescr()) {
                sharedPref.edit().putBoolean("longClicked", true).apply();
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getFragmentManager(), "date picker");
            }
        }
        if (viewId == R.id.btnToBank) {
            if (validateAmount()) {
                sharedPref.edit().putBoolean("chooseAccFromWallet", true).apply();
                DialogChooseAcc dialogChooseAcc = new DialogChooseAcc();
                dialogChooseAcc.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        sharedPref.edit().putBoolean("chooseAccFromWallet", false).apply();
                    }
                });
                dialogChooseAcc.show(getFragmentManager(), "choose account");
            }
        }
    }

    void continueLongClickProcess() {
        handleData(viewId);
    }

    private void handleData(int viewId) {
        //balance
        double doubBalance = Double.valueOf(tvBalance.getText().toString());
        double oldBalance = 0;
        if (sharedPref.contains("balance"))
            oldBalance = Double.valueOf(sharedPref.getString("balance", "0"));

        //date
        boolean longClicked = sharedPref.getBoolean("longClicked", false);
        String date;
        if (longClicked) {
            date = sharedPref.getString("preDate", null);
            sharedPref.edit().putBoolean("longClicked", false).apply();
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
            date = LocalDateTime.now().format(formatter);
        }

        //amount
        double doubAmount = Double.valueOf(etAmount.getText().toString());
        DecimalFormat df = new DecimalFormat("0.00");
        String amount = df.format(doubAmount);

        if ((viewId == R.id.btnSpent || viewId == R.id.btnToBank) && doubAmount > oldBalance)
            Toast.makeText(context, getActivity().getResources().getString(R.string.spend_more_than_have), Toast.LENGTH_LONG).show();
        else {
            //description
            String accountName = sharedPref.getString("selectedAccName", null);
            if (etDescr.getText().toString().isEmpty()) {
                if (language.equals("සිංහල"))
                    etDescr.setText(accountName + getString(R.string.deposited_to));
                else
                    etDescr.setText(getString(R.string.deposited_to) + accountName);
            }
            String descr = etDescr.getText().toString();

            //calculate
            String prefix = "";
            if (viewId == R.id.btnEarned) {
                prefix = "+";
                doubBalance = oldBalance + doubAmount;
            }
            if (viewId == R.id.btnSpent) {
                prefix = "-";
                doubBalance = oldBalance - doubAmount;
                createReports(doubAmount);
            }
            if (viewId == R.id.btnToBank) {
                prefix = "-";
                doubBalance = oldBalance - doubAmount;
            }
            String balance = df.format(doubBalance);

            saveData(balance, currency, prefix, amount, descr, date);
            etAmount.setText("");
            etDescr.setText("");
            etAmount.requestFocus();
        }
    }

    private void saveData(final String balance, String currency, final String prefix, final String amount, String descr, String date) {
        //update
        tvBalance.setText(balance);
        tvCurrency.setText(currency);
        String amountStr = prefix + currency + amount; //for the array

        //save
        sharedPref.edit().putString("balance", balance).apply();
        sharedPref.edit().putString("amount", amountStr).apply();
        sharedPref.edit().putString("descr", descr).apply();
        sharedPref.edit().putString("date", date).apply();
        sharedPref.edit().putString("currency", currency).apply();

        //full list
        final String threeItemStr = amountStr + "~" + descr + "~" + date;
        String fullItemList = sharedPref.getString("fullItemList", null); //get old data
        fullItemList = fullItemList + "," + threeItemStr; //add new one
        sharedPref.edit().putString("fullItemList", fullItemList).apply();

        final double[] correctedBalance = new double[1];
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        Snackbar snackbar = Snackbar.make(bottomNav, R.string.updated, Snackbar.LENGTH_LONG);
        snackbar.setAnchorView(bottomNav);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullItemList = sharedPref.getString("fullItemList", null);
                fullItemList = fullItemList.replace(threeItemStr, "");
                sharedPref.edit().putString("fullItemList", fullItemList).apply();
                if (prefix.equals("+")) {
                    correctedBalance[0] = Double.valueOf(balance) - Double.valueOf(amount);
                    tvBalance.setText(String.valueOf(correctedBalance[0]));
                }
                if (prefix.equals("-")) {
                    correctedBalance[0] = Double.valueOf(balance) + Double.valueOf(amount);
                    tvBalance.setText(String.valueOf(correctedBalance[0]));
                }
                sharedPref.edit().putString("balance", String.valueOf(correctedBalance[0])).apply();
                if (viewId == R.id.btnToBank) undoBankStuff(Double.valueOf(amount));
            }
        });
        snackbar.show();
    }

    private void loadQuickList() {
        String fullQuickListStr = sharedPref.getString("fullQuickListStr", null);
        Log.i(TAG, "fullQuickListStr: " + fullQuickListStr + "end");
        if (fullQuickListStr != null && !fullQuickListStr.equals("")) {
            ArrayList<String> fullQuickList = new ArrayList<>(Arrays.asList(fullQuickListStr.split("\n")));

            for (int i = 0; i < fullQuickList.size(); i += 2) {
                LinearLayout layout = v.findViewById(R.id.childLinear);

                //padding
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 16, 0, 0);

                final Button button = new Button(context);
                button.setTag("listItem" + i);
                button.setLayoutParams(params); //padding
                button.setText(fullQuickList.get(i) + "\n" + currency + fullQuickList.get(i + 1));
                String theme = sharedPref.getString("theme", "light");
                if (theme.equalsIgnoreCase("light"))
                    button.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorQuickList, null));
                else
                    button.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorQuickListDark, null));
                button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                button.setAllCaps(false);
                button.setTypeface(Typeface.DEFAULT);
                button.setPadding(4, 4, 4, 4);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickThreeButtons(button);
                    }
                });
                layout.addView(button);
            }
        } else {
            //add "add to quicklist" button
            LinearLayout layout = v.findViewById(R.id.childLinear);

            //padding
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 0);

            Button button = new Button(context);
            button.setTag("sample");
            button.setLayoutParams(params); //padding
            button.setText(getActivity().getResources().getString(R.string.example_quick_item_text));
            String theme = sharedPref.getString("theme", "light");
            if (theme.equalsIgnoreCase("light"))
                button.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorQuickList, null));
            else
                button.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorQuickListDark, null));
            button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            button.setAllCaps(false);
            button.setTypeface(Typeface.DEFAULT);
            button.setPadding(4, 4, 4, 4);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.onclick_effect));
                    Intent intent = new Intent(getActivity(), EditQuickList.class);
                    startActivity(intent);
                }
            });
            layout.addView(button);
        }
    }

    private void createReports(double amount) {
        double monthlyBudget = Double.parseDouble(sharedPref.getString("monthlyBudget", "0"));
        double weeklyBudget = monthlyBudget / 4;
        double dailyBudget = monthlyBudget / YearMonth.now().lengthOfMonth();

        //--------------------------DAY----------------------------//
        double todaySpent;
        double todayBudgetLeft;

        //day count
        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        int lastSavedDay = sharedPref.getInt("lastSavedDay", dayOfWeek);

        if (lastSavedDay == dayOfWeek) {
            double oldTodaySpent = Double.parseDouble(sharedPref.getString("todaySpent", "0"));
            todaySpent = oldTodaySpent + amount;
            sharedPref.edit().putString("todaySpent", String.valueOf(todaySpent)).apply();
            todayBudgetLeft = dailyBudget - todaySpent;
            sharedPref.edit().putString("todayBudgetLeft", String.valueOf(todayBudgetLeft)).apply();
        } else {
            String yesterSpent = sharedPref.getString("todaySpent", "0");
            sharedPref.edit().putString("yesterSpent", yesterSpent).apply();
            sharedPref.edit().putString("todaySpent", "0").apply();
            String yesterBudgetLeft = sharedPref.getString("todayBudgetLeft", String.valueOf(dailyBudget));
            sharedPref.edit().putString("yesterBudgetLeft", yesterBudgetLeft).apply();
            sharedPref.edit().putString("todayBudgetLeft", String.valueOf(dailyBudget)).apply();
        }
        sharedPref.edit().putInt("lastSavedDay", dayOfWeek).apply();


        //--------------------------WEEK----------------------------//
        double weekSpent;
        double weekBudgetLeft;

        //week count
        TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        int weekOfYear = LocalDate.now().get(woy);
        int lastSavedWeek = sharedPref.getInt("lastSavedWeek", weekOfYear);

        if (lastSavedWeek == weekOfYear) {
            double oldWeekSpent = Double.parseDouble(sharedPref.getString("weekSpent", "0"));
            weekSpent = oldWeekSpent + amount;
            sharedPref.edit().putString("weekSpent", String.valueOf(weekSpent)).apply();
            weekBudgetLeft = weeklyBudget - weekSpent;
            sharedPref.edit().putString("weekBudgetLeft", String.valueOf(weekBudgetLeft)).apply();
        } else {
            String lastWeekSpent = sharedPref.getString("weekSpent", "0");
            sharedPref.edit().putString("lastWeekSpent", lastWeekSpent).apply();
            sharedPref.edit().putString("weekSpent", "0").apply();
            String lastWeekBudgetLeft = sharedPref.getString("weekBudgetLeft", String.valueOf(weeklyBudget));
            sharedPref.edit().putString("lastWeekBudgetLeft", lastWeekBudgetLeft).apply();
            sharedPref.edit().putString("weekBudgetLeft", String.valueOf(weeklyBudget)).apply();
        }
        sharedPref.edit().putInt("lastSavedWeek", weekOfYear).apply();


        //--------------------------MONTH----------------------------//
        double monthSpent;
        double monthBudgetLeft;

        //day count
        int monthOfYear = LocalDate.now().getMonthValue();
        int lastSavedMonth = sharedPref.getInt("lastSavedMonth", monthOfYear);

        if (lastSavedMonth == monthOfYear) {
            double oldMonthSpent = Double.parseDouble(sharedPref.getString("monthSpent", "0"));
            monthSpent = oldMonthSpent + amount;
            sharedPref.edit().putString("monthSpent", String.valueOf(monthSpent)).apply();
            monthBudgetLeft = monthlyBudget - monthSpent;
            sharedPref.edit().putString("monthBudgetLeft", String.valueOf(monthBudgetLeft)).apply();
        } else {
            String lastMonthSpent = sharedPref.getString("monthSpent", "0");
            sharedPref.edit().putString("lastMonthSpent", lastMonthSpent).apply();
            sharedPref.edit().putString("monthSpent", "0").apply();
            String lastMonthBudgetLeft = sharedPref.getString("monthBudgetLeft", String.valueOf(monthlyBudget));
            sharedPref.edit().putString("lastMonthBudgetLeft", lastMonthBudgetLeft).apply();
            sharedPref.edit().putString("monthBudgetLeft", String.valueOf(monthlyBudget)).apply();
        }
        sharedPref.edit().putInt("lastSavedMonth", monthOfYear).apply();
    }

    void doBankStuff() {
        //get data
        double amount = Double.parseDouble(etAmount.getText().toString());
        double currentBalance = Double.parseDouble(sharedPref.getString("selectedAccBalance", null));
        String accountName = sharedPref.getString("selectedAccName", null);
        int i = sharedPref.getInt("selectedAccNo", 0);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        String timeStamp = LocalDateTime.now().format(formatter);
        if (language.equalsIgnoreCase("සිංහල"))
            activity = currency + amount + "ක් පසුම්බියෙන් " + accountName + " ගිණුමට බැර කරන ලදී" + "###" + timeStamp;
        else
            activity = "Deposited " + currency + amount + " from wallet to " + accountName + "###" + timeStamp;

        //save data
        String newBalance = String.valueOf(currentBalance + amount);
        sharedPref.edit().putString("accountBalance" + i, newBalance).apply();
        sharedPref.edit().putString("selectedAccBalance", newBalance).apply();

        String activities = sharedPref.getString("activities" + i, null);
        activities = activities + "~~~" + activity;
        sharedPref.edit().putString("activities" + i, activities).apply();
    }

    private void undoBankStuff(double inputAmount) {
        int i = sharedPref.getInt("selectedAccNo", 0);
        String undoActivities = sharedPref.getString("activities" + i, null);
        undoActivities = undoActivities.replace("~~~" + activity, "");
        sharedPref.edit().putString("activities" + i, undoActivities).apply();
        double undoBalance;
        undoBalance = Double.parseDouble(sharedPref.getString("selectedAccBalance", null));
        undoBalance = undoBalance - inputAmount;
        sharedPref.edit().putString("accountBalance" + i, String.valueOf(undoBalance)).apply();
        sharedPref.edit().putString("selectedAccBalance", String.valueOf(undoBalance)).apply();
    }
}

