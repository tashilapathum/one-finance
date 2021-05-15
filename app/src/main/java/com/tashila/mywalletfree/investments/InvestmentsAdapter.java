package com.tashila.mywalletfree.investments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.tashila.mywalletfree.Amount;
import com.tashila.mywalletfree.DateTimeHandler;
import com.tashila.mywalletfree.R;

import java.time.LocalDate;
import java.time.Period;

public class InvestmentsAdapter extends ListAdapter<Investment, InvestmentsAdapter.InvestmentViewHolder> {

    public InvestmentsAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Investment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Investment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Investment oldItem, @NonNull Investment newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Investment oldItem, @NonNull Investment newItem) {
            return false;
        }
    };

    @NonNull
    @Override
    public InvestmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_investment, parent, false);
        return new InvestmentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InvestmentViewHolder holder, int position) {
        Investment currentInvestment = getItem(position);
        Context context = holder.itemView.getContext();
        holder.tvTitle.setText(currentInvestment.getTitle());
        holder.tvDescription.setText(currentInvestment.getDescription());

        //tag
        if (currentInvestment.getTag().isEmpty())
            holder.chipTag.setVisibility(View.GONE);
        else
            holder.chipTag.setText(currentInvestment.getTag());

        //invested money
        Amount investValue = new Amount(context, currentInvestment.getInvestValue());
        holder.tvInvestValue.setText(investValue.getAmountString());

        //profit
        Amount profit = new Amount(context, currentInvestment.getReturnValue() - currentInvestment.getInvestValue());
        holder.tvProfitValue.setText(profit.getAmountString());

        //passed time
        String passedTime = new DateTimeHandler(String.valueOf(currentInvestment.getDateInMillis())).getPassedTime(context);
        holder.tvTimePassed.setText(passedTime);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InvestmentsFragment.getInstance().openInvestment(currentInvestment);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class InvestmentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvInvestValue;
        private TextView tvProfitValue;
        private TextView tvTimePassed;
        private Chip chipTag;

        public InvestmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.title);
            tvDescription = itemView.findViewById(R.id.description);
            tvInvestValue = itemView.findViewById(R.id.investValue);
            tvProfitValue = itemView.findViewById(R.id.profitValue);
            tvTimePassed = itemView.findViewById(R.id.timePassed);
            chipTag = itemView.findViewById(R.id.tag);
        }
    }
}
