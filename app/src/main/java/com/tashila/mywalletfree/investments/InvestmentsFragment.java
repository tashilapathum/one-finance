package com.tashila.mywalletfree.investments;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialSharedAxis;
import com.tashila.mywalletfree.R;

import java.util.List;

public class InvestmentsFragment extends Fragment {
    private RecyclerView recyclerView;
    private static InvestmentsFragment instance;
    private InvestmentsViewModel investmentsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_investments, container, false);
        instance = this;

        FloatingActionButton invFAB = view.findViewById(R.id.fab);
        invFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickFAB();
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.scheduleLayoutAnimation();
        InvestmentsAdapter investmentsAdapter = new InvestmentsAdapter();
        recyclerView.setAdapter(investmentsAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        investmentsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.
                getInstance(getActivity().getApplication())).get(InvestmentsViewModel.class);
        investmentsViewModel.getAllInvestments().observe(getActivity(), new Observer<List<Investment>>() {
            @Override
            public void onChanged(List<Investment> investments) {
                investmentsAdapter.submitList(investments);
            }
        });

        return view;
    }

    public static InvestmentsFragment getInstance() {
        return instance;
    }

    private void onClickFAB() {
        DialogAddInvestment dialogAddInvestment = new DialogAddInvestment(null);
        dialogAddInvestment.show(getChildFragmentManager(), "add investments dialog"); //altered this
    }

    public void addInvestment(Investment investment) {
        investmentsViewModel.insert(investment);
    }

    public void openInvestment(Investment investment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new InvestmentView(investment), "investment view")
                .addToBackStack(null)
                .commit();
    }

    public void updateInvestment(Investment editingInvestment) {
        investmentsViewModel.update(editingInvestment);
        Toast.makeText(getActivity(), R.string.updated, Toast.LENGTH_SHORT).show();
    }


}
