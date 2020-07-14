package com.tashila.mywalletfree;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CartFragment extends Fragment {
    View view;
    SharedPreferences sharedPref;
    private static CartFragment instance;
    FloatingActionButton cartFAB;
    private TextView tvItemCount;
    private int itemCount;
    private TextView tvTotal;
    private double total;
    private String currency;
    public static final String TAG = "CartFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        instance = this;
        view = inflater.inflate(R.layout.frag_cart, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            Essentials essentials = new Essentials(getActivity());
            essentials.invertDrawable(view.findViewById(R.id.cart_delete));
        }

        cartFAB = view.findViewById(R.id.cartFAB);
        cartFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createList();
            }
        });
        tvItemCount = view.findViewById(R.id.itemCount);
        itemCount = 0;
        tvTotal = view.findViewById(R.id.cartTotal);
        total = 0;

        return view;
    }

    public static CartFragment getInstance() {
        return instance;
    }

    public void setListName(String listName) {
        RelativeLayout title_layout = view.findViewById(R.id.title_layout);
        TextView tvListName = view.findViewById(R.id.tvListName);
        tvListName.setText(listName);
        title_layout.setVisibility(View.VISIBLE);
    }

    private void createList() {
        cartFAB.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_add_to_cart));
        cartFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToList();
            }
        });

        DialogNewShoppingList dialogNewShoppingList = new DialogNewShoppingList();
        dialogNewShoppingList.show(getActivity().getSupportFragmentManager(), "new shopping list dialog");
    }

    private void addToList() {
        DialogNewCartItem dialogNewCartItem = new DialogNewCartItem();
        dialogNewCartItem.show(getActivity().getSupportFragmentManager(), "add new item dialog");
    }

    public void addItem(String itemName, String itemPrice, int quantity) {
        //show
        currency = sharedPref.getString("currency", "");
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout cartListContainer = view.findViewById(R.id.items_container);
        final View sampleProductItem = inflater.inflate(R.layout.sample_product_item, null);
        TextView tvItem = sampleProductItem.findViewById(R.id.itemName);
        TextView tvPrice = sampleProductItem.findViewById(R.id.itemPrice);
        ImageView ivRemove = sampleProductItem.findViewById(R.id.itemRemove);
        tvItem.setText(itemName);
        if (!itemPrice.isEmpty()) {
            tvPrice.setText(currency + itemPrice + " x " + quantity);

            //update item count
            itemCount = itemCount + quantity;
            showItemCount(itemCount);

            //update total
            view.findViewById(R.id.totalCard).setVisibility(View.VISIBLE);
            total = total + Double.parseDouble(itemPrice) * quantity;
            showTotal(total);
        }
        else {
            tvPrice.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            tvItem.setLayoutParams(params);
        }

        ivRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(view);
            }
        });
        cartListContainer.addView(sampleProductItem);
        cartListContainer.invalidate();
    }

    private void removeItem(View view) {
        ViewGroup item = (ViewGroup) view.getParent();
        item.setVisibility(View.GONE);

        //update item count
        TextView tvPricexQuantity = item.findViewById(R.id.itemPrice);
        if (!tvPricexQuantity.getText().toString().isEmpty()) {
            int removingQuantity = Integer.parseInt(tvPricexQuantity.getText().toString().split("x")[1].trim());
            Log.i(TAG, "removingQuantity: " + removingQuantity);
            itemCount = itemCount - removingQuantity;
            showItemCount(itemCount);

            //update total
            double minusAmount = Double.parseDouble(tvPricexQuantity.getText().toString().split("x")[0].replace(currency, "").trim());
            Log.i(TAG, "minusAmount: " + minusAmount);
            total = total - minusAmount * removingQuantity;
            showTotal(total);
        }
    }

    private void showItemCount(int itemCount) {
        String language = sharedPref.getString("language", "english");
        if (language.equalsIgnoreCase("සිංහල"))
            tvItemCount.setText("මිල සහිත අයිතම: " + itemCount);
        else
            tvItemCount.setText(itemCount + " priced items");
    }

    private void showTotal(double total) {
        DecimalFormat df = new DecimalFormat("#.00");
        String totalStr = df.format(total);
        tvTotal.setText(currency + totalStr);
    }
}
