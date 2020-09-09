package com.tashila.mywalletfree;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class QuickListAdapter extends ListAdapter<QuickItem, QuickListAdapter.QuickItemHolder> {
    private Context context;
    private String currency;

    protected QuickListAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        SharedPreferences sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
    }

    private static final DiffUtil.ItemCallback<QuickItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<QuickItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull QuickItem oldItem, @NonNull QuickItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull QuickItem oldItem, @NonNull QuickItem newItem) {
            return oldItem.getItemName().equals(newItem.getItemName()) &&
                    oldItem.getItemPrice().equals(newItem.getItemPrice());
        }
    };

    @NonNull
    @Override
    public QuickItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_quick_item, parent, false);
        return new QuickItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickItemHolder holder, int position) {
        final QuickItem quickItem = getItem(position);
        holder.tvItemName.setText(quickItem.getItemName());
        holder.tvItemPrice.setText(currency + quickItem.getItemPrice());
        holder.imRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditQuickList) context).removeItemNEW(quickItem);
            }
        });
    }


    class QuickItemHolder extends RecyclerView.ViewHolder {
        private TextView tvItemName;
        private TextView tvItemPrice;
        private ImageButton imRemove;

        public QuickItemHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.itemName);
            tvItemPrice = itemView.findViewById(R.id.itemPrice);
            imRemove = itemView.findViewById(R.id.itemRemove);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((EditQuickList) context).editItem(getItem(getAdapterPosition()));
                }
            });
        }
    }

}
