package com.tashila.mywalletfree;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.GONE;

public class CartAdapter extends ListAdapter<CartItem, CartAdapter.CartItemHolder> {
    public static final String TAG = "CartAdapter";
    private OnCartItemClickListener listener;
    private View itemCard;

    public CartAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<CartItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<CartItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getItemName().equals(newItem.getItemName()) &&
                    oldItem.getItemPrice().equals(newItem.getItemPrice()) &&
                    oldItem.getQuantity() == newItem.getQuantity();
        }
    };

    @NonNull
    @Override
    public CartItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_cart_item, parent, false);
        return new CartItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemHolder holder, int position) {
        CartItem currentCartItem = getItem(position);
        holder.tvItemName.setText(currentCartItem.getItemName());
        holder.tvItemPrice.setText(currentCartItem.getItemPrice());
        holder.tvQuantity.setText(String.valueOf(currentCartItem.getQuantity()));
        holder.tvItemTotal.setText(currentCartItem.getItemTotal());
    }

    public CartItem getCartItemAt(int position) {
        return getItem(position);
    }

    class CartItemHolder extends RecyclerView.ViewHolder {
        private TextView tvItemName;
        private TextView tvItemPrice;
        private TextView tvQuantity;
        private TextView tvItemTotal;

        public CartItemHolder(final View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.itemName);
            tvItemPrice = itemView.findViewById(R.id.itemPrice);
            tvQuantity = itemView.findViewById(R.id.quantity);
            tvItemTotal = itemView.findViewById(R.id.itemTotal);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION)
                        listener.OnCartItemClick(getItem(position));
                }
            });
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    itemCard = itemView;
                    return false;
                }
            });
        }
    }

    public interface OnCartItemClickListener {
        void OnCartItemClick(CartItem cartItem);
    }

    public void setOnCartItemClickListener(OnCartItemClickListener listener) {
        this.listener = listener;
    }

}
