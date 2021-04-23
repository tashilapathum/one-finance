package com.tashila.mywalletfree;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class LoansFragment extends Fragment {
    public static final String TAG = "LoansFragment";
    View view;
    private static LoansFragment instance;
    private LoansViewModel loansViewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_loans, null);
        instance = this;

        ViewPager viewPager = view.findViewById(R.id.loans_view_pager);
        TabLayout tabLayout = view.findViewById(R.id.loans_tab_layout);

        DueFragment dueFragment = new DueFragment();
        PaidFragment paidFragment = new PaidFragment();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 0);
        viewPagerAdapter.addFragment(dueFragment, getActivity().getResources().getString(R.string.due));
        viewPagerAdapter.addFragment(paidFragment, getActivity().getResources().getString(R.string.paid));
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton loansFAB = view.findViewById(R.id.loansFAB);
        loansFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickFAB();
            }
        });

        loansViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(LoansViewModel.class);
        return view;
    }

    public static LoansFragment getInstance() {
        return instance;
    }

    private void onClickFAB() {
        DialogNewLoan dialogNewLoan = new DialogNewLoan(null);
        dialogNewLoan.show(getActivity().getSupportFragmentManager(), "new loan dialog");
    }

    public void addLoan(Loan loan) {
        loansViewModel.insert(loan);
    }

    public void markAsPaid(Loan loan) {
        loan.setPaid(true);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        loan.setPaidDate(formatter.format(LocalDate.now()));
        loan.setLastPaidMonth(LocalDate.now().getMonthValue());
        Log.i(TAG, "month value: " + LocalDate.now().getMonthValue());
        loansViewModel.update(loan);
        Toast.makeText(getActivity(), R.string.as_paid, Toast.LENGTH_SHORT).show();
    }

    public void markUnpaid(Loan loan) {
        loan.setPaid(false);
        loan.setPaidDate("N/A");
        loansViewModel.update(loan);
        Toast.makeText(getActivity(), R.string.as_due, Toast.LENGTH_SHORT).show();
    }

    public void editLoan(Loan loan) {
        DialogNewLoan dialogNewLoan = new DialogNewLoan(loan);
        dialogNewLoan.show(getActivity().getSupportFragmentManager(), "edit loan dialog");
    }

    public void updateLoan(Loan editingLoan) {
        loansViewModel.update(editingLoan);
        Toast.makeText(getActivity(), R.string.updated, Toast.LENGTH_SHORT).show();
    }

    public void deleteLoan(final Loan loan) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm)
                .setMessage(R.string.confirm_delete_loan)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loansViewModel.delete(loan);
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
