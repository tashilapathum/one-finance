package com.tashila.mywalletfree.investments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.transition.MaterialSharedAxis;
import com.tashila.mywalletfree.Amount;
import com.tashila.mywalletfree.DateTimeHandler;
import com.tashila.mywalletfree.R;

import org.qap.ctimelineview.TimelineRow;
import org.qap.ctimelineview.TimelineViewAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InvestmentView extends Fragment {
    private Investment investment;
    private InvestmentsViewModel investmentsViewModel;
    private TextView tvInvestedValue;
    private TextView tvReturnValue;
    private TextView tvProfitValue;
    private TextView tvTimePeriod;

    public InvestmentView(Investment investment) {
        this.investment = investment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_investment_view, container, false);

        investmentsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.
                getInstance(getActivity().getApplication())).get(InvestmentsViewModel.class);

        view.findViewById(R.id.editInvestment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editInvestment(investment);
            }
        });

        view.findViewById(R.id.deleteInvestment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteInvestment(investment);
            }
        });

        tvInvestedValue = view.findViewById(R.id.investedValue);
        tvReturnValue = view.findViewById(R.id.returnValue);
        tvProfitValue = view.findViewById(R.id.profitValue);
        tvTimePeriod = view.findViewById(R.id.timePeriod);

        //basic details
        double investValue = investment.getInvestValue();
        double returnValue = investment.getReturnValue();

        tvInvestedValue.setText(new Amount(getActivity(), investValue).getAmountString());
        tvReturnValue.setText(new Amount(getActivity(), returnValue).getAmountString());
        tvProfitValue.setText(
                new Amount(getActivity(), investValue - returnValue).getAmountString() //profit value
                + " (" +
                new DecimalFormat("0.##").format(((investValue - returnValue) / investValue) * 100) //profit percentage
                + "%)"
        );
        tvTimePeriod.setText(new DateTimeHandler(String.valueOf(investment.getDateInMillis())).getPassedTime(getActivity()));

        //timeline
        List<String> history = investment.getHistory();
        ArrayList<TimelineRow> timelineRowsList = new ArrayList<>();
        for (int i=0; i<history.size(); i++) {
            String historyItem = history.get(i);
            TimelineRow timelineRow = new TimelineRow(i);
            timelineRow.setDescription(historyItem);
            timelineRowsList.add(timelineRow);
        }

        ArrayAdapter<TimelineRow> timelineViewAdapter = new TimelineViewAdapter(getActivity(), 0, timelineRowsList, false);
        ListView timeline = view.findViewById(R.id.timeline);
        timeline.setAdapter(timelineViewAdapter);

        return view;
    }

    public void editInvestment(Investment investment) {
        DialogAddInvestment dialogAddInvestment = new DialogAddInvestment(investment);
        dialogAddInvestment.show(getActivity().getSupportFragmentManager(), "edit investment dialog");
    }

    public void deleteInvestment(final Investment investment) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm)
                .setMessage(R.string.delete_inv_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        investmentsViewModel.delete(investment);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
