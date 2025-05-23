package com.tantalum.onefinance.investments;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.MaterialSharedAxis;
import com.tantalum.onefinance.Amount;
import com.tantalum.onefinance.DateTimeHandler;
import com.tantalum.onefinance.R;

import org.qap.ctimelineview.TimelineRow;
import org.qap.ctimelineview.TimelineViewAdapter;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InvestmentViewFragment extends Fragment {
    public static final String TAG = "InvestmentView";
    private View view;
    private Investment investment;
    private InvestmentsViewModel investmentsViewModel;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvInvestedValue;
    private TextView tvReturnValue;
    private TextView tvProfitValue;
    private TextView tvTimePeriod;
    private TextView tvDate;
    private Chip tagChip;
    private static InvestmentViewFragment instance;
    private ListView timeline;

    public static InvestmentViewFragment getInstance() {
        return instance;
    }

    public InvestmentViewFragment(Investment investment) {
        this.investment = investment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_investment_view, container, false);
        timeline = view.findViewById(R.id.timeline);
        instance = this;

        investmentsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.
                getInstance(getActivity().getApplication())).get(InvestmentsViewModel.class);

        view.findViewById(R.id.editInvestment).setOnClickListener(v -> editInvestment(investment));
        view.findViewById(R.id.deleteInvestment).setOnClickListener(v -> deleteInvestment(investment));
        view.findViewById(R.id.addNote).setOnClickListener(v -> addNote());
        view.findViewById(R.id.addFunds).setOnClickListener(v -> new InputTextDialog(
                getString(R.string.amount),
                InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER,
                inputText -> {
                    if (!inputText.isEmpty()) {
                        //update value
                        double amount = investment.getInvestValue();
                        amount = amount + Double.parseDouble(inputText);
                        investment.setInvestValue(amount);

                        //update timeline
                        String timelineItem = getString(R.string.invested_)
                                + new Amount(getActivity(), amount).getAmountString()
                                + getString(R.string.invested_suffix_si)
                                + "###" + System.currentTimeMillis();
                        List<String> history = investment.getHistory();
                        history.add(timelineItem);
                        investment.setHistory(history);

                        investmentsViewModel.update(investment);
                        showDetails(null);
                        updateTimeline();
                    }
                })
                .show(getChildFragmentManager(), "input text dialog"));
        view.findViewById(R.id.addReturns).setOnClickListener(v -> new InputTextDialog(
                getString(R.string.amount),
                InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER,
                inputText -> {
                    if (!inputText.isEmpty()) {
                        //update amount
                        double amount = investment.getReturnValue();
                        amount = amount + Double.parseDouble(inputText);
                        investment.setReturnValue(amount);

                        //update timeline
                        String timelineItem = getString(R.string.earned_)
                                + new Amount(getActivity(), amount).getAmountString()
                                + getString(R.string.returned_suffix_si)
                                + "###" + System.currentTimeMillis();
                        List<String> history = investment.getHistory();
                        history.add(timelineItem);
                        investment.setHistory(history);

                        investmentsViewModel.update(investment);
                        showDetails(null);
                        updateTimeline();
                    }
                })
                .show(getChildFragmentManager(), "input text dialog"));

        tvTitle = view.findViewById(R.id.title);
        tvDescription = view.findViewById(R.id.description);
        tvInvestedValue = view.findViewById(R.id.investedValue);
        tvReturnValue = view.findViewById(R.id.returnValue);
        tvProfitValue = view.findViewById(R.id.profitValue);
        tvTimePeriod = view.findViewById(R.id.timePeriod);
        tvDate = view.findViewById(R.id.date);
        tagChip = view.findViewById(R.id.tag);

        view.findViewById(R.id.expandHistory).setOnClickListener(view -> {
            DialogTimeline dialogTimeline = new DialogTimeline(investment);
            dialogTimeline.show(getChildFragmentManager(), "timeline dialog");
        });

        //basic details
        showDetails(null);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTimeline();
    }

    public void editInvestment(Investment investment) {
        DialogAddInvestment dialogAddInvestment = new DialogAddInvestment(investment);
        dialogAddInvestment.show(getActivity().getSupportFragmentManager(), "edit investment dialog");
    }

    public void deleteInvestment(final Investment investment) {
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.confirm)
                .setMessage(R.string.delete_inv_confirm)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    investmentsViewModel.delete(investment);
                    getActivity().onBackPressed();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void addNote() {
        new InputTextDialog(
                getString(R.string.note),
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE,
                inputText -> {
                    if (!inputText.isEmpty())
                        saveNote(inputText);
                }).show(getChildFragmentManager(), "input text dialog");
    }

    private void saveNote(String note) {
        List<String> history = investment.getHistory();
        long dateMillis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        history.add(note + "###" + dateMillis);
        investment.setHistory(history);
        investmentsViewModel.update(investment);
        updateTimeline();
    }

    private void updateTimeline() {
        List<String> history = investment.getHistory();
        ArrayList<TimelineRow> timelineRowsList = new ArrayList<>();
        Log.d(TAG, "timelineSize: " + history.size());
        for (int i = history.size() - 1; i >= 0; i--) {
            String historyItem = history.get(i);
            TimelineRow timelineRow = new TimelineRow(i);
            timelineRow.setDate(Date.from(Instant.ofEpochMilli(Long.parseLong(historyItem.split("###")[1]))));
            timelineRow.setDescription(historyItem.split("###")[0]);
            timelineRow.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.blue_dot));
            timelineRow.setBellowLineColor(getResources().getColor(R.color.colorAccentLight));
            timelineRow.setBellowLineSize(4);
            timelineRow.setImageSize(16);
            timelineRowsList.add(timelineRow);
        }

        ArrayAdapter<TimelineRow> timelineViewAdapter = new TimelineViewAdapter(getActivity(), 0, timelineRowsList, false);
        timeline.setScrollingCacheEnabled(false);
        timeline.setAdapter(timelineViewAdapter);
    }

    @SuppressLint("SetTextI18n")
    public void showDetails(Investment editedInvestment) {
        if (editedInvestment != null) investment = editedInvestment;

        double investValue = investment.getInvestValue();
        double returnValue = investment.getReturnValue();

        tvInvestedValue.setText(new Amount(getActivity(), investValue).getAmountString());
        tvReturnValue.setText(new Amount(getActivity(), returnValue).getAmountString());
        tvProfitValue.setText(
                new Amount(getActivity(), returnValue - investValue).getAmountString() //profit value
                        + " (" +
                        new DecimalFormat("0.##").format(((returnValue - investValue) / investValue) * 100) //profit percentage
                        + "%)"
        );

        tvTitle.setText(investment.getTitle());
        tvDescription.setText(investment.getDescription());
        DateTimeHandler dateTimeHandler = new DateTimeHandler(String.valueOf(investment.getDateInMillis()));
        tvTimePeriod.setText(dateTimeHandler.getPassedTime(getActivity()));
        tvDate.setText(dateTimeHandler.getDateStamp());
        if (investment.getTag().isEmpty())
            tagChip.setVisibility(View.GONE);
        else
            tagChip.setText(investment.getTag());
    }
}
