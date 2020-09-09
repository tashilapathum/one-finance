package com.tashila.mywalletfree;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
    private int itemCount;
    private TextView tvTotal;
    private double total;
    private String currency;
    public static final String TAG = "CartFragment";
    private CartViewModel cartViewModel;
    private ImageButton imDeleteCart;
    private RecyclerView recyclerView;
    private LinearLayout cart_instructions;
    private String theme;
    private CartAdapter cartAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        instance = this;
        view = inflater.inflate(R.layout.frag_cart, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        theme = sharedPref.getString("theme", "light");
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
        cart_instructions = view.findViewById(R.id.cart_instructions);
        tvItemCount = view.findViewById(R.id.itemCount);
        tvTotal = view.findViewById(R.id.cartTotal);
        imDeleteCart = view.findViewById(R.id.cart_delete);
        imDeleteCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCart();
            }
        });
        itemCount = sharedPref.getInt("cartItemCount", 0);
        total = Double.parseDouble(sharedPref.getString("cartTotal", "0"));

        recyclerView = view.findViewById(R.id.cart_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        cartAdapter = new CartAdapter();
        recyclerView.setAdapter(cartAdapter);

        cartViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(CartViewModel.class);
        cartViewModel.getAllCartItems().observe(getActivity(), new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {
                cartAdapter.submitList(cartItems);
                showItemCount(cartItems.size());
                toggleInsVisibility(cartItems.size());
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                final CartItem cartItem = cartAdapter.getCartItemAt(viewHolder.getAdapterPosition());
                Snackbar snackbar = Snackbar.make(recyclerView, R.string.deleted, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cartAdapter.notifyDataSetChanged();
                            }
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                    removeItem(cartItem);
                                }
                            }
                        });
                snackbar.show();
            }
        }).attachToRecyclerView(recyclerView);


        cartAdapter.setOnCartItemClickListener(new CartAdapter.OnCartItemClickListener() {
            @Override
            public void OnCartItemClick(CartItem cartItem) {
                int dbID = cartItem.getId();
                String itemName = cartItem.getItemName();
                String oldItemPrice = cartItem.getItemPrice();
                int oldQuantity = cartItem.getQuantity();
                boolean isChecked = cartItem.isChecked();
                Bundle bundle = new Bundle();
                bundle.putInt("cart dbID", dbID);
                bundle.putString("cart itemName", itemName);
                bundle.putString("cart itemPrice", oldItemPrice);
                bundle.putInt("cart quantity", oldQuantity);
                bundle.putBoolean("cart isChecked", isChecked);

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
        String itemTotal = getItemTotal(itemPrice, quantity);
        CartItem cartItem = new CartItem(itemName, itemPrice, quantity, itemTotal, false);
        cartViewModel.insert(cartItem);

        //update total
        if (!itemPrice.isEmpty()) {
            total = total + Double.parseDouble(itemPrice) * quantity;
            showTotal(total);
            sharedPref.edit().putString("cartTotal", String.valueOf(total)).apply();
        }
    }

    public void updateItem(int dbID, String itemName, String oldItemPrice, String newItemPrice, int oldQuantity, int newQuantity, boolean isChecked) {
        String itemTotal = getItemTotal(newItemPrice, newQuantity);
        CartItem cartItem = new CartItem(itemName, newItemPrice, newQuantity, itemTotal, isChecked);
        cartItem.setId(dbID);
        cartViewModel.update(cartItem);

        double oldPrice = Double.parseDouble(oldItemPrice);
        double newPrice = Double.parseDouble(newItemPrice);
        total = total - oldPrice * oldQuantity + newPrice * newQuantity;
        showTotal(total);
        sharedPref.edit().putString("cartTotal", String.valueOf(total)).apply();
    }

    private void removeItem(CartItem cartItem) {
        cartViewModel.delete(cartItem);
        int quantity = cartItem.getQuantity();

        //update total
        String itemPrice = cartItem.getItemPrice();
        if (!itemPrice.isEmpty()) {
            total = total - Double.parseDouble(itemPrice) * quantity;
            showTotal(total);
            sharedPref.edit().putString("cartTotal", String.valueOf(total)).apply();
        }
    }

    public void checkItem(CartItem cartItem) {
        cartItem.setChecked(true);
        cartViewModel.update(cartItem);
    }

    public void uncheckItem(CartItem cartItem) {
        cartItem.setChecked(false);
        cartViewModel.update(cartItem);
    }

    public void clearCart() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm)
                .setMessage(R.string.sure_delete_cart)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cartViewModel.deleteAllCartItems();
                        showTotal(0);
                        total = 0;
                        sharedPref.edit().putString("cartTotal", "0.00").apply();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showItemCount(int itemCount) {
        String language = sharedPref.getString("language", "english");
        if (language.equalsIgnoreCase("සිංහල"))
            tvItemCount.setText("අයිතම ගණන: " + itemCount);
        else
            tvItemCount.setText(itemCount + " items");
    }

    private void showTotal(double total) {
        DecimalFormat df = new DecimalFormat("#.00");
        String totalStr = df.format(total);
        if (total == 0)
            totalStr = "0.00";
        tvTotal.setText(currency + totalStr);
    }

    private void loadItemCountAndTotal() {
        String total = sharedPref.getString("cartTotal", "");
        if (!total.isEmpty())
            showTotal(Double.parseDouble(total));
        else
            tvTotal.setText(currency + "0.00");
    }

    private String getItemTotal(String itemPrice, int intQuantity) {
        double price = Double.parseDouble(itemPrice);
        double itemTotal = price * (double) intQuantity;
        DecimalFormat df = new DecimalFormat("#.00");
        String itemTotalStr = df.format(itemTotal);
        if (itemPrice.equals("0.00"))
            itemTotalStr = "0.00";
        return currency + itemTotalStr;
    }

    private void toggleInsVisibility(int itemCount) {
        if (theme.equalsIgnoreCase("dark"))
            new Essentials(getActivity()).invertDrawable(cart_instructions.findViewById(R.id.cartIcon));
        cart_instructions.setAlpha(0.5f);
        if (itemCount > 0)
            cart_instructions.setVisibility(View.GONE);
        else
            cart_instructions.setVisibility(View.VISIBLE);
    }
}
