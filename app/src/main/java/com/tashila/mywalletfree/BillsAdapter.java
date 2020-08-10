package com.tashila.mywalletfree;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class BillsAdapter extends ListAdapter<Bill, BillsAdapter.BillHolder> {
    public static final String TAG = "BillsAdapter";
    private Context context;
    private SharedPreferences sharedPref;
    private String currency;
    private String theme;


    public BillsAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        AndroidThreeTen.init(context);
        sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
        theme = sharedPref.getString("theme", "light");
    }

    private static final DiffUtil.ItemCallback<Bill> DIFF_CALLBACK = new DiffUtil.ItemCallback<Bill>() {
        @Override
        public boolean areItemsTheSame(@NonNull Bill oldItem, @NonNull Bill newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Bill oldItem, @NonNull Bill newItem) {
            return false;
        }
    };

    @NonNull
    @Override
    public BillHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_bill, parent, false);
        return new BillHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BillHolder holder, int position) {
        final Bill currentBill = getItem(position);
        holder.tvTitle.setText(currentBill.getTitle());
        if (currentBill.isMonthly())
            holder.tvAmount.setText(currency + currentBill.getAmount() + " (" + context.getResources().getString(R.string.monthly) + ")");
        else
            holder.tvAmount.setText(currency + currentBill.getAmount());
        holder.tvPaidDate.setText(currentBill.getPaidDate());
        holder.tvDueDate.setText(currentBill.getDueDate());
        holder.tvRemarks.setText(currentBill.getRemarks());
        holder.imMarkAsPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillsFragment.getInstance().markAsPaid(currentBill);
            }
        });
        holder.imMarkUnpaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillsFragment.getInstance().markUnpaid(currentBill);
            }
        });
        holder.imEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillsFragment.getInstance().editBill(currentBill);
            }
        });
        holder.imDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillsFragment.getInstance().deleteBill(currentBill);
            }
        });

        //toggle due and paid buttons
        if (currentBill.isPaid()) {
            holder.imMarkAsPaid.setVisibility(View.GONE);
            holder.imMarkUnpaid.setVisibility(View.VISIBLE);
        } else {
            holder.imMarkAsPaid.setVisibility(View.VISIBLE);
            holder.imMarkUnpaid.setVisibility(View.GONE);
        }

        //show and hide "overdue"

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            int today = LocalDate.now().getDayOfYear();
            int dueDay;
            if (!currentBill.getDueDate().equals("N/A")) {
                dueDay = LocalDate.parse(currentBill.getDueDate(), formatter).getDayOfYear();
                if (today > dueDay &&
                        !currentBill.isPaid())
                    holder.tvOverdue.setVisibility(View.VISIBLE);
                else
                    holder.tvOverdue.setVisibility(View.GONE);
            }
        } catch (Exception e) { //because the user might change the localization
            e.printStackTrace();
        }
    }


    class BillHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvAmount;
        private TextView tvPaidDate;
        private TextView tvDueDate;
        private TextView tvRemarks;
        private TextView tvOverdue;
        private ImageButton imMarkAsPaid;
        private ImageButton imEdit;
        private ImageButton imDelete;
        private ImageButton imMarkUnpaid;
        private LinearLayout invisible_part;

        public BillHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.title);
            tvAmount = itemView.findViewById(R.id.amount);
            tvPaidDate = itemView.findViewById(R.id.paidDate);
            tvDueDate = itemView.findViewById(R.id.dueDate);
            tvRemarks = itemView.findViewById(R.id.remarks);
            tvOverdue = itemView.findViewById(R.id.overdue);
            imMarkAsPaid = itemView.findViewById(R.id.markAsPaid);
            imEdit = itemView.findViewById(R.id.edit);
            imDelete = itemView.findViewById(R.id.delete);
            imMarkUnpaid = itemView.findViewById(R.id.markUnpaid);
            invisible_part = itemView.findViewById(R.id.invisible_part);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleVisibility();
                }
            });

            if (theme.equalsIgnoreCase("dark"))
                invertDrawables(imMarkAsPaid, imDelete, imEdit, imMarkUnpaid);
        }

        private void toggleVisibility() {
            if (invisible_part.getVisibility() == View.GONE)
                invisible_part.setVisibility(View.VISIBLE);
            else
                invisible_part.setVisibility(View.GONE);
        }

        private void invertDrawables(ImageButton... imageButtons) {
            for (ImageButton imageButton : imageButtons)
                new Essentials(context).invertDrawable(imageButton);
        }
    }
}
