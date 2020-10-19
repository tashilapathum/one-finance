package com.tashila.mywalletfree;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

public class WalletFragment extends Fragment {
    private static final String TAG = "WalletFragment";
    private Context context;
    private SharedPreferences sharedPref;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDescr;
    private EditText etAmount;
    private EditText etDescr;
    private TextView txtBalance;
    private TextView tvBalance;
    private TextView tvCurrency;
    private View v;
    private String currency;
    private int viewId; //to differentiate spent, earned, and toBank
    private String activity;
    private String language;
    private static WalletFragment instance;
    private TransactionsViewModel transactionsViewModel;
    private String theme;
    private double amount;
    private DecimalFormat df;
    private AccountsViewModel accountsViewModel;
    private Account account;
    private boolean longClicked;
    private QuickListViewModel quickListViewModel;
    private String date;
    private boolean isBankRelated;
    private final int QUICK_LIST = R.id.quickList;
    private final int TODAY_REPORT = R.id.todayReport;
    private final int THIS_WEEK_REPORT = R.id.thisWeekReport;
    private final int THIS_MONTH_REPORT = R.id.thisMonthReport;
    private MaterialButton btnEarned;
    private MaterialButton btnSpent;
    private MaterialButton btnToBank;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = container.getContext();
        v = inflater.inflate(R.layout.frag_wallet, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
        theme = sharedPref.getString("theme", "light");
        instance = this;
        transactionsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        accountsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AccountsViewModel.class);
        quickListViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(QuickListViewModel.class);

        //get data
        txtBalance = v.findViewById(R.id.txtBalance);
        tvCurrency = v.findViewById(R.id.currency);
        tvBalance = v.findViewById(R.id.balance);
        tilAmount = v.findViewById(R.id.editAmnt);
        etAmount = tilAmount.getEditText();
        tilDescr = v.findViewById(R.id.editDescr);
        etDescr = tilDescr.getEditText();
        btnEarned = v.findViewById(R.id.btnEarned);
        btnSpent = v.findViewById(R.id.btnSpent);
        btnToBank = v.findViewById(R.id.btnTransfer);
        Button btnUpdate = v.findViewById(R.id.btnUpdate);
        ImageButton imEditQuickList = v.findViewById(R.id.editQuickList);
        language = sharedPref.getString("language", "english");
        setShadows(txtBalance, tvBalance, tvCurrency);
        df = new DecimalFormat("#.00");

