package com.tantalum.financejournal.wallet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.MaterialSharedAxis;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;
import com.tantalum.financejournal.Amount;
import com.tantalum.financejournal.Constants;
import com.tantalum.financejournal.R;
import com.tantalum.financejournal.accounts.NewAccount;
import com.tantalum.financejournal.quicklist.EditQuickList;
import com.tantalum.financejournal.quicklist.QuickItem;
import com.tantalum.financejournal.quicklist.QuickListViewModel;
import com.tantalum.financejournal.reports.DailyReportsFragment;
import com.tantalum.financejournal.reports.MonthlyReportsFragment;
import com.tantalum.financejournal.reports.WeeklyReportsFragment;
import com.tantalum.financejournal.transactions.TransactionItem;
import com.tantalum.financejournal.transactions.TransactionsFragment;
import com.tantalum.financejournal.transactions.TransactionsViewModel;

import java.text.DecimalFormat;
import java.util.List;

public class WalletFragmentNEW extends Fragment {
    private View view;
    private TickerView tvBalance;
    private TextView tvCurrency;
    private String currency;
    private DecimalFormat df;
    private SharedPreferences sharedPref;
    private final int QUICK_LIST = R.id.quickList;
    private final int TODAY_REPORT = R.id.todayReport;
    private final int THIS_WEEK_REPORT = R.id.thisWeekReport;
    private final int THIS_MONTH_REPORT = R.id.thisMonthReport;
    private static WalletFragmentNEW instance;
    private boolean contentLoaded = false;

    public static WalletFragmentNEW getInstance() {
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wallet, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
        instance = this;

        tvCurrency = view.findViewById(R.id.currency);
        tvBalance = view.findViewById(R.id.balance);

        tvCurrency.setText(currency);
        tvBalance.setCharacterLists(TickerUtils.provideNumberList());
        tvBalance.setAnimationInterpolator(new DecelerateInterpolator());
        tvBalance.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        tvBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUpdateBalance dialogUpdateBalance = new DialogUpdateBalance(getActivity());
                dialogUpdateBalance.show(getActivity().getSupportFragmentManager(), "update balance dialog");
            }
        });
        tvBalance.setText(sharedPref.getString("balance", "0.00"));

        //reset picked date (for bug fix only)
        sharedPref.edit().putInt("reports_year", 0).apply();
        sharedPref.edit().putInt("reports_month", 0).apply();
        sharedPref.edit().putInt("reports_week", 0).apply();
        sharedPref.edit().putInt("reports_day", 0).apply();

        SpeedDialView fab = view.findViewById(R.id.fab);
        fab.inflate(R.menu.wallet_fab_menu);
        fab.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                int transactionType = 0;
                switch (actionItem.getId()) {
                    case R.id.add_expense: {
                        transactionType = Constants.EXPENSE;
                        break;
                    }
                    case R.id.add_income: {
                        transactionType = Constants.INCOME;
                        break;
                    }
                    case R.id.add_transfer: {
                        if (sharedPref.getBoolean("haveAccounts", false))
                            transactionType = Constants.TRANSFER;
                        break;
                    }
                }
                if (transactionType != 0) {
                    new DialogWalletInput(transactionType).show(getChildFragmentManager(), "wallet input dialog");
                    fab.close();
                } else {
                    new MaterialAlertDialogBuilder(getActivity())
                            .setTitle(R.string.acc_na)
                            .setMessage(R.string.acc_na_des)
                            .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(getActivity(), NewAccount.class));
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!contentLoaded) {
            loadContent();
            contentLoaded = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showNegativeWarning();
    }

    private void loadContent() {
        loadContentItem(QUICK_LIST);
        loadContentItem(TODAY_REPORT);
        loadContentItem(THIS_WEEK_REPORT);
        loadContentItem(THIS_MONTH_REPORT);
    }

    private void loadContentItem(int itemId) {
        Fragment fragment = null;
        String fragmentTag = null;
        int containerId = R.id.content_container;
        switch (itemId) {
            case QUICK_LIST: {
                loadQuickChips();
                break;
            }
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

        if (fragment != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("fromWallet", true);
            fragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction().add(containerId, fragment, fragmentTag).commit();
        }
    }

    private void loadQuickChips() {
        QuickListViewModel quickListViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(QuickListViewModel.class);
        List<QuickItem> fullQuickList = quickListViewModel.getQuickItemsList();
        if (fullQuickList.size() != 0) {
            for (int i = 0; i < fullQuickList.size(); i++) {
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ChipGroup chipGroup = view.findViewById(R.id.quickChipGroup);
                Chip chip = new Chip(getActivity());
                chip.setText(fullQuickList.get(i).getItemName() + " (" + currency + fullQuickList.get(i).getItemPrice() + ")");
                chip.setElevation(8f);
                chip.setHint(fullQuickList.get(i).getCategory());
                chip.setChipStrokeWidth(4f);
                chip.setChipStrokeColorResource(R.color.colorAccent);
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addExpense(
                                chip.getText().toString().split("\\(")[0], //item name
                                chip.getText().toString().split("\\(")[1]
                                        .replace(currency, "")
                                        .replace(")", ""), //price
                                chip.getHint().toString() //category + color (previously set as hint)
                        );
                    }
                });
                chipGroup.addView(chip, i, params);
            }
        } else {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ChipGroup chipGroup = view.findViewById(R.id.quickChipGroup);
            Chip chip = new Chip(getActivity());
            chip.setText(R.string.example_quick_item_text);
            chip.setElevation(8f);
            chip.setChipStrokeWidth(4f);
            chip.setChipStrokeColorResource(R.color.colorAccent);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), EditQuickList.class));
                }
            });
            chipGroup.addView(chip, 0, params);
        }
    }

    private void addExpense(String itemName, String price, String category) {
        String balance = sharedPref.getString("balance", "0.00");
        TransactionItem transactionItem = new TransactionItem(
                balance, "-", price, itemName, String.valueOf(System.currentTimeMillis()), category
        );
        TransactionsViewModel transactionsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);
        transactionsViewModel.insert(transactionItem);
        setNewBalance(String.valueOf(Double.parseDouble(balance) - Double.parseDouble(price)));
        Toast.makeText(getActivity(), R.string.added, Toast.LENGTH_SHORT).show();
    }

    public void setNewBalance(String balance) {
        balance = new Amount(getActivity(), balance).getAmountStringWithoutCurrency();
        sharedPref.edit().putString("balance", balance).apply();
        tvBalance.setText(balance);

        showNegativeWarning();
    }

    private void showNegativeWarning() {
        String balance = sharedPref.getString("balance", "0.00");
        ImageButton imWarning = view.findViewById(R.id.warning);
        if (balance.contains("-")) {
            tvBalance.setTextColor(getActivity().getResources().getColor(android.R.color.holo_red_light));
            tvCurrency.setTextColor(getActivity().getResources().getColor(android.R.color.holo_red_light));
            if (!sharedPref.getBoolean("negativeEnabled", false)) {
                imWarning.setVisibility(View.VISIBLE);
                imWarning.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new MaterialAlertDialogBuilder(getActivity())
                                .setTitle(R.string.neg_balance)
                                .setMessage(R.string.update_balance_des)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                });
            }
        } else {
            imWarning.setVisibility(View.GONE);
            tvBalance.setTextColor(getActivity().getResources().getColor(android.R.color.holo_green_dark));
            tvCurrency.setTextColor(getActivity().getResources().getColor(android.R.color.holo_green_dark));
        }
    }

}
