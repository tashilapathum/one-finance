package com.tantalum.onefinance.investments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialSharedAxis;
import com.tantalum.onefinance.Constants;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.pro.UpgradeHandler;
import com.tantalum.onefinance.pro.UpgradeToProActivity;

public class InvestmentsFragment extends Fragment {
    private Context context;
    private RecyclerView recyclerView;
    private static InvestmentsFragment instance;
    private InvestmentsViewModel investmentsViewModel;
    private InvestmentsAdapter investmentsAdapter;
    private LinearLayout inv_instructions;
    private FloatingActionButton invFAB;
    private OnInvestmentClickListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_investments, container, false);
        instance = this;
        context = getActivity();

        invFAB = view.findViewById(R.id.fab);
        invFAB.setOnClickListener(view1 -> onClickFAB());

        inv_instructions = view.findViewById(R.id.inv_instructions);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.scheduleLayoutAnimation();
        investmentsAdapter = new InvestmentsAdapter();
        investmentsAdapter.setOnItemClickListener(investment -> listener.onClick(investment));
        recyclerView.setAdapter(investmentsAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        investmentsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.
                getInstance(getActivity().getApplication())).get(InvestmentsViewModel.class);
        investmentsViewModel.getAllInvestments().observe(getActivity(), investments -> {
            investmentsAdapter.submitList(investments);
            toggleInsVisibility(investments.size());
        });

        //filters
        ((Chip) view.findViewById(R.id.recentChip)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                investmentsViewModel.getAllInvestments().observe(getActivity(), investments ->
                        investmentsAdapter.submitList(investments)
                );
            } else clearFilter();
        });
        ((Chip) view.findViewById(R.id.profitChip)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                investmentsViewModel.getInvestmentsSortByMostProfitable().observe(getActivity(), investments ->
                        investmentsAdapter.submitList(investments)
                );
            } else clearFilter();
        });
        ((Chip) view.findViewById(R.id.returnChip)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                investmentsViewModel.getInvestmentsSortByReturnValue().observe(getActivity(), investments ->
                        investmentsAdapter.submitList(investments)
                );
            } else clearFilter();
        });
        ((Chip) view.findViewById(R.id.timeChip)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                investmentsViewModel.getInvestmentsSortByTime().observe(getActivity(), investments ->
                        investmentsAdapter.submitList(investments)
                );
            } else clearFilter();
        });
        ((Chip) view.findViewById(R.id.investChip)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                investmentsViewModel.getInvestmentsSortByInvestValue().observe(getActivity(), investments ->
                        investmentsAdapter.submitList(investments)
                );
            } else clearFilter();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        invFAB.show();
    }

    private void clearFilter() {
        investmentsAdapter.notifyDataSetChanged();
    }

    public static InvestmentsFragment getInstance() {
        return instance;
    }

    private void onClickFAB() {
        if (UpgradeHandler.isProActive(context) || investmentsAdapter.getItemCount() < Constants.FREE_INVESTMENTS_LIMIT) {
            DialogAddInvestment dialogAddInvestment = new DialogAddInvestment(null);
            dialogAddInvestment.show(getChildFragmentManager(), "add investments dialog");
        } else {
            new MaterialAlertDialogBuilder(getActivity())
                    .setTitle(R.string.reached_free_limit)
                    .setMessage(R.string.unlimited_inv_descr)
                    .setPositiveButton(R.string.buy, (dialog, which) -> {
                        Intent intent = new Intent(getActivity(), UpgradeToProActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    public void addInvestment(Investment investment) {
        investmentsViewModel.insert(investment);
    }

    public interface OnInvestmentClickListener {
        void onClick(Investment investment);
    }

    public void setInvestmentClickListener(OnInvestmentClickListener listener) {
        this.listener = listener;
    }

    public void updateInvestment(Investment editingInvestment) {
        investmentsViewModel.update(editingInvestment);
        Toast.makeText(getActivity(), R.string.updated, Toast.LENGTH_SHORT).show();
    }

    private void toggleInsVisibility(int itemCount) {
        inv_instructions.setAlpha(0.5f);
        if (itemCount > 0)
            inv_instructions.setVisibility(View.GONE);
        else
            inv_instructions.setVisibility(View.VISIBLE);
    }

}
