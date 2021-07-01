package com.tantalum.financejournal.transactions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.tantalum.financejournal.DateTimeHandler;
import com.tantalum.financejournal.R;

public class TransactionsAdapter extends ListAdapter<TransactionItem, TransactionsAdapter.TransactionsViewHolder> {
    public static final String TAG = "TransactionsAdapter";
    private String currency;
    public Context context;
    private OnTransactionClickListener listener;

    public TransactionsAdapter(@NonNull Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        SharedPreferences sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
    }

    private static final DiffUtil.ItemCallback<TransactionItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransactionItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionItem oldItem, @NonNull TransactionItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionItem oldItem, @NonNull TransactionItem newItem) {
            return oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getAmount().equals(newItem.getAmount()) &&
                    oldItem.getTimeInMillis().equals(newItem.getTimeInMillis());
        }
    };

    @NonNull
    @Override
    public TransactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_transaction_item, parent, false);
        return new TransactionsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionsViewHolder holder, int position) {
        TransactionItem currentItem = getItem(position);
        //amount
        holder.mAmount.setText(currentItem.getPrefix() + currency + currentItem.getAmount());
        if (currentItem.getPrefix().equals("+"))
            holder.mAmount.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        else
            holder.mAmount.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));

        //description
        if (currentItem.getDescription().contains("###")) //for withdrawals
            holder.mDescr.setText(currentItem.getDescription().split("###")[0]);
        else
            holder.mDescr.setText(currentItem.getDescription());

        //category
        Chip categoryChip = holder.mCategory;
        if (currentItem.getCategory() != null) {
            categoryChip.setText(currentItem.getCategory().split("###")[0]);
            categoryChip.setChipBackgroundColor(ColorStateList.valueOf(Integer.parseInt(currentItem.getCategory().split("###")[1])));
        }
        else categoryChip.setVisibility(View.GONE);

        //date
        holder.mDate.setText(new DateTimeHandler(currentItem.getTimeInMillis()).getTimestamp());
    }

    public TransactionItem getTransactionItemAt(int position) {
        return getItem(position);
    }

    class TransactionsViewHolder extends RecyclerView.ViewHolder {
        private TextView mAmount;
        private TextView mDescr;
        private TextView mDate;
        private Chip mCategory;

        TransactionsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION)
                        listener.OnTransactionClick(getItem(position));
                }
            });
            mAmount = itemView.findViewById(R.id.hAmount);
            mDescr = itemView.findViewById(R.id.hDescr);
            mDate = itemView.findViewById(R.id.hDate);
            mCategory = itemView.findViewById(R.id.hCategory);
        }
    }

    public interface OnTransactionClickListener {
        void OnTransactionClick(TransactionItem transactionItem);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

}
