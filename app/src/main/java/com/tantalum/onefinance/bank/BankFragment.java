package com.tantalum.onefinance.bank;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.leinardi.android.speeddial.SpeedDialView;
import com.tantalum.onefinance.Amount;
import com.tantalum.onefinance.Constants;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.accounts.Account;
import com.tantalum.onefinance.accounts.AccountDetails;
import com.tantalum.onefinance.accounts.AccountManager;
import com.tantalum.onefinance.accounts.AccountsViewModel;
import com.tantalum.onefinance.accounts.NewAccount;
import com.tantalum.onefinance.pro.UpgradeHandler;

import java.util.List;

public class BankFragment extends Fragment implements DialogInterface.OnDismissListener {
    private Context context;
    private View view;
    private AccountsViewModel accountsViewModel;
    private List<Account> accountList;
    private ChipGroup chipGroup;
    private Account selectedAccount;
    private SpeedDialView fab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bank, container, false);
        context = getActivity();
        accountsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AccountsViewModel.class);
        accountList = accountsViewModel.getAllAccounts();
        chipGroup = view.findViewById(R.id.chipGroup);
        fab = view.findViewById(R.id.fab);

        view.findViewById(R.id.accounts).setOnClickListener(v -> startActivity(new Intent(getActivity(), AccountManager.class)));
        view.findViewById(R.id.accountName).setOnClickListener(v -> showAccDetails());

        if (accountList.isEmpty()) {
            showPlaceholder();
            fab.getMainFab().setOnClickListener(view -> showNoAccount());
        }
        else {
            loadAccountsChips();
            switchAccount();
            setupFab();
        }

        return view;
    }

    private void showPlaceholder() {
        view.findViewById(R.id.placeholder_layout).setVisibility(VISIBLE);
        view.findViewById(R.id.addAccount).setOnClickListener(view -> showNewAccount());
        fab.setOnClickListener(view -> showNoAccount() );
    }

    private void showNoAccount() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.acc_na)
                .setMessage(R.string.acc_na_des)
                .setPositiveButton(R.string.add, (dialog, which) -> showNewAccount())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showNewAccount() {
        Intent intent = new Intent(getActivity(), NewAccount.class);
        intent.putExtra("isNewAccount", true);
        startActivity(intent);
    }

    private void loadAccountsChips() {
        if (!accountList.isEmpty()) {
            for (Account account : accountList) {
                Chip chip = (Chip) getActivity().getLayoutInflater().inflate(R.layout.sample_account_chip, null);
                chip.setText(account.getAccName());
                chip.setTextColor(getResources().getColor(R.color.colorBlack)); //because of visibility issue in dark mode
                chip.setCheckedIconTintResource(R.color.colorAccentLightest);
                chip.setCheckedIconVisible(true);
                chip.setElevation(8f);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    chip.setChecked(isChecked);
                    if (isChecked) {
                        chip.setChipBackgroundColorResource(R.color.colorSelectedChip);
                        chip.setTextColor(getResources().getColor(R.color.colorWhite));
                    } else {
                        chip.setChipBackgroundColorResource(R.color.colorDeselectedChip);
                        chip.setTextColor(getResources().getColor(R.color.colorBlack));
                    }
                    switchAccount();
                });
                if (account.getId() == accountList.get(0).getId()) //check first account of the list
                    chip.setChecked(true);
                chipGroup.addView(chip);
                chipGroup.setSelectionRequired(true);
                chipGroup.setSingleSelection(true);
            }
        }
    }

    private void switchAccount() {
        //find selected acc
        accountList = accountsViewModel.getAllAccounts();
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
        ((TextView) view.findViewById(R.id.accountBalance)).setText(new Amount(context, selectedAccount.getAccBalance()).getAmountString());

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
        ImageView overlay = view.findViewById(R.id.overlay);
        fab.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                return false;
            }

            @Override
            public void onToggleChanged(boolean isOpen) {
                overlay.setVisibility(VISIBLE);
                float alpha;
                if (isOpen) alpha = 0.5f;
                else alpha = 0f;
                overlay.animate().alpha(alpha).setDuration(300).start();
            }
        });
        fab.inflate(R.menu.bank_fab_menu);
        fab.setOnActionSelectedListener(actionItem -> {
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
        });
    }

    private void showAccDetails() {
        if (selectedAccount != null) {
            Intent intent = new Intent(getActivity(), AccountDetails.class);
            intent.putExtra("neededAccountName", selectedAccount.getAccName());
            startActivity(intent);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //refresh selected account
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getText().toString().equals(selectedAccount.getAccName()))
                chip.performClick();
        }
    }

}
