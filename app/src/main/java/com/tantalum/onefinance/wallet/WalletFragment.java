package com.tantalum.onefinance.wallet;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.MaterialSharedAxis;
import com.leinardi.android.speeddial.SpeedDialView;
import com.nmssalman.bubbleshowcasenew.BubbleShowCase;
import com.nmssalman.bubbleshowcasenew.BubbleShowCaseBuilder;
import com.nmssalman.bubbleshowcasenew.BubbleShowCaseListener;
import com.permissionx.guolindev.PermissionX;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;
import com.tantalum.onefinance.Amount;
import com.tantalum.onefinance.Constants;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.accounts.NewAccount;
import com.tantalum.onefinance.quicklist.QuickListActivity;
import com.tantalum.onefinance.quicklist.QuickItem;
import com.tantalum.onefinance.quicklist.QuickListViewModel;
import com.tantalum.onefinance.reports.DailyReportsFragment;
import com.tantalum.onefinance.reports.MonthlyReportsFragment;
import com.tantalum.onefinance.reports.WeeklyReportsFragment;
import com.tantalum.onefinance.transactions.TransactionItem;
import com.tantalum.onefinance.transactions.TransactionsViewModel;

import java.text.DecimalFormat;
import java.util.List;

public class WalletFragment extends Fragment {
    private View view;
    private TickerView tvBalance;
    private TextView tvCurrency;
    private String currency;
    private DecimalFormat df;
    private SharedPreferences sharedPref;
    private final int QUICK_LIST = 0;
    private final int TODAY_REPORT = 1;
    private final int THIS_WEEK_REPORT = 2;
    private final int THIS_MONTH_REPORT = 3;
    private static WalletFragment instance;
    private boolean contentLoaded = false;
    private SpeedDialView fab;

    public static WalletFragment getInstance() {
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
        tvBalance.setOnClickListener(view -> {
            DialogUpdateBalance dialogUpdateBalance = new DialogUpdateBalance(getActivity());
            dialogUpdateBalance.show(getActivity().getSupportFragmentManager(), "update balance dialog");
        });
        tvBalance.setText(sharedPref.getString("balance", "0.00"));

        //reset picked date (for bug fix only)
        sharedPref.edit().putInt("reports_year", 0).apply();
        sharedPref.edit().putInt("reports_month", 0).apply();
        sharedPref.edit().putInt("reports_week", 0).apply();
        sharedPref.edit().putInt("reports_day", 0).apply();

        ImageView overlay = view.findViewById(R.id.overlay);
        fab = view.findViewById(R.id.fab);
        fab.inflate(R.menu.wallet_fab_menu);
        fab.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                return false;
            }

