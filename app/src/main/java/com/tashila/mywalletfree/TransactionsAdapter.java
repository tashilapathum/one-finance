package com.tashila.mywalletfree;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionsViewHolder> implements Filterable {
    private ArrayList<TransactionItem> mExampleList;
    private ArrayList<TransactionItem> mExampleListFull;

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

    TransactionsAdapter(ArrayList<TransactionItem> exampleList) {
        this.mExampleList = exampleList;
        mExampleListFull = new ArrayList<>(mExampleList);
    }

    @NonNull
    @Override
    public TransactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_item, parent, false);
        TransactionsViewHolder evh = new TransactionsViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionsViewHolder holder, int position) {
        TransactionItem currentItem = mExampleList.get(position);
        holder.mAmount.setText(currentItem.getAmount());
        holder.mDescr.setText(currentItem.getDescr());
        holder.mDate.setText(currentItem.getDate());
    }

    @Override
    public int getItemCount() {
        return mExampleList.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<TransactionItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mExampleListFull);
            }
            else {
                String filteredPattern = constraint.toString().toLowerCase().trim();

                for (TransactionItem item : mExampleListFull) {
                    if (item.getDescr().toLowerCase().contains(filteredPattern) || item.getDate().toLowerCase().contains(filteredPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mExampleList.clear();
            mExampleList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
