package com.tashila.mywalletfree;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LentFragment extends Fragment {
    View view;
    private RecyclerView recyclerView;
    private LoansViewModel loansViewModel;
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
        final LoansAdapter loansAdapter = new LoansAdapter(getActivity());
        recyclerView.setAdapter(loansAdapter);

        loansViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(LoansViewModel.class);
        loansViewModel.getAllLoans(false).observe(getActivity(), new Observer<List<Loan>>() {
            @Override
            public void onChanged(List<Loan> loans) {
                loansAdapter.submitList(loans);
                toggleInsVisibility(loans.size());
            }
        });
        return view;
    }

    private void toggleInsVisibility(int itemCount) {
        RelativeLayout loans_instructions = view.findViewById(R.id.bills_instructions);
        if (itemCount > 0)
            loans_instructions.setVisibility(View.GONE);
        else
            loans_instructions.setVisibility(View.VISIBLE);
    }
}
