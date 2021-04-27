package com.tashila.mywalletfree.loans;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.tashila.mywalletfree.R;

public class LoansAdapter extends ListAdapter<Loan, LoansAdapter.LoanHolder> {
    public static final String TAG = "LoansAdapter";
    private Context context;
    private SharedPreferences sharedPref;
    private String currency;


    public LoansAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
    }

    private static final DiffUtil.ItemCallback<Loan> DIFF_CALLBACK = new DiffUtil.ItemCallback<Loan>() {
        @Override
        public boolean areItemsTheSame(@NonNull Loan oldItem, @NonNull Loan newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Loan oldItem, @NonNull Loan newItem) {
            return oldItem.isBorrowed() == newItem.isBorrowed()
                    && oldItem.isSettled() == newItem.isSettled()
                    && oldItem.getAmount().equals(newItem.getAmount())
                    && oldItem.getDetails().equals(newItem.getDetails());
        }
    };

    @NonNull
    @Override
    public LoanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_loan, parent, false);
        return new LoanHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LoanHolder holder, int position) {
        final Loan currentLoan = getItem(position);
        if (currentLoan.isLent()) {
            holder.tvLentBorrowed.setText(R.string.lent_);
            holder.tvTitle.setText("Lent to " + currentLoan.getPerson());
        } else {
            holder.tvLentBorrowed.setText(R.string.borrowed_);
            holder.tvTitle.setText("Borrowed from " + currentLoan.getPerson());
        }
        holder.tvAmount.setText(currency + currentLoan.getAmount());
        holder.tvSettledDate.setText(currentLoan.getSettledDate());
        holder.tvDetails.setText(currentLoan.getDetails());

        holder.imEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoansFragment.getInstance().editLoan(currentLoan);
            }
        });
        holder.imDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoansFragment.getInstance().deleteLoan(currentLoan);
            }
        });

        //settle
        if (currentLoan.isSettled())
            holder.lottieAnimationView.playAnimation();
        else
            holder.lottieAnimationView.setProgress(0.4f); //to show empty circle on start
    }

    class LoanHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvAmount;
        private final TextView tvLentBorrowed;
        private final TextView tvSettledDate;
        private final TextView tvDetails;
        private final ImageButton imEdit;
        private final ImageButton imDelete;
        private final FrameLayout tick_layout;
        private final MaterialCheckBox cbMarkSettled;
        private final LottieAnimationView lottieAnimationView;
        private final LinearLayout invisible_part;

        public LoanHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.title);
            tvAmount = itemView.findViewById(R.id.amount);
            tvLentBorrowed = itemView.findViewById(R.id.txtLentBorrowed);
            tvSettledDate = itemView.findViewById(R.id.settledDate);
            tvDetails = itemView.findViewById(R.id.details);
            tick_layout = itemView.findViewById(R.id.tick_layout);
            cbMarkSettled = itemView.findViewById(R.id.markAsSettled);
            lottieAnimationView = itemView.findViewById(R.id.animatedTick);
            imEdit = itemView.findViewById(R.id.edit);
            imDelete = itemView.findViewById(R.id.delete);
            invisible_part = itemView.findViewById(R.id.invisible_part);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleVisibility();
                }
            });

            tick_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!getItem(getBindingAdapterPosition()).isSettled())
                        lottieAnimationView.playAnimation();
                    else
                        lottieAnimationView.setProgress(0.4f);
                    cbMarkSettled.toggle();
                }
            });

            cbMarkSettled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            LoansFragment.getInstance().toggleSettled(getItem(getBindingAdapterPosition()), isChecked);
                        }
                    };
                    Handler handler = new Handler(Looper.getMainLooper());
                    if (isChecked)
                        handler.postDelayed(runnable, 1000); //to move down after tick animation
                    else
                        handler.post(runnable);
                }
            });
        }

        private void toggleVisibility() {
            if (invisible_part.getVisibility() == View.GONE)
                invisible_part.setVisibility(View.VISIBLE);
            else
                invisible_part.setVisibility(View.GONE);
        }
    }
}
