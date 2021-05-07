package com.tashila.mywalletfree.investments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialSharedAxis;
import com.tashila.mywalletfree.MainActivity;
import com.tashila.mywalletfree.R;

import java.util.List;

public class InvestmentsFragment extends Fragment {
    private RecyclerView recyclerView;
    private static InvestmentsFragment instance;
    private InvestmentsViewModel investmentsViewModel;
    private InvestmentsAdapter investmentsAdapter;

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
        investmentsAdapter = new InvestmentsAdapter();
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

        //filters
        ((Chip) view.findViewById(R.id.profitChip)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    investmentsViewModel.getInvestmentsSortByMostProfitable().observe(getActivity(), new Observer<List<Investment>>() {
                        @Override
                        public void onChanged(List<Investment> investments) {
                            investmentsAdapter.submitList(investments);
                        }
                    });
                } else clearFilter();
            }
        });
        ((Chip) view.findViewById(R.id.returnChip)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    investmentsViewModel.getInvestmentsSortByReturnValue().observe(getActivity(), new Observer<List<Investment>>() {
                        @Override
                        public void onChanged(List<Investment> investments) {
                            investmentsAdapter.submitList(investments);
                        }
                    });
                } else clearFilter();
            }
        });
        ((Chip) view.findViewById(R.id.timeChip)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    investmentsViewModel.getInvestmentsSortByTime().observe(getActivity(), new Observer<List<Investment>>() {
                        @Override
                        public void onChanged(List<Investment> investments) {
                            investmentsAdapter.submitList(investments);
                        }
                    });
                } else clearFilter();
            }
        });
        ((Chip) view.findViewById(R.id.investChip)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    investmentsViewModel.getInvestmentsSortByInvestValue().observe(getActivity(), new Observer<List<Investment>>() {
                        @Override
                        public void onChanged(List<Investment> investments) {
                            investmentsAdapter.submitList(investments);
                        }
                    });
                } else clearFilter();
            }
        });

        return view;
    }

    private void clearFilter() {
        investmentsViewModel.getAllInvestments().observe(getActivity(), new Observer<List<Investment>>() {
            @Override
            public void onChanged(List<Investment> investments) {
                investmentsAdapter.submitList(investments);
            }
        });
    }

    public static InvestmentsFragment getInstance() {
        return instance;
    }

    private void onClickFAB() {
        DialogAddInvestment dialogAddInvestment = new DialogAddInvestment(null);
        dialogAddInvestment.show(getChildFragmentManager(), "add investments dialog");
    }

    public void addInvestment(Investment investment) {
        investmentsViewModel.insert(investment);
    }

    public void openInvestment(Investment investment) {
        if (getChildFragmentManager().findFragmentByTag("InvestmentView") == null) { //avoid opening over and over
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new InvestmentView(investment), "InvestmentView")
                    .addToBackStack(null)
                    .commit();
        }
    }

    public void updateInvestment(Investment editingInvestment) {
        investmentsViewModel.update(editingInvestment);
        Toast.makeText(getActivity(), R.string.updated, Toast.LENGTH_SHORT).show();
    }


}
