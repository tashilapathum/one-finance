package com.tashila.mywalletfree;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class TransactionsAdapter extends ListAdapter<TransactionItem, TransactionsAdapter.TransactionsViewHolder> {
    public static final String TAG = "TransactionsAdapter";
    private String currency;

    public TransactionsAdapter(String currency) {
        super(DIFF_CALLBACK);
        this.currency = currency;
    }

    private static final DiffUtil.ItemCallback<TransactionItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransactionItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionItem oldItem, @NonNull TransactionItem newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionItem oldItem, @NonNull TransactionItem newItem) {
            return false;
        }
    };

    static class TransactionsViewHolder extends RecyclerView.ViewHolder {
        TextView mAmount;
        TextView mDescr;
        TextView mDate;

        TransactionsViewHolder(@NonNull View itemView) {
            super(itemView);
            mAmount = itemView.findViewById(R.id.hAmount);
            mDescr = itemView.findViewById(R.id.hDescr);
            mDate = itemView.findViewById(R.id.hDate);
        }
    }


    @NonNull
    @Override
    public TransactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_transaction_item, parent, false);
        TransactionsViewHolder evh = new TransactionsViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionsViewHolder holder, int position) {
        TransactionItem currentItem = getItem(position);
        holder.mAmount.setText(currency + currentItem.getAmount());
        holder.mDescr.setText(currentItem.getDescription());
        holder.mDate.setText(currentItem.getUserDate());
    }
}
