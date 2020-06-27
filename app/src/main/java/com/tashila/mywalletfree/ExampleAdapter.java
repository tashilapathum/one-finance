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

public class ExampleAdapter extends RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder> implements Filterable {
    private ArrayList<ExampleItem> mExampleList;
    private ArrayList<ExampleItem> mExampleListFull;

    static class ExampleViewHolder extends RecyclerView.ViewHolder {
        TextView mAmount;
        TextView mDescr;
        TextView mDate;

        ExampleViewHolder(@NonNull View itemView) {
            super(itemView);
            mAmount = itemView.findViewById(R.id.hAmount);
            mDescr = itemView.findViewById(R.id.hDescr);
            mDate = itemView.findViewById(R.id.hDate);
        }
    }

    ExampleAdapter(ArrayList<ExampleItem> exampleList) {
        this.mExampleList = exampleList;
        mExampleListFull = new ArrayList<>(mExampleList);
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_item, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
        ExampleItem currentItem = mExampleList.get(position);
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
            List<ExampleItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mExampleListFull);
            }
            else {
                String filteredPattern = constraint.toString().toLowerCase().trim();

                for (ExampleItem item : mExampleListFull) {
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
