package com.tantalum.onefinance.bank;

import android.app.Dialog;
import android.content.DialogInterface;
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
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DialogBankInput extends BottomSheetDialogFragment {
    private View view;
    private int transactionType;
    private Account selectedAccount;
    private SharedPreferences sharedPref;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDescription;
    private TextInputLayout tilDate;
    private EditText etAmount;
    private EditText etDescription;
    private EditText etDate;
    private ChipGroup chipGroup;
    private String timeInMillis;
    private static DialogBankInput instance;
    private AccountsViewModel accountsViewModel;
    private List<Account> accountList;
    private BottomSheetDialog dialog;
    private boolean sinhala;
    private String currency;

    public DialogBankInput(int transactionType, Account selectedAccount) {
        this.transactionType = transactionType;
        this.selectedAccount = selectedAccount;
    }

    public static DialogBankInput getInstance() {
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
        currency = sharedPref.getString("currency", "");

        dialog = new BottomSheetDialog(getActivity());
        tilAmount = view.findViewById(R.id.amount);
        tilDescription = view.findViewById(R.id.description);
        tilDate = view.findViewById(R.id.date);
        etAmount = tilAmount.getEditText();
        etDescription = tilDescription.getEditText();
        etDate = tilDate.getEditText();
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("pickDate", "fromBankInput");
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.setArguments(bundle);
                datePicker.show(getChildFragmentManager(), "date picker dialog");
            }
        });
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
            case Constants.DEPOSIT: {
                imDialogIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_deposit));
                btnAdd.setText(R.string.deposit);
                break;
            }
            case Constants.WITHDRAWAL: {
                imDialogIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_withdraw));
                btnAdd.setText(R.string.withdraw);
                tilDescription.setVisibility(View.GONE);
                break;
            }
            case Constants.TRANSFER: {
                imDialogIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_bank_transfer));
                tilDescription.setVisibility(View.GONE);
                view.findViewById(R.id.transferTo).setVisibility(View.VISIBLE);
                btnAdd.setText(R.string.transfer);
                loadAccountsChips();
                break;
            }
            case Constants.PAYMENT: {
                imDialogIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_pay));
                btnAdd.setText(R.string.pay);
                loadCategoryChips();
                break;
            }
        }

        dialog.setContentView(view);

        return dialog;

    }

    private void loadAccountsChips() {
        accountList = accountsViewModel.getAllAccounts();
        for (Account account : accountList) {
            if (!account.getAccName().equals(selectedAccount.getAccName())) { //can transfer only to other accounts
                Chip chip = (Chip) getActivity().getLayoutInflater().inflate(R.layout.sample_account_chip, null);
                chip.setText(account.getAccName());
                chip.setTextColor(getResources().getColor(R.color.colorBlack)); //because of visibility issue in dark mode
                chip.setCheckable(true);
                chip.setCheckedIconVisible(true);
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
                chipGroup.addView(chip);
                chipGroup.setSelectionRequired(true);
            }
        }
    }

    private void addTransaction() {
        if (!isAmountValid() || !isDescriptionValid() || !isAccountSelectionValid()) return;

        Amount amountObj = new Amount(requireContext(), etAmount.getText().toString());
        double amountValue = amountObj.getAmountValue();
        String amountStr = amountObj.getAmountString();
        String plainAmountStr = amountObj.getAmountStringWithoutCurrency();

        String selectedAccBalance = selectedAccount.getAccBalance();
        String description = etDescription.getText().toString();
        String date = (timeInMillis != null) ? timeInMillis : String.valueOf(System.currentTimeMillis());

        double newBalance = 0;
        String activity = null;
        String category = null;

        switch (transactionType) {
            case Constants.DEPOSIT: {
                newBalance = Double.parseDouble(selectedAccBalance) + amountValue;
                activity = description.isEmpty()
                        ? getString(R.string.deposit_prefix) + amountStr +
                        getString(R.string.deposit_mid) + selectedAccount.getAccName() +
                        getString(R.string.deposit_suffix)
                        : description + " (" + amountStr + " " + getString(R.string.deposit) + ")";
                break;
            }

            case Constants.WITHDRAWAL: {
                newBalance = Double.parseDouble(selectedAccBalance) - amountValue;

                double currentWalletBalance = Double.parseDouble(sharedPref.getString("balance", Amount.zero()));
                String newWalletBalance = String.valueOf(currentWalletBalance + amountValue);
                sharedPref.edit().putString("balance", newWalletBalance).apply();

                category = getString(R.string.withdrawal);
                activity = getString(R.string.withdraw_prefix) + amountStr +
                        getString(R.string.withdraw_mid) + selectedAccount.getAccName() +
                        getString(R.string.withdraw_suffix);
                break;
            }

            case Constants.PAYMENT: {
                newBalance = Double.parseDouble(selectedAccBalance) - amountValue;

                if (!chipGroup.getCheckedChipIds().isEmpty()) {
                    Chip catChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                    category = catChip.getText().toString() + "###" +
                            catChip.getChipBackgroundColor().getDefaultColor();
                } else {
                    Chip chip = new Chip(requireContext());
                    category = getString(R.string.uncategorized) + "###" +
                            chip.getChipBackgroundColor().getDefaultColor();
                }

                activity = description + " (" + amountStr + getString(R.string.payment) + ")";
                break;
            }

            case Constants.TRANSFER: {
                newBalance = Double.parseDouble(selectedAccBalance) - amountValue;

                Chip accChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                Account transferringAccount = null;
                for (Account account : accountList) {
                    if (accChip.getText().equals(account.getAccName())) {
                        transferringAccount = account;
                        break;
                    }
                }

                if (transferringAccount != null) {
                    double transferringAccBalance = Double.parseDouble(transferringAccount.getAccBalance()) + amountValue;
                    transferringAccount.setAccBalance(String.valueOf(transferringAccBalance));

                    List<String> accHistory = transferringAccount.getActivities();
                    String transferringActivity = getString(R.string.transfer_from_prefix) + amountStr +
                            getString(R.string.transfer_from_mid) + selectedAccount.getAccName() +
                            getString(R.string.transfer_from_suffix) + "###" + date;
                    accHistory.add(0, transferringActivity);
                    transferringAccount.setActivities(accHistory);

                    List<String> balanceHistory = transferringAccount.getBalanceHistory();
                    balanceHistory.add(String.valueOf(newBalance));
                    transferringAccount.setBalanceHistory(balanceHistory);

                    accountsViewModel.update(transferringAccount);

                    activity = getString(R.string.transfer_to_prefix) + amountStr +
                            getString(R.string.transfer_to_mid) + transferringAccount.getAccName() +
                            getString(R.string.transfer_to_suffix);
                }
                break;
            }
        }

        // Insert transaction if payment
        if (transactionType == Constants.PAYMENT) {
            TransactionItem transactionItem = new TransactionItem(
                    selectedAccBalance, "", plainAmountStr, description, date, category
            );
            new ViewModelProvider(this,
                    ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                    .get(TransactionsViewModel.class)
                    .insert(transactionItem);
        }

        // Save updates to selected account
        selectedAccount.setAccBalance(String.valueOf(newBalance));
        selectedAccount.getActivities().add(0, activity + "###" + date);
        selectedAccount.getBalanceHistory().add(String.valueOf(newBalance));
        accountsViewModel.update(selectedAccount);

        Toast.makeText(requireContext(), R.string.updated, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    public void loadCategoryChips() {
        List<Chip> categories = new CategoriesManager(requireContext()).getCategoryChips();
        for (Chip category : categories)
            chipGroup.addView(category);
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
        if (transactionType != Constants.PAYMENT)
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

    private boolean isAccountSelectionValid() {
        if (transactionType != Constants.TRANSFER)
            return true;
        else if (chipGroup.getCheckedChipIds().isEmpty()) {
            Toast.makeText(getActivity(), R.string.pls_select_account, Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    public void setDate(String timeInMillis) {
        this.timeInMillis = timeInMillis;
        etDate.setText(new DateTimeHandler(timeInMillis).getTimestamp());
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof DialogInterface.OnDismissListener)
            ((DialogInterface.OnDismissListener) parentFragment).onDismiss(dialog);
    }
}
