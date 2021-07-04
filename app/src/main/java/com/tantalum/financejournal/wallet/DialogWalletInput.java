package com.tantalum.financejournal.wallet;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.tantalum.financejournal.Amount;
import com.tantalum.financejournal.Constants;
import com.tantalum.financejournal.DatePickerFragment;
import com.tantalum.financejournal.DateTimeHandler;
import com.tantalum.financejournal.R;
import com.tantalum.financejournal.accounts.Account;
import com.tantalum.financejournal.accounts.AccountsViewModel;
import com.tantalum.financejournal.transactions.TransactionItem;
import com.tantalum.financejournal.transactions.TransactionsViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DialogWalletInput extends BottomSheetDialogFragment {
    private View view;
    private int transactionType;
    private SharedPreferences sharedPref;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDescription;
    private TextInputLayout tilDate;
    private EditText etAmount;
    private EditText etDescription;
    private EditText etDate;
    private ChipGroup chipGroup;
    private String timeInMillis;
    private static DialogWalletInput instance;
    private AccountsViewModel accountsViewModel;
    private List<Account> accountList;
    private BottomSheetDialog dialog;
    private boolean sinhala;
    private String currency;

    public DialogWalletInput(int transactionType) {
        this.transactionType = transactionType;
    }

    public static DialogWalletInput getInstance() {
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_input, null);
        sharedPref = getActivity().getSharedPreferences("myPref", MODE_PRIVATE);
        instance = this;
        accountsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AccountsViewModel.class);
        if (sharedPref.getString("language", "english").equalsIgnoreCase("සිංහල")) sinhala = true;

        dialog = new BottomSheetDialog(getActivity());
        tilAmount = view.findViewById(R.id.amount);
        tilDescription = view.findViewById(R.id.description);
        tilDate = view.findViewById(R.id.date);
        tilDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("pickDate", "fromWalletInput");
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.setArguments(bundle);
                datePicker.show(getChildFragmentManager(), "date picker dialog");
            }
        });
        etAmount = tilAmount.getEditText();
        etDescription = tilDescription.getEditText();
        etDate = tilDate.getEditText();
        setDate(String.valueOf(System.currentTimeMillis()));
        chipGroup = view.findViewById(R.id.chipGroup);
        chipGroup.removeAllViews();
        chipGroup.setSingleSelection(true);

        ImageView imDialogIcon = view.findViewById(R.id.icon);
        Button btnCancel = view.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Button btnAdd = view.findViewById(R.id.add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTransaction();
            }
        });

        switch (transactionType) {
            case Constants.INCOME: {
                imDialogIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_wallet_income));
                chipGroup.setVisibility(View.GONE);
                break;
            }
            case Constants.EXPENSE: {
                imDialogIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_wallet_expense));
                loadCategoryChips();
                break;
            }
            case Constants.TRANSFER: {
                imDialogIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_wallet_transfer));
                tilDescription.setVisibility(View.GONE);
                view.findViewById(R.id.transferTo).setVisibility(View.VISIBLE);
                btnAdd.setText(R.string.transfer);
                loadAccountsChips();
                break;
            }
        }

        dialog.setContentView(view);

        return dialog;

    }

    public void loadCategoryChips() {
        String allCategories = sharedPref.getString(Constants.SP_CATEGORIES, null);
        List<String> categories = new ArrayList<>();
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
            getActivity().getSharedPreferences("myPref", MODE_PRIVATE).edit()
                    .putString(Constants.SP_CATEGORIES, allCategoriesBuilder.toString()).apply();
        } else
            categories.addAll(Arrays.asList(allCategories.split("~~~")));

        for (String categoryItem : categories) {
            Chip chip = new Chip(getActivity());
            chip.setText(categoryItem.split("###")[0]);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Integer.parseInt(categoryItem.split("###")[1])));
            chip.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    chip.setChecked(isChecked);
                }
            });
            chipGroup.addView(chip);
        }
    }

    private void loadAccountsChips() {
        accountList = accountsViewModel.getAllAccounts();
        if (!accountList.isEmpty()) {
            for (Account account : accountList) {
                Chip chip = (Chip) getActivity().getLayoutInflater().inflate(R.layout.sample_account_chip, null);
                chip.setText(account.getAccName());
                chip.setTextColor(getResources().getColor(R.color.colorBlack)); //because of visibility issue in dark mode
                chip.setCheckedIconResource(R.drawable.ic_checked_box);
                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        chip.setChecked(isChecked);
                        if (isChecked) {
                            chip.setChipBackgroundColorResource(R.color.colorSelectedChip);
                            chip.setTextColor(getResources().getColor(R.color.colorWhite));
                        } else {
                            chip.setChipBackgroundColorResource(R.color.colorDeselectedChip);
                            chip.setTextColor(getResources().getColor(R.color.colorBlack));
                        }
                    }
                });
                if (account.getId() == accountList.get(0).getId()) //check first account of the list
                    chip.setChecked(true);
                chipGroup.addView(chip);
                chipGroup.setSelectionRequired(true);
            }
        }
    }

    private void addTransaction() {
        if (isAmountValid() && isDescriptionValid()) {
            String balance = sharedPref.getString(Constants.SP_BALANCE, "0.00");
            String prefix = "";
            String amount = etAmount.getText().toString().replace(",", "."); //comma decimal place countries fix
            String description = etDescription.getText().toString();
            String date;
            if (timeInMillis != null)
                date = timeInMillis;
            else
                date = String.valueOf(System.currentTimeMillis());
            String category = null;
            double newBalance = 0;
            boolean showNegativeWarning = false;

            switch (transactionType) {
                case Constants.INCOME: {
                    prefix = "+";
                    newBalance = Double.parseDouble(balance) + Double.parseDouble(amount);
                    break;
                }
                case Constants.EXPENSE: {
                    prefix = "-";
                    //category
                    if (!chipGroup.getCheckedChipIds().isEmpty()) {
                        Chip catChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                        category = catChip.getText().toString()
                                + "###"
                                + catChip.getChipBackgroundColor().getDefaultColor();
                    }
                    else {
                        Chip chip = new Chip(getActivity());
                        category = getString(R.string.uncategorized) + "###" + chip.getChipBackgroundColor().getDefaultColor();
                    }
                    //balance
                    newBalance = Double.parseDouble(balance) - Double.parseDouble(amount);
                    if (newBalance < 0)
                        if (!sharedPref.getBoolean("negativeEnabled", false))
                            showNegativeWarning = true;
                    break;
                }
                case Constants.TRANSFER: {
                    Chip accChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                    //find selected account
                    Account selectedAccount = null;
                    for (Account account : accountList)
                        if (accChip.getText().equals(account.getAccName()))
                            selectedAccount = account;

                    description = getString(R.string.deposit_from_wallet_to) + selectedAccount.getAccName();

                    //update balance
                    double finalBalance = Double.parseDouble(selectedAccount.getAccBalance()) + Double.parseDouble(amount);
                    selectedAccount.setAccBalance(String.valueOf(finalBalance));
                    accountsViewModel.update(selectedAccount);

                    //update transaction
                    List<String> accHistory = selectedAccount.getActivities();
                    String activity;
                    if (sinhala)
                        activity = new Amount(getActivity(), amount).getAmountString() + "ක් තැන්පත් කරන ලදී" + "###" + date;
                    else
                        activity = "Deposited " + new Amount(getActivity(), amount).getAmountString() + "###" + date;
                    accHistory.add(0, activity);
                    selectedAccount.setActivities(accHistory);

                    newBalance = Double.parseDouble(balance) - Double.parseDouble(amount);

                    break;
                }
            }

            if (showNegativeWarning)
                Toast.makeText(getActivity(), getResources().getString(R.string.spend_more_than_have), Toast.LENGTH_LONG).show();
            else {
                //add transaction
                TransactionItem transactionItem = new TransactionItem(balance, prefix, amount, description, date, category);
                TransactionsViewModel transactionsViewModel = new ViewModelProvider(this, ViewModelProvider
                        .AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
                transactionsViewModel.insert(transactionItem);

                //show
                Toast.makeText(getActivity(), R.string.added, Toast.LENGTH_SHORT).show();
                WalletFragmentNEW.getInstance().setNewBalance(String.valueOf(newBalance));
                dialog.dismiss();
            }

        }
    }

    private boolean isAmountValid() {
        if (etAmount.getText().toString().isEmpty()) {
            tilAmount.setError(getString(R.string.required));
            return false;
        } else {
            tilAmount.setError(null);
            return true;
        }
    }

    private boolean isDescriptionValid() {
        if (transactionType == Constants.TRANSFER)
            return true;
        else {
            String text = etDescription.getText().toString();
            if (text.isEmpty()) {
                tilDescription.setError(getString(R.string.required));
                return false;
            } else {
                if (text.contains("~~~") || text.contains(",,,")) {
                    tilDescription.setError("~~~ and ,,, are not allowed");
                    return false;
                } else {
                    tilDescription.setError(null);
                    return true;
                }
            }
        }
    }

    public void setDate(String timeInMillis) {
        this.timeInMillis = timeInMillis;
        etDate.setText(new DateTimeHandler(timeInMillis).getTimestamp());
    }

}
