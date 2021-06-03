package com.tantalum.financejournal.bills;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tantalum.financejournal.R;

public class DueFragment extends Fragment {
    View view;
    private RecyclerView recyclerView;
    private BillsViewModel billsViewModel;
    private SharedPreferences sharedPref;
    private String theme;
    private int itemCount;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_due, null);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        theme = sharedPref.getString("theme", "light");

        recyclerView = view.findViewById(R.id.dueRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        final BillsAdapter billsAdapter = new BillsAdapter(getActivity());
        recyclerView.setAdapter(billsAdapter);

        billsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(BillsViewModel.class);
        billsViewModel.getAllBills(false).observe(getActivity(), new Observer<List<Bill>>() {
            @Override
            public void onChanged(List<Bill> bills) {
                billsAdapter.submitList(bills);
                toggleInsVisibility(bills.size());
            }
        });
        return view;
    }

    private void toggleInsVisibility(int itemCount) {
        RelativeLayout bills_instructions = view.findViewById(R.id.bills_instructions);
        if (itemCount > 0)
            bills_instructions.setVisibility(View.GONE);
        else
            bills_instructions.setVisibility(View.VISIBLE);
    }
}
