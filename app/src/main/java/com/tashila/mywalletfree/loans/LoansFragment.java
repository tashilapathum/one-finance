package com.tashila.mywalletfree.loans;

import android.content.DialogInterface;
import android.os.Bundle;
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
import com.google.android.material.transition.MaterialSharedAxis;
import com.tashila.mywalletfree.R;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_loans, null);
        instance = this;

        ViewPager viewPager = view.findViewById(R.id.loans_view_pager);
        TabLayout tabLayout = view.findViewById(R.id.loans_tab_layout);

        BorrowedFragment borrowedFragment = new BorrowedFragment();
        LentFragment lentFragment = new LentFragment();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 0);
        viewPagerAdapter.addFragment(borrowedFragment, getActivity().getResources().getString(R.string.borrowed));
        viewPagerAdapter.addFragment(lentFragment, getActivity().getResources().getString(R.string.lent));
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
        DialogAddInvestment dialogAddInvestment = new DialogAddInvestment(null);
        dialogAddInvestment.show(getActivity().getSupportFragmentManager(), "new loan dialog");
    }

    public void addLoan(Loan loan) {
        loansViewModel.insert(loan);
    }

    public void toggleSettled(Loan loan, boolean isSettled) {
        loan.setSettled(isSettled);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        if (isSettled) loan.setSettledDate(formatter.format(LocalDate.now()));
        else loan.setSettledDate(null);
        loansViewModel.update(loan);
    }

    public void editLoan(Loan loan) {
        DialogAddInvestment dialogAddInvestment = new DialogAddInvestment(loan);
        dialogAddInvestment.show(getActivity().getSupportFragmentManager(), "edit loan dialog");
    }

    public void updateLoan(Loan editingLoan) {
        loansViewModel.update(editingLoan);
        Toast.makeText(getActivity(), R.string.updated, Toast.LENGTH_SHORT).show();
    }

    public void deleteLoan(final Loan loan) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm)
                .setMessage(R.string.confirm_item_delete)
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
