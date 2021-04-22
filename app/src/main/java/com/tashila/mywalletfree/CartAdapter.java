package com.tashila.mywalletfree;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class CartAdapter extends ListAdapter<CartItem, CartAdapter.CartItemHolder> {
    public static final String TAG = "CartAdapter";
    private OnCartItemClickListener listener;

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
    public void onBindViewHolder(@NonNull CartItemHolder holder, final int position) {
        CartItem currentCartItem = getItem(position);
        holder.tvItemName.setText(currentCartItem.getItemName());
        holder.tvItemPrice.setText(currentCartItem.getItemPrice());
        holder.tvQuantity.setText(String.valueOf(currentCartItem.getQuantity()));
        holder.tvItemTotal.setText(currentCartItem.getItemTotal());
        if (currentCartItem.isChecked())
            holder.cbItemCheck.setChecked(true);
        else
            holder.cbItemCheck.setChecked(false);
    }

    public CartItem getCartItemAt(int position) {
        return getItem(position);
    }

    class CartItemHolder extends RecyclerView.ViewHolder {
        private TextView tvItemName;
        private TextView tvItemPrice;
        private TextView tvQuantity;
        private TextView tvItemTotal;
        private CheckBox cbItemCheck;

        public CartItemHolder(final View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.itemName);
            tvItemPrice = itemView.findViewById(R.id.itemPrice);
            tvQuantity = itemView.findViewById(R.id.quantity);
            tvItemTotal = itemView.findViewById(R.id.itemTotal);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getBindingAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION)
                        listener.OnCartItemClick(getItem(position));
                }
            });
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            });
            cbItemCheck = itemView.findViewById(R.id.itemCheck);
            cbItemCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    toggleChecked(itemView, isChecked);
                }
            });
        }

        private void toggleChecked(final View itemView, boolean isChecked) {
            final Animation fadeOut = new AlphaAnimation(1.0f, 0.5f);
            fadeOut.setDuration(250);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    itemView.setAlpha(0.5f);
                    CartFragment.getInstance().checkItem(getItem(getAdapterPosition()));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            final Animation fadeIn = new AlphaAnimation(0.5f, 1.0f);
            fadeIn.setDuration(250);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    itemView.setAlpha(1.0f);
                    CartFragment.getInstance().uncheckItem(getItem(getAdapterPosition()));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if (isChecked) {
                itemView.startAnimation(fadeOut);
            }
            else {
                itemView.startAnimation(fadeIn);
            }
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public interface OnCartItemClickListener {
        void OnCartItemClick(CartItem cartItem);
    }

    public void setOnCartItemClickListener(OnCartItemClickListener listener) {
        this.listener = listener;
    }

}
