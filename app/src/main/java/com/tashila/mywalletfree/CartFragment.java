package com.tashila.mywalletfree;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CartFragment extends Fragment {
    View view;
    SharedPreferences sharedPref;
    private static CartFragment instance;
    FloatingActionButton cartFAB;
    private TextView tvItemCount;
    private int itemCount = 0;
    private TextView tvTotal;
    private double total = 0;
    private String currency;
    public static final String TAG = "CartFragment";
    private CartItemViewModel cartItemViewModel;
    private ImageButton imDeleteCart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        instance = this;
        view = inflater.inflate(R.layout.frag_cart, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String theme = sharedPref.getString("theme", "light");
        currency = sharedPref.getString("currency", "");
        if (theme.equalsIgnoreCase("dark")) {
            Essentials essentials = new Essentials(getActivity());
            essentials.invertDrawable(view.findViewById(R.id.cart_delete));
        }

        cartFAB = view.findViewById(R.id.cartFAB);
        cartFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToList();
            }
        });
        tvItemCount = view.findViewById(R.id.itemCount);
        tvTotal = view.findViewById(R.id.cartTotal);
        imDeleteCart = view.findViewById(R.id.cart_delete);
        imDeleteCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCart();
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.cart_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        final CartItemAdapter adapter = new CartItemAdapter();
        recyclerView.setAdapter(adapter);

        cartItemViewModel = new ViewModelProvider(getActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(CartItemViewModel.class);
        cartItemViewModel.getAllCartItems().observe(getActivity(), new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {
                adapter.submitList(cartItems);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                CartItem cartItem = adapter.getCartItemAt(viewHolder.getAdapterPosition());
                removeItem(cartItem);
            }
        }).attachToRecyclerView(recyclerView);


        adapter.setOnCartItemClickListener(new CartItemAdapter.OnCartItemClickListener() {
            @Override
            public void OnCartItemClick(CartItem cartItem) {
                int dbID = cartItem.getId();
                String itemName = cartItem.getItemName();
                String oldItemPrice = cartItem.getItemPrice();
                int oldQuantity = cartItem.getQuantity();

                Bundle bundle = new Bundle();
                bundle.putInt("cart dbID", dbID);
                bundle.putString("cart itemName", itemName);
                bundle.putString("cart itemPrice", oldItemPrice);
                bundle.putInt("cart quantity", oldQuantity);

                DialogNewCartItem dialogNewCartItem = new DialogNewCartItem();
                dialogNewCartItem.setArguments(bundle);
                dialogNewCartItem.show(getActivity().getSupportFragmentManager(), "edit cart item dialog");
            }
        });

        loadItemCountAndTotal();
        return view;
    }

    public static CartFragment getInstance() {
        return instance;
    }

    private void addToList() {
        DialogNewCartItem dialogNewCartItem = new DialogNewCartItem();
        dialogNewCartItem.show(getActivity().getSupportFragmentManager(), "add new cart item dialog");
    }

    public void addItem(String itemName, String itemPrice, int quantity) {
        CartItem cartItem = new CartItem(itemName, itemPrice, quantity);
        cartItemViewModel.insert(cartItem);

        //update item count and total
        itemCount = itemCount + quantity;
        showItemCount(itemCount);
        if (!itemPrice.isEmpty()) {
            total = total + Double.parseDouble(itemPrice) * quantity;
            showTotal(total);
        }
    }

    public void updateItem(int dbID, String itemName, String oldItemPrice, String newItemPrice, int oldQuantity, int newQuantity) {
        CartItem cartItem = new CartItem(itemName, newItemPrice, newQuantity);
        cartItem.setId(dbID);
        cartItemViewModel.update(cartItem);

        //update item count and total
        if (oldQuantity < newQuantity)
            itemCount = itemCount + (newQuantity - oldQuantity);
        else
            itemCount = itemCount - (oldQuantity - newQuantity);
        showItemCount(itemCount);

        double oldPrice = Double.parseDouble(oldItemPrice);
        double newPrice = Double.parseDouble(newItemPrice);
        total = total - oldPrice * oldQuantity + newPrice * newQuantity;
        showTotal(total);
    }

    private void removeItem(CartItem cartItem) {
        cartItemViewModel.delete(cartItem);
        int quantity = cartItem.getQuantity();

        //update item count and total
        itemCount = itemCount - quantity;
        showItemCount(itemCount);

        String itemPrice = cartItem.getItemPrice();
        if (!itemPrice.isEmpty()) {
            total = total - Double.parseDouble(itemPrice) * quantity;
            showTotal(total);
        }
    }

    public void clearCart() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm)
                .setMessage(R.string.sure_delete_cart)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cartItemViewModel.deleteAllCartItems();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
        showItemCount(0);
        showTotal(0);
    }

    private void showItemCount(int itemCount) {
        String language = sharedPref.getString("language", "english");
        if (language.equalsIgnoreCase("සිංහල"))
            tvItemCount.setText("මිල සහිත අයිතම: " + itemCount);
        else
            tvItemCount.setText(itemCount + " priced items");
        sharedPref.edit().putInt("cartItemCount", itemCount).apply();
    }

    private void showTotal(double total) {
        DecimalFormat df = new DecimalFormat("#.00");
        String totalStr = df.format(total);
        tvTotal.setText(currency + totalStr);
        sharedPref.edit().putString("cartTotal", totalStr).apply();
    }

    private void loadItemCountAndTotal() {
        int itemCount = sharedPref.getInt("cartItemCount", 0);
        showItemCount(itemCount);

        String total = sharedPref.getString("cartTotal", "");
        if (!total.isEmpty())
            showTotal(Double.parseDouble(total));
        else
            tvTotal.setText(currency + "0.00");
    }
}
