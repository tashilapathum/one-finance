package com.tashila.mywalletfree;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PaidFragment extends Fragment {
    public static final String TAG = "PaidFragment";
    View view;
    private RecyclerView recyclerView;
    private BillsViewModel billsViewModel;
    private SharedPreferences sharedPref;
    private String theme;
    private RelativeLayout bills_instructions;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_paid, null);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        theme = sharedPref.getString("theme", "light");

        recyclerView = view.findViewById(R.id.paidRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        final BillsAdapter billsAdapter = new BillsAdapter(getActivity());
        recyclerView.setAdapter(billsAdapter);

        billsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(BillsViewModel.class);
        billsViewModel.getAllBills(true).observe(getActivity(), new Observer<List<Bill>>() {
            @Override
            public void onChanged(List<Bill> bills) {
                billsAdapter.submitList(bills);
                toggleInsVisibility(bills.size());
            }
        });

        showInstructions();
        return view;
    }

    private void showInstructions() {
        bills_instructions = view.findViewById(R.id.bills_instructions);
        if (theme.equalsIgnoreCase("dark")) {
            ImageView im1 = bills_instructions.findViewById(R.id.paid);
            ImageView im2 = bills_instructions.findViewById(R.id.unpaid);
            ImageView im3 = bills_instructions.findViewById(R.id.add);
            new Essentials(getActivity()).invertDrawable(im1);
            new Essentials(getActivity()).invertDrawable(im2);
            new Essentials(getActivity()).invertDrawable(im3);
        }
    }

    private void toggleInsVisibility(int itemCount) {
        if (itemCount > 0)
            bills_instructions.setVisibility(View.GONE);
        else
            bills_instructions.setVisibility(View.VISIBLE);
    }
}
