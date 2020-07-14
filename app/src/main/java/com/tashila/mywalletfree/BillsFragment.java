package com.tashila.mywalletfree;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class BillsFragment extends Fragment {
    public static final String TAG = "BillsFragment";
    View view;
    private static BillsFragment instance;
    private ViewPagerAdapter viewPagerAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_bills, null);
        instance = this;

        ViewPager viewPager = view.findViewById(R.id.bills_view_pager);
        TabLayout tabLayout = view.findViewById(R.id.bills_tab_layout);

        DueFragment dueFragment = new DueFragment();
        PaidFragment paidFragment = new PaidFragment();

        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 0);
        viewPagerAdapter.addFragment(dueFragment, "Due");
        viewPagerAdapter.addFragment(paidFragment, "Paid");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton billsFAB = view.findViewById(R.id.billsFAB);
        billsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickFAB();
            }
        });

        return view;
    }

    public static BillsFragment getInstance() {
        return instance;
    }

    private void onClickFAB() {
        DialogNewBill dialogNewBill = new DialogNewBill();
        dialogNewBill.show(getActivity().getSupportFragmentManager(), "new bill dialog");
    }

    public void addBill(String title, String amount, String date, String remarks, String paymentStatus) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //View rootFragment = inflater.inflate(R.layout.frag_bills, null);
        View paidFragment = inflater.inflate(R.layout.frag_paid, (ViewGroup) view);
        View dueFragment = inflater.inflate(R.layout.frag_due, (ViewGroup) view);
        LinearLayout paidContainer = paidFragment.findViewById(R.id.paid_container);
        LinearLayout dueContainer = dueFragment.findViewById(R.id.due_container);
        View sampleBill = inflater.inflate(R.layout.sample_bill, null);
        TextView billTitle = sampleBill.findViewById(R.id.billTitle);
        TextView billAmount = sampleBill.findViewById(R.id.billAmount);
        TextView billDate = sampleBill.findViewById(R.id.billDate);
        TextView billRemarks = sampleBill.findViewById(R.id.billRemarks);

        billTitle.setText(title);
        billAmount.setText(amount);
        billDate.setText(date);
        billRemarks.setText(remarks);
        sampleBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHiddenDetails();
            }
        });

        if (paymentStatus.equals("paid")) {
            Log.i(TAG, "paid bill added!");
            paidContainer.addView(sampleBill);
            paidContainer.invalidate();
        }
        if (paymentStatus.equals("due")) {
            Log.i(TAG, "paid bill added!");
            dueContainer.addView(sampleBill);
            dueContainer.invalidate();
        }
        viewPagerAdapter.notifyDataSetChanged();
    }

    public void showHiddenDetails() {
        view.findViewById(R.id.billDate).setVisibility(View.VISIBLE);
        view.findViewById(R.id.billRemarks).setVisibility(View.VISIBLE);
        view.invalidate();
    }


    //--------------------------------------------------------------------------------------------//

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
