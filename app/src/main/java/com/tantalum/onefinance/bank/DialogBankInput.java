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
                chipGroup.addView(chip);
                chipGroup.setSelectionRequired(true);
            }
        }
    }

    private void addTransaction() {
        if (isAmountValid() && isDescriptionValid() && isAccountSelectionValid()) {
            String selectedAccBalance = selectedAccount.getAccBalance();
            String prefix = "";
            String amount = new Amount(
                    getActivity(), etAmount.getText().toString().replace(",", ".")
            ).getAmountStringWithoutCurrency(); //comma decimal place countries fix
            String description = etDescription.getText().toString();
            String activity = null;
            String date;
            if (timeInMillis != null)
                date = timeInMillis;
            else
                date = String.valueOf(System.currentTimeMillis());
            String category = null;
            double newBalance = 0;

            switch (transactionType) {
                case Constants.DEPOSIT: {
                    newBalance = Double.parseDouble(selectedAccBalance) + Double.parseDouble(amount);

                    if (description.isEmpty())
                    activity = getString(R.string.deposit_prefix)
                            + currency + amount
                            + getString(R.string.deposit_mid)
                            + selectedAccount.getAccName()
                            + getString(R.string.deposit_suffix);
                    else
                        activity = description + " (" + currency + amount + " " + getString(R.string.deposit) + ") " ;
                    break;
                }
                case Constants.WITHDRAWAL: {
                    newBalance = Double.parseDouble(selectedAccBalance) - Double.parseDouble(amount);

                    //update wallet balance
                    double currentBalance = Double.parseDouble(sharedPref.getString("balance", "0.00"));
                    String newWalletBalance = String.valueOf(currentBalance + Double.parseDouble(amount));
                    sharedPref.edit().putString("balance", newWalletBalance).apply();

                    category = getString(R.string.withdrawal);

                    activity = getString(R.string.withdraw_prefix)
                            + currency + amount
                            + getString(R.string.withdraw_mid)
                            + selectedAccount.getAccName()
                            + getString(R.string.withdraw_suffix);
                    break;
                }
                case Constants.PAYMENT: {
                    newBalance = Double.parseDouble(selectedAccBalance) - Double.parseDouble(amount);

                    //category
                    if (!chipGroup.getCheckedChipIds().isEmpty()) {
                        Chip catChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                        category = catChip.getText().toString()
                                + "###"
                                + catChip.getChipBackgroundColor().getDefaultColor();
                    } else {
                        Chip chip = new Chip(getActivity());
                        category = getString(R.string.uncategorized) + "###" + chip.getChipBackgroundColor().getDefaultColor();
                    }

                    activity = description + " (" + currency + amount + getString(R.string.payment) + ")";
                    break;
                }
                case Constants.TRANSFER: {
                    newBalance = Double.parseDouble(selectedAccBalance) - Double.parseDouble(amount);

                    //find transferring account
                    Chip accChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                    Account transferringAccount = null;
                    for (Account account : accountList)
                        if (accChip.getText().equals(account.getAccName()))
                            transferringAccount = account;

                    //update balance
                    double transferringAccBalance = Double.parseDouble(transferringAccount.getAccBalance()) + Double.parseDouble(amount);
                    transferringAccount.setAccBalance(String.valueOf(transferringAccBalance));

                    //add activity
                    List<String> accHistory = transferringAccount.getActivities();
                    String transferringAccActivity;
                    transferringAccActivity =
                            getString(R.string.transfer_from_prefix)
                                    + currency + amount
                                    + getString(R.string.transfer_from_mid)
                                    + selectedAccount.getAccName()
                                    + getString(R.string.transfer_from_suffix)
                                    + "###" + date;
                    accHistory.add(0, transferringAccActivity);
                    transferringAccount.setActivities(accHistory);

                    //add to balance history
                    List<String> balanceHistory = transferringAccount.getBalanceHistory();
                    balanceHistory.add(String.valueOf(newBalance));
                    transferringAccount.setBalanceHistory(balanceHistory);

                    accountsViewModel.update(transferringAccount);

                    activity = getString(R.string.transfer_to_prefix)
                            + currency + amount
                            + getString(R.string.transfer_to_mid)
                            + transferringAccount.getAccName()
                            + getString(R.string.transfer_to_suffix);
                    break;
                }
            }

            //add transaction
            if (transactionType == Constants.PAYMENT) {
                TransactionItem transactionItem = new TransactionItem(selectedAccBalance, prefix, amount, description, date, category);
                TransactionsViewModel transactionsViewModel = new ViewModelProvider(this, ViewModelProvider
                        .AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
                transactionsViewModel.insert(transactionItem);
            }

            //save
            //balance
            selectedAccount.setAccBalance(String.valueOf(newBalance));
            //activities
            List<String> selectedAccActivities = selectedAccount.getActivities();
            selectedAccActivities.add(0, activity + "###" + date);
            selectedAccount.setActivities(selectedAccActivities);
            //balance history
            List<String> balanceHistory = selectedAccount.getBalanceHistory();
            balanceHistory.add(String.valueOf(newBalance));
            selectedAccount.setBalanceHistory(balanceHistory);
            //update
            accountsViewModel.update(selectedAccount);

            //show
            Toast.makeText(getActivity(), R.string.updated, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
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
            chip.setCheckedIconVisible(true);
            chip.setChipStrokeWidth(0f);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    chip.setChecked(isChecked);
                }
            });
            chipGroup.addView(chip);
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