        setMainButtonActions();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUpdateBalance dialogUpdateBalance = new DialogUpdateBalance(getActivity());
                dialogUpdateBalance.show(getActivity().getSupportFragmentManager(), "update balance dialog");
            }
        });
        imEditQuickList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditQuickList.class);
                startActivity(intent);
            }
        });

        loadContent();
        setupButtons();
        setupInputMode();
        return v;
    }

    private void setMainButtonActions() {
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
    }

    private void loadContent() {
        boolean hasCustomized = sharedPref.getBoolean("walletContentCustomized", false);
        if (hasCustomized) {
            String contentStr = sharedPref.getString("walletContent", null);
            String[] contentStrings = contentStr.split("~");
            int[] contentIds = new int[contentStrings.length];
            for (int i = 0; i < contentStrings.length; i++)
                contentIds[i] = Integer.parseInt(contentStrings[i]);
            for (int itemId : contentIds) {
                switch (itemId) {
                    case QUICK_LIST: {
                        loadQuickList();
                        break;
                    }
                    case TODAY_REPORT: {
                        loadContentItem(TODAY_REPORT);
                        break;
                    }
                    case THIS_WEEK_REPORT: {
                        loadContentItem(THIS_WEEK_REPORT);
                        break;
                    }
                    case THIS_MONTH_REPORT: {
                        loadContentItem(THIS_MONTH_REPORT);
                        break;
                    }
                }
            }
        } else
            loadQuickList(); //default
    }

    private void loadContentItem(int itemId) {
        Fragment fragment = null;
        String fragmentTag = null;
        switch (itemId) {
            case TODAY_REPORT: {
                fragment = new DailyReportsFragment();
                fragmentTag = "TODAY_REPORT";
                break;
            }
            case THIS_WEEK_REPORT: {
                fragment = new WeeklyReportsFragment();
                fragmentTag = "THIS_WEEK_REPORT";
                break;
            }
            case THIS_MONTH_REPORT: {
                fragment = new MonthlyReportsFragment();
                fragmentTag = "THIS_MONTH_REPORT";
                break;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean("fromWallet", true);
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.content_container, fragment, fragmentTag)
                .commit();
    }

    public void onStart() {
        super.onStart();
        //load data
        String balance = sharedPref.getString("balance", "0.00");
        if (sharedPref.contains("currency"))
            tvCurrency.setText(sharedPref.getString("currency", null));
        if (sharedPref.contains("balance"))
            tvBalance.setText(balance);

        if (!sharedPref.getBoolean("negativeEnabled", false))
            showNegativeWarning();

        showEditQLButton();
    }

    private void showNegativeWarning() {
        String balance = sharedPref.getString("balance", "0.00");
        ImageButton imWarning = v.findViewById(R.id.warning);
        if (balance.contains("-")) {
            tvBalance.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
            tvCurrency.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
            imWarning.setVisibility(View.VISIBLE);
            imWarning.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.neg_balance)
                            .setMessage(R.string.update_balance_des)
                            .setPositiveButton(R.string.ok, null)
                            .show();
                }
            });
        } else {
            imWarning.setVisibility(View.GONE);
            tvBalance.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            tvCurrency.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedPref.getBoolean("quickItemsChanged", false)
                || sharedPref.getBoolean("walletContentChanged", false)) {
            reloadFragment();
            sharedPref.edit().putBoolean("quickItemsChanged", false).apply();
            sharedPref.edit().putBoolean("walletContentChanged", false).apply();
        }
        setupAutocomplete();
    }

    public static WalletFragment getInstance() {
        return instance;
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
            case R.id.btnTransfer: {
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
        String text = etDescr.getText().toString();
        if (text.isEmpty()) {
            tilDescr.setError(getString(R.string.required));
            return false;
        } else {
            if (text.contains("~~~") || text.contains(",,,")) {
                tilDescr.setError("~~~ and ,,, are not allowed");
                return false;
            } else {
                tilDescr.setError(null);
                return true;
            }
        }
    }

    private void onClickThreeButtons(View view) { //onClick earned, spent or quick list
        sharedPref.edit().putBoolean("longClicked", false).apply();
        if (view.getId() == R.id.btnEarned || view.getId() == R.id.btnSpent) {
            if (validateAmount() & validateDescr()) {
                amount = Double.parseDouble(etAmount.getText().toString());
                viewId = view.getId();
                handleData(viewId);
            }

        } else if (view.getId() == R.id.btnTransfer) {
            if (sharedPref.getBoolean("haveAccounts", false)) {
                if (validateAmount()) {
                    amount = Double.parseDouble(etAmount.getText().toString());
                    String accountName = getSelectedAccount().getAccName();
                    if (etDescr.getText().toString().isEmpty()) {
                        if (language.equals("සිංහල"))
                            etDescr.setText(accountName + getString(R.string.deposited_to));
                        else
                            etDescr.setText(getString(R.string.deposited_to) + accountName);
                    }
                    viewId = R.id.btnTransfer;
                    handleData(viewId);
                }
            } else
                Toast.makeText(getActivity(), R.string.add_acc_first, Toast.LENGTH_LONG).show();

        } else { //for quick list items
            Button quickButton = (Button) view;
            String quickItem = quickButton.getText().toString();
            String[] amountAndDescr = quickItem.replace(currency, "").split("\n");
            String descr = amountAndDescr[0];
            String amount = amountAndDescr[1];
            setTexts(amount, descr);
            viewId = R.id.btnSpent; //because they always work as spent
            handleData(viewId);
            etAmount.clearFocus();
        }

    }

    private void onLongClickThreeButtons(int viewId) {
        sharedPref.edit().putBoolean("longClicked", true).apply();
        if (viewId == R.id.btnEarned || viewId == R.id.btnSpent) {
            if (validateAmount() & validateDescr()) {
                Bundle bundle = new Bundle();
                bundle.putString("pickDate", "fromWalletFragment");
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.setArguments(bundle);
                datePicker.show(getFragmentManager(), "date picker");
            }
        }
        if (viewId == R.id.btnTransfer) {
            if (sharedPref.getBoolean("haveAccounts", false)) {
                if (validateAmount()) {
                    amount = Double.parseDouble(etAmount.getText().toString());
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
            } else
                Toast.makeText(getActivity(), R.string.add_acc_first, Toast.LENGTH_SHORT).show();
        }
    }

    void continueLongClickProcess() { //after returning from dialog
        handleData(viewId);
        sharedPref.edit().putInt("walletViewID", viewId).apply();
    }

    private void handleData(int viewId) {
        //balance
        double doubBalance = Double.valueOf(tvBalance.getText().toString());
        double oldBalance = 0;
        if (sharedPref.contains("balance"))
            oldBalance = Double.valueOf(sharedPref.getString("balance", "0"));

        //date
        longClicked = sharedPref.getBoolean("longClicked", false);
        if (longClicked && (viewId == R.id.btnSpent || viewId == R.id.btnEarned)) {
            date = sharedPref.getString("preDate", null);
            sharedPref.edit().putBoolean("longClicked", false).apply();
        } else
            date = String.valueOf(System.currentTimeMillis());

        //amount
        double doubAmount = Double.valueOf(etAmount.getText().toString());
        String amount = df.format(doubAmount);

        boolean negativeEnabled = sharedPref.getBoolean("negativeEnabled", false);
        if ((viewId == R.id.btnSpent || viewId == R.id.btnTransfer) && doubAmount > oldBalance && !negativeEnabled)
            Toast.makeText(context, getActivity().getResources().getString(R.string.spend_more_than_have), Toast.LENGTH_LONG).show();
        else {
            //description
            account = getSelectedAccount();
            if (account != null) {
                String accountName = account.getAccName();
                if (etDescr.getText().toString().isEmpty()) {
                    if (language.equals("සිංහල"))
                        etDescr.setText(accountName + getString(R.string.deposited_to));
                    else
                        etDescr.setText(getString(R.string.deposited_to) + accountName);
                }
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
            }
            if (viewId == R.id.btnTransfer) {
                prefix = "-";
                doubBalance = oldBalance - doubAmount;
                isBankRelated = true;
            }
            String balance = df.format(doubBalance);


            saveToDatabase(balance, prefix, currency, amount, descr, date);
            etAmount.setText("");
            etDescr.setText("");
            etAmount.requestFocus();
        }
    }

    private void saveToDatabase(final String balance, final String prefix, String currency, final String amount,
                                final String description, final String userDate) {
        //save to show
        sharedPref.edit().putString("balance", balance).apply();
        sharedPref.edit().putString("currency", currency).apply();

        //to show the changed balance to user regardless of saving to database
        tvBalance.setText(balance);
        tvCurrency.setText(currency);

        boolean isUndoEnabled = sharedPref.getBoolean("undoActionEnabled", true);
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        Snackbar snackbar = Snackbar.make(bottomNav, R.string.updated, Snackbar.LENGTH_SHORT);
        snackbar.setAnchorView(bottomNav);

        if (isUndoEnabled) {
            snackbar.setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double numBalance = Double.parseDouble(balance);
                    double numAmount = Double.parseDouble(amount);

                    if (prefix.equals("+"))
                        numBalance = numBalance - numAmount;
                    if (prefix.equals("-"))
                        numBalance = numBalance + numAmount;
                    DecimalFormat df = new DecimalFormat("#.00");
                    String strBalance = df.format(numBalance);
                    tvBalance.setText(strBalance);
                    sharedPref.edit().putString("balance", strBalance).apply();
                }
            });
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        TransactionItem transactionItem =
                                new TransactionItem(balance, prefix, amount, description, userDate, null, isBankRelated);
                        transactionsViewModel.insert(transactionItem);
                        if (viewId == R.id.btnTransfer && !longClicked)
                            doBankStuff(null);
                        sharedPref.edit().putBoolean("longClicked", false).apply();
                        //reloadFragment();
                    }
                }
            });
        }
        else {
            TransactionItem transactionItem =
                    new TransactionItem(balance, prefix, amount, description, userDate, null, isBankRelated);
            transactionsViewModel.insert(transactionItem);
            if (viewId == R.id.btnTransfer && !longClicked)
                doBankStuff(null);
            sharedPref.edit().putBoolean("longClicked", false).apply();
            //reloadFragment();
        }
        snackbar.show();
    }

    private void loadQuickList() {
        v.findViewById(R.id.quickList_title).setVisibility(View.VISIBLE);
        List<QuickItem> fullQuickList = quickListViewModel.getQuickItemsList();
        if (fullQuickList.size() != 0) {
            for (int i = 0; i < fullQuickList.size(); i++) {
                LinearLayout layout = v.findViewById(R.id.content_container);

                //padding
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 2);

                final MaterialButton button = new MaterialButton(context);
                button.setTag("listItem" + i);
                button.setLayoutParams(params); //padding
                button.setText(fullQuickList.get(i).getItemName() + "\n" + currency + fullQuickList.get(i).getItemPrice());
                String theme = sharedPref.getString("theme", "light");
                if (theme.equalsIgnoreCase("light"))
                    button.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorQuickList, null));
                else {
                    button.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorQuickListDark, null));
                    button.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                }
                button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                button.setAllCaps(false);
                button.setTypeface(Typeface.DEFAULT);
                button.setPadding(4, 4, 4, 4);
                button.setCornerRadius(16);
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
            LinearLayout layout = v.findViewById(R.id.content_container);

            //padding
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 0);

            MaterialButton button = new MaterialButton(context);
            button.setTag("sample");
            button.setLayoutParams(params); //padding
            button.setText(getActivity().getResources().getString(R.string.example_quick_item_text));
            String theme = sharedPref.getString("theme", "light");
            if (theme.equalsIgnoreCase("light"))
                button.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorQuickList, null));
            else {
                button.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorQuickListDark, null));
                button.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
            }
            button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            button.setAllCaps(false);
            button.setTypeface(Typeface.DEFAULT);
            button.setPadding(4, 4, 4, 4);
            button.setCornerRadius(16);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), EditQuickList.class);
                    startActivity(intent);
                }
            });
            layout.addView(button);
        }

        //empty space below
        LinearLayout layout = v.findViewById(R.id.content_container);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 0);

        MaterialButton button = new MaterialButton(context);
        button.setPadding(4, 4, 4, 4);
        button.setVisibility(View.INVISIBLE);
        layout.addView(button);
    }

    void doBankStuff(Account account) {
        if (account == null)
            account = getSelectedAccount();

        //update activity
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        String timeStamp = LocalDateTime.now().format(formatter);
        if (language.equalsIgnoreCase("සිංහල"))
            activity = currency + df.format(amount) + "ක් පසුම්බියෙන් බැර කරන ලදී" + "###" + timeStamp;
        else
            activity = "Deposited " + currency + df.format(amount) + " from Wallet" + "###" + timeStamp;
        account.getActivities().add(activity);

        //update balance
        String newBalance = String.valueOf(Double.parseDouble(account.getAccBalance()) + amount);
        account.setAccBalance(newBalance);

        accountsViewModel.update(account);
        sharedPref.edit().putBoolean("transferred", true).apply();
    }

    private Account getSelectedAccount() {
        Account account = null;
        List<Account> accountList = accountsViewModel.getAllAccounts();
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).isSelected())
                account = accountList.get(i);
        }
        return account;
    }

    public void updateBalance(String newBalance) {
        sharedPref.edit().putString("balance", newBalance).apply();
        Toast.makeText(getActivity(), R.string.updated, Toast.LENGTH_SHORT).show();
        reloadFragment();
    }

    private void reloadFragment() {
        Fragment walletFrag = getActivity().getSupportFragmentManager().findFragmentByTag("WalletFragment");
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(walletFrag).attach(walletFrag).commit();
    }

    private void setupAutocomplete() {
        boolean isEnabled = sharedPref.getBoolean("showSuggestions", false);
        if (isEnabled) {
            MaterialAutoCompleteTextView aetDescription = v.findViewById(R.id.aetDescription);
            List<QuickItem> quickItems = quickListViewModel.getQuickItemsList();
            List<String> templates = new ArrayList<>();
            for (int i = 0; i < quickItems.size(); i++)
                templates.add(quickItems.get(i).getItemName());
            CustomAutocompleteArrayAdapter arrayAdapter = new CustomAutocompleteArrayAdapter(
                    getActivity(), android.R.layout.simple_list_item_1, templates);
            aetDescription.setAdapter(arrayAdapter);
        }
    }

    private void setShadows(TextView... views) {
        for (TextView view : views) {
            if (theme.equalsIgnoreCase("dark"))
                view.setShadowLayer(3, 1, 1, R.color.colorShadowDark);
            else
                view.setShadowLayer(3, 1, 1, R.color.colorShadow);
        }
    }

    private void setupInputMode() {
        String inputMode = sharedPref.getString("inputMode", "classic"); //no need to do anything if classic
        if (inputMode.equals("floating")) {
            v.findViewById(R.id.wallet_input_layout).setVisibility(View.GONE);
            ExtendedFloatingActionButton fab = v.findViewById(R.id.fabInput);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogWalletInput dialogWalletInput = new DialogWalletInput();
                    dialogWalletInput.show(getActivity().getSupportFragmentManager(), "wallet input dialog");
                }
            });
        }
    }

    private void showEditQLButton() {
        boolean qlButtonEnabled = sharedPref.getBoolean("qlShortcutEnabled", false);
        if (qlButtonEnabled)
            v.findViewById(R.id.editQuickList).setVisibility(View.VISIBLE);
    }

    private void setupButtons() {
        String buttonType = sharedPref.getString("buttonType", "labelOnly");
        if (buttonType.equals("labelOnly")) {
            btnEarned.setIcon(null);
            btnSpent.setIcon(null);
            btnToBank.setIcon(null);
        }
        else if (buttonType.equals("iconOnly")) {
            btnEarned.setText(null);
            btnEarned.setIconPadding(0);
            btnEarned.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            btnSpent.setText(null);
            btnSpent.setIconPadding(0);
            btnSpent.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            btnToBank.setText(null);
            btnToBank.setIconPadding(0);
            btnToBank.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        }
    }

    public void setTexts(String amount, String description) {
        etAmount.setText(amount);
        etDescr.setText(description);
    }

    public void clickFromBottomSheet(MaterialButton button) {

    }

   /* private void setupAutocomplete() {
        boolean isEnabled = sharedPref.getBoolean("autocompleteEnabled", false);
        if (!isEnabled) {
            MaterialAutoCompleteTextView aetDescription = v.findViewById(R.id.aetDescription);
            MaterialAutoCompleteTextView aetAmount = v.findViewById(R.id.aetAmount);
            final List<QuickItem> quickItems = quickListViewModel.getQuickItemsList();
            List<String> templates = new ArrayList<>();
            for (int i = 0; i < quickItems.size(); i++)
                templates.add(quickItems.get(i).getItemName());
            CustomAutocompleteArrayAdapter arrayAdapter = new CustomAutocompleteArrayAdapter(
                    getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, templates) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                    text1.setText(quickItems.get(position).getItemName());
                    text2.setText(currency + quickItems.get(position).getItemPrice());
                    return view;
                }
            };
            aetDescription.setAdapter(arrayAdapter);
            aetAmount.setAdapter(arrayAdapter);
        }
    }*/
}

