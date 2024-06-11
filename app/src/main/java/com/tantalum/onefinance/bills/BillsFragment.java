package com.tantalum.onefinance.bills;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.transition.MaterialSharedAxis;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.pro.UpgradeHandler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

public class BillsFragment extends Fragment {
    public static final String TAG = "BillsFragment";
    View view;
    private static BillsFragment instance;
    private BillsViewModel billsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_bills, container, false);
        instance = this;

        ViewPager viewPager = view.findViewById(R.id.bills_view_pager);
        TabLayout tabLayout = view.findViewById(R.id.bills_tab_layout);

        DueFragment dueFragment = new DueFragment();
        PaidFragment paidFragment = new PaidFragment();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 0);
        viewPagerAdapter.addFragment(dueFragment, getActivity().getResources().getString(R.string.due));
        viewPagerAdapter.addFragment(paidFragment, getActivity().getResources().getString(R.string.paid));
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton billsFAB = view.findViewById(R.id.billsFAB);
        billsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickFAB();
            }
        });

        billsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(BillsViewModel.class);
        return view;
    }

    public static BillsFragment getInstance() {
        return instance;
    }

    private void onClickFAB() {
        DialogNewBill dialogNewBill = new DialogNewBill(null);
        dialogNewBill.show(getActivity().getSupportFragmentManager(), "new bill dialog");
    }

    public void addBill(Bill bill) {
        billsViewModel.insert(bill);
    }

    public void markAsPaid(Bill bill) {
        bill.setPaid(true);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        bill.setPaidDate(formatter.format(LocalDate.now()));
        bill.setLastPaidMonth(LocalDate.now().getMonthValue());
        Log.i(TAG, "month value: " + LocalDate.now().getMonthValue());
        billsViewModel.update(bill);
        Toast.makeText(getActivity(), R.string.as_paid, Toast.LENGTH_SHORT).show();
    }

    public void markUnpaid(Bill bill) {
        bill.setPaid(false);
        bill.setPaidDate("N/A");
        billsViewModel.update(bill);
        Toast.makeText(getActivity(), R.string.as_due, Toast.LENGTH_SHORT).show();
    }

    public void editBill(Bill bill) {
        DialogNewBill dialogNewBill = new DialogNewBill(bill);
        dialogNewBill.show(getActivity().getSupportFragmentManager(), "edit bill dialog");
    }

    public void updateBill(Bill editingBill) {
        billsViewModel.update(editingBill);
        Toast.makeText(getActivity(), R.string.updated, Toast.LENGTH_SHORT).show();
    }

    public void deleteBill(final Bill bill) {
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.confirm)
                .setMessage(R.string.confirm_delete_bill)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        billsViewModel.delete(bill);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    //------------------------------- for tabbed layout ----------------------------------//

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> fragmentTitlesList = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitlesList.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitlesList.get(position);
        }
    }
}
