package com.tantalum.financejournal.bank;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.MaterialSharedAxis;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.tantalum.financejournal.Constants;
import com.tantalum.financejournal.R;
import com.tantalum.financejournal.accounts.Account;
import com.tantalum.financejournal.accounts.AccountManager;
import com.tantalum.financejournal.accounts.AccountsViewModel;
import com.tantalum.financejournal.accounts.NewAccount;
import com.tantalum.financejournal.wallet.DialogWalletInput;

import java.util.List;

public class BankFragmentNEW extends Fragment {
    private View view;
    private AccountsViewModel accountsViewModel;
    private List<Account> accountList;
    private ChipGroup chipGroup;
    private SharedPreferences sharedPref;
    private Account selectedAccount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bank, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        accountsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AccountsViewModel.class);
        accountList = accountsViewModel.getAllAccounts();
        chipGroup = view.findViewById(R.id.chipGroup);
        ((TextView) view.findViewById(R.id.currency)).setText(sharedPref.getString("currency", ""));

        view.findViewById(R.id.accounts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AccountManager.class));
            }
        });

        loadAccountsChips();
        switchAccount();
        setupFab();

        return view;
    }

    private void loadAccountsChips() {
        if (!accountList.isEmpty()) {
            for (Account account : accountList) {
                Chip chip = (Chip) getActivity().getLayoutInflater().inflate(R.layout.sample_account_chip, null);
                chip.setText(account.getAccName());
                chip.setTextColor(getResources().getColor(R.color.colorBlack)); //because of visibility issue in dark mode
                chip.setCheckedIconResource(R.drawable.ic_checked_box);
                chip.setElevation(8f);
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
                        switchAccount();
                    }
                });
                if (account.getId() == accountList.get(0).getId()) //check first account of the list
                    chip.setChecked(true);
                chipGroup.addView(chip);
                chipGroup.setSelectionRequired(true);
            }
        }
    }

    private void switchAccount() {
        //find selected acc
        selectedAccount = null;
        for (Account account : accountList)
            for (int i = 0; i < chipGroup.getChildCount(); i++)
                if (((Chip) chipGroup.getChildAt(i)).getText().toString().equals(account.getAccName()))
                    if (((Chip) chipGroup.getChildAt(i)).isChecked())
                        selectedAccount = account;
        if (selectedAccount == null)
            selectedAccount = accountList.get(0);

        //show acc
        ((TextView) view.findViewById(R.id.accountName)).setText(selectedAccount.getAccName());
        ((TextView) view.findViewById(R.id.accountBalance)).setText(selectedAccount.getAccBalance());

        //show activity history
        List<String> activityHistory = selectedAccount.getActivities();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        AccountActivitiesAdapter adapter = new AccountActivitiesAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.scheduleLayoutAnimation();
        adapter.submitList(activityHistory);

    }

    private void setupFab() {
        SpeedDialView fab = view.findViewById(R.id.fab);
        fab.inflate(R.menu.bank_fab_menu);
        fab.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                int transactionType = 0;
                switch (actionItem.getId()) {
                    case R.id.add_deposit: {
                        transactionType = Constants.DEPOSIT;
                        break;
                    }
                    case R.id.add_withdraw: {
                        transactionType = Constants.WITHDRAWAL;
                        break;
                    }
                    case R.id.add_transfer: {
                        transactionType = Constants.TRANSFER;
                        break;
                    }
                    case R.id.add_payment: {
                        transactionType = Constants.PAYMENT;
                        break;
                    }
                }
                new DialogBankInput(transactionType, selectedAccount).show(getChildFragmentManager(), "bank input dialog");
                fab.close();
                return false;
            }
        });
    }

}
