package com.tantalum.financejournal.bank;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tantalum.financejournal.DateTimeHandler;
import com.tantalum.financejournal.R;

public class AccountActivitiesAdapter extends ListAdapter<String, AccountActivitiesAdapter.PassBookViewHolder> {

    protected AccountActivitiesAdapter() {
        super(DIFF_CALLBACK);
    }

    public static final DiffUtil.ItemCallback<String> DIFF_CALLBACK = new DiffUtil.ItemCallback<String>() {
        @Override
        public boolean areItemsTheSame(@NonNull String s, @NonNull String t1) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull String s, @NonNull String t1) {
            return false;
        }
    };

    @NonNull
    @Override
    public PassBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_acc_activity_item, null);
        return new PassBookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PassBookViewHolder holder, int position) {
        String activity = getItem(position);
        holder.tvTransaction.setText(activity.split("###")[0]);
        holder.tvTimestamp.setText(new DateTimeHandler(activity.split("###")[1]).getTimestamp());
    }

    class PassBookViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTransaction;
        private TextView tvTimestamp;

        public PassBookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransaction = itemView.findViewById(R.id.transaction);
            tvTimestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}
