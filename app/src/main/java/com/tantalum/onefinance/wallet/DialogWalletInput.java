package com.tantalum.onefinance.wallet;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.tantalum.onefinance.Amount;
import com.tantalum.onefinance.Constants;
import com.tantalum.onefinance.DatePickerFragment;
import com.tantalum.onefinance.DateTimeHandler;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.accounts.Account;
import com.tantalum.onefinance.accounts.AccountsViewModel;
import com.tantalum.onefinance.categories.CategoriesManager;
import com.tantalum.onefinance.transactions.TransactionItem;
import com.tantalum.onefinance.transactions.TransactionsViewModel;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DialogWalletInput extends BottomSheetDialogFragment {
    private final int transactionType;
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

    public DialogWalletInput(int transactionType) {
        this.transactionType = transactionType;
    }

    public static DialogWalletInput getInstance() {
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_input, null);
        sharedPref = getActivity().getSharedPreferences("myPref", MODE_PRIVATE);
        instance = this;
        accountsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AccountsViewModel.class);
        if (sharedPref.getString("language", "english").equalsIgnoreCase("සිංහල")) sinhala = true;

        dialog = new BottomSheetDialog(getActivity());
        tilAmount = view.findViewById(R.id.amount);
        tilDescription = view.findViewById(R.id.description);
        tilDate = view.findViewById(R.id.date);
        etAmount = tilAmount.getEditText();
        etDescription = tilDescription.getEditText();
        etDate = tilDate.getEditText();
        etDate.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("pickDate", "fromWalletInput");
            DatePickerFragment datePicker = new DatePickerFragment();
            datePicker.setArguments(bundle);
            datePicker.show(getChildFragmentManager(), "date picker dialog");
        });
        setDate(String.valueOf(System.currentTimeMillis()));
        chipGroup = view.findViewById(R.id.chipGroup);
        chipGroup.removeAllViews();
        chipGroup.setSingleSelection(true);

        ImageView imDialogIcon = view.findViewById(R.id.icon);
        Button btnCancel = view.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        Button btnAdd = view.findViewById(R.id.add);
        btnAdd.setOnClickListener(v -> addTransaction());

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
        List<String> categories = new CategoriesManager(requireContext()).getCategoryItems();

        for (String categoryItem : categories) {
            if (!categoryItem.trim().isEmpty()) {
                Chip chip = new Chip(getActivity());
                chip.setText(categoryItem.split("###")[0]);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Integer.parseInt(categoryItem.split("###")[1])));
                chip.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
                chip.setCheckable(true);
                chip.setCheckedIconVisible(true);
                chip.setChipStrokeWidth(0f);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> chip.setChecked(isChecked));
                chipGroup.addView(chip);
            }
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
                chip.setChipStrokeWidth(0f);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    chip.setChecked(isChecked);
                    if (isChecked) {
                        chip.setChipBackgroundColorResource(R.color.colorSelectedChip);
                        chip.setTextColor(getResources().getColor(R.color.colorWhite));
                    } else {
                        chip.setChipBackgroundColorResource(R.color.colorDeselectedChip);
                        chip.setTextColor(getResources().getColor(R.color.colorBlack));
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
        if (!isAmountValid() || !isDescriptionValid()) return;

        String balanceStr = Amount.getStoredBalance(getActivity());
        Amount balance = new Amount(getActivity(), balanceStr);
        String prefix = "";
        String amountInput = etAmount.getText().toString().replace(",", ".");
        Amount amount = new Amount(getActivity(), amountInput);
        String description = etDescription.getText().toString();
        String date = (timeInMillis != null) ? timeInMillis : String.valueOf(System.currentTimeMillis());
        String category;
        Amount newBalance;
        boolean showNegativeWarning = false;

        switch (transactionType) {
            case Constants.INCOME:
                prefix = "+";
                category = getString(R.string.income);
                newBalance = new Amount(getActivity(), balance.getAmountValue() + amount.getAmountValue());
                break;

            case Constants.EXPENSE:
                prefix = "-";
                if (!chipGroup.getCheckedChipIds().isEmpty()) {
                    Chip catChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                    category = catChip.getText().toString()
                            + "###"
                            + catChip.getChipBackgroundColor().getDefaultColor();
                } else {
                    category = getString(R.string.uncategorized);
                }
                newBalance = new Amount(getActivity(), balance.getAmountValue() - amount.getAmountValue());
                if (newBalance.getAmountValue() < 0 && !sharedPref.getBoolean("negativeEnabled", false)) {
                    showNegativeWarning = true;
                }
                break;

            case Constants.TRANSFER:
                Chip accChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                Account selectedAccount = null;
                for (Account account : accountList) {
                    if (accChip.getText().equals(account.getAccName())) {
                        selectedAccount = account;
                        break;
                    }
                }

                description = getString(R.string.deposit_from_wallet_to) + selectedAccount.getAccName();
                category = getString(R.string.acc_transfer);

                double finalBalance = Double.parseDouble(selectedAccount.getAccBalance()) + amount.getAmountValue();
                selectedAccount.setAccBalance(String.valueOf(finalBalance));
                accountsViewModel.update(selectedAccount);

                List<String> accHistory = selectedAccount.getActivities();
                String activity;
                if (sinhala)
                    activity = amount.getAmountString() + "ක් තැන්පත් කරන ලදී" + "###" + date;
                else
                    activity = "Deposited " + amount.getAmountString() + "###" + date;
                accHistory.add(0, activity);
                selectedAccount.setActivities(accHistory);

                newBalance = new Amount(getActivity(), balance.getAmountValue() - amount.getAmountValue());
                break;

            default:
                // handle other cases if any
                return;
        }

        if (showNegativeWarning) {
            Toast.makeText(getActivity(), getResources().getString(R.string.spend_more_than_have), Toast.LENGTH_LONG).show();
            return;
        }

        TransactionItem transactionItem = new TransactionItem(balanceStr, prefix, amountInput, description, date, category);
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        transactionsViewModel.insert(transactionItem);

        Toast.makeText(getActivity(), R.string.added, Toast.LENGTH_SHORT).show();
        WalletFragment.getInstance().setNewBalance(String.valueOf(newBalance.getAmountValue()));
        dialog.dismiss();
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
