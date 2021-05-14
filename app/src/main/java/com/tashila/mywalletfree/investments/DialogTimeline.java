package com.tashila.mywalletfree.investments;

import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tashila.mywalletfree.R;

import org.qap.ctimelineview.TimelineRow;
import org.qap.ctimelineview.TimelineViewAdapter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogTimeline extends BottomSheetDialogFragment {
    private final Investment investment;
    private View view;


    public DialogTimeline(Investment investment) {
        this.investment = investment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_timeline, null);

        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(view);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        ListView timeline = view.findViewById(R.id.timeline);
        List<String> history = investment.getHistory();
        ArrayList<TimelineRow> timelineRowsList = new ArrayList<>();
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
}