            @Override
            public void onToggleChanged(boolean isOpen) {
                float alpha;
                if (isOpen) alpha = 0.5f;
                else alpha = 0f;
                overlay.animate().alpha(alpha).setDuration(300).start();
            }
        });
        fab.setOnActionSelectedListener(actionItem -> {
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
                        .setPositiveButton(R.string.add, (dialog, which) -> {
                            Intent intent = new Intent(getActivity(), NewAccount.class);
                            intent.putExtra("isNewAccount", true);
                            startActivity(intent);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
            return false;
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!contentLoaded) {
            loadContent();
            contentLoaded = true;
            showInstruction();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showNegativeWarning();
    }

    private void showInstruction() {
        if (!sharedPref.getBoolean("insWalletShown", false)) {
            new BubbleShowCaseBuilder(requireActivity())
                    .title(getString(R.string.start_here))
                    .description(getString(R.string.tap_plus_or_minus))
                    .targetView(fab)
                    .highlightMode(BubbleShowCase.HighlightMode.VIEW_SURFACE)
                    .listener(new BubbleShowCaseListener() {
                        @Override
                        public void onTargetClick(@NonNull BubbleShowCase bubbleShowCase) {
                            
                        }

                        @Override
                        public void onCloseActionImageClick(@NonNull BubbleShowCase bubbleShowCase) {
                            sharedPref.edit().putBoolean("insWalletShown", true).apply();
                        }

                        @Override
                        public void onBackgroundDimClick(@NonNull BubbleShowCase bubbleShowCase) {
                        }

                        @Override
                        public void onBubbleClick(@NonNull BubbleShowCase bubbleShowCase) {

                        }
                    })
                    .show();
        }
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
        if (!fullQuickList.isEmpty()) {
            for (int i = 0; i < fullQuickList.size(); i++) {
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ChipGroup chipGroup = view.findViewById(R.id.quickChipGroup);
                Chip chip = getChip(fullQuickList, i);
                chipGroup.addView(chip, i, params);
            }
        } else if (!sharedPref.getBoolean("quick_list_helper_dismissed", false)) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ChipGroup chipGroup = view.findViewById(R.id.quickChipGroup);
            Chip chip = new Chip(getActivity());
            chip.setText(R.string.example_quick_item_text);
            chip.setChipIcon(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_quick_list));
            chip.setElevation(8f);
            chip.setChipStrokeWidth(0f);
            chip.setChipCornerRadius(64f);
            chip.setCloseIconVisible(true);
            chip.setOnClickListener(view -> startActivity(new Intent(getActivity(), QuickListActivity.class)));
            chip.setOnCloseIconClickListener(view1 -> {
                sharedPref.edit().putBoolean("quick_list_helper_dismissed", true).apply();
                chip.setVisibility(View.GONE);
            });
            chipGroup.addView(chip, 0, params);
        }
    }

    private Chip getChip(List<QuickItem> fullQuickList, int i) {
        Chip chip = new Chip(getActivity());
        chip.setText(fullQuickList.get(i).getItemName() + " (" + currency + fullQuickList.get(i).getItemPrice() + ")");
        chip.setElevation(8f);
        chip.setChipStrokeWidth(0f);
        chip.setHint(fullQuickList.get(i).getCategory());
        chip.setChipCornerRadius(64f);
        chip.setOnClickListener(view -> addExpense(
                chip.getText().toString().split("\\(")[0], //item name
                chip.getText().toString().split("\\(")[1]
                        .replace(currency, "")
                        .replace(")", ""), //price
                chip.getHint().toString() //category + color (previously set for hint)
        ));
        return chip;
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
        balance = new Amount(requireContext(), balance).getAmountStringWithoutCurrency();
        sharedPref.edit().putString("balance", balance).apply();
        tvBalance.setText(balance);

        showNegativeWarning();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requestNotificationPermission();
    }

    private void showNegativeWarning() {
        String balance = sharedPref.getString("balance", "0.00");
        ImageButton imWarning = view.findViewById(R.id.warning);
        if (balance.contains("-")) {
            tvBalance.setTextColor(getActivity().getResources().getColor(android.R.color.holo_red_light));
            tvCurrency.setTextColor(getActivity().getResources().getColor(android.R.color.holo_red_light));
            if (!sharedPref.getBoolean("negativeEnabled", false)) {
                imWarning.setVisibility(VISIBLE);
                imWarning.setOnClickListener(view -> new MaterialAlertDialogBuilder(getActivity())
                        .setTitle(R.string.neg_balance)
                        .setMessage(R.string.update_balance_des)
                        .setPositiveButton(R.string.ok, null)
                        .show());
            }
        } else {
            imWarning.setVisibility(View.GONE);
            tvBalance.setTextColor(getActivity().getResources().getColor(android.R.color.holo_green_dark));
            tvCurrency.setTextColor(getActivity().getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission() {
        if (!PermissionX.areNotificationsEnabled(requireContext()))
            PermissionX.init(this)
                    .permissions(Manifest.permission.POST_NOTIFICATIONS)
                    .onExplainRequestReason((scope, deniedList) ->
                            scope.showRequestReasonDialog(
                                    deniedList,
                                    getString(R.string.notification_permission_reason),
                                    getString(R.string.yes),
                                    getString(R.string.no)
                            ))
                    .request((allGranted, grantedList, deniedList) -> {
                                if (!allGranted)
                                    new MaterialAlertDialogBuilder(requireContext())
                                            .setTitle(R.string.notifications_enabled)
                                            .setMessage(R.string.notifications_enabled_description)
                                            .setPositiveButton(R.string.ok, null)
                                            .show();
                            }
                    );
    }

}
