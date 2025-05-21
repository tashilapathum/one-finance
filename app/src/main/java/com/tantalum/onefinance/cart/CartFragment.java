package com.tantalum.onefinance.cart;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialSharedAxis;
import com.tantalum.onefinance.Amount;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.pro.UpgradeHandler;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private TextView tvTotal;
    private double total;
    private String currency;
    public static final String TAG = "CartFragment";
    private CartViewModel cartViewModel;
    private ImageButton imDeleteCart;
    private RecyclerView recyclerView;
    private LinearLayout cart_instructions;
    private CartAdapter cartAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        instance = this;
        view = inflater.inflate(R.layout.frag_cart, container, false);
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");

        cartFAB = view.findViewById(R.id.cartFAB);
        cartFAB.setOnClickListener(view -> addToList());
        cart_instructions = view.findViewById(R.id.cart_instructions);
        tvItemCount = view.findViewById(R.id.itemCount);
        tvTotal = view.findViewById(R.id.cartTotal);
        imDeleteCart = view.findViewById(R.id.cart_delete);
        imDeleteCart.setOnClickListener(view -> clearCart());
        total = Double.parseDouble(sharedPref.getString("cartTotal", "0"));

        recyclerView = view.findViewById(R.id.cart_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        cartAdapter = new CartAdapter();
        recyclerView.setAdapter(cartAdapter);

        cartViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(CartViewModel.class);
        cartViewModel.getAllCartItems().observe(getActivity(), cartItems -> {
            cartAdapter.submitList(cartItems);
            showItemCount(cartItems.size());
            toggleInsVisibility(cartItems.size());
            new Handler(Looper.getMainLooper()).post(() -> recyclerView.smoothScrollToPosition(0));
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
                        .setAction(R.string.undo, view -> cartAdapter.notifyDataSetChanged())
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


        cartAdapter.setOnCartItemClickListener(cartItem -> {
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
            total = total + Double.parseDouble(itemPrice.replace(",", ".")) * quantity;
            showTotal(total);
            sharedPref.edit().putString("cartTotal", String.valueOf(total)).apply();
        }
    }

    public void updateItem(int dbID, String itemName, String oldItemPrice, String newItemPrice, int oldQuantity, int newQuantity, boolean isChecked) {
        String itemTotal = getItemTotal(newItemPrice, newQuantity);
        CartItem cartItem = new CartItem(itemName, newItemPrice, newQuantity, itemTotal, isChecked);
        cartItem.setId(dbID);
        cartViewModel.update(cartItem);

        double oldPrice = Double.parseDouble(oldItemPrice.replace(",", "."));
        double newPrice = Double.parseDouble(newItemPrice.replace(",", "."));
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

    public void toggleChecked(CartItem cartItem, boolean isChecked) {
        cartItem.setChecked(isChecked);
        cartViewModel.update(cartItem);
    }

    public void clearCart() {
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.confirm)
                .setMessage(R.string.sure_delete_cart)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    cartViewModel.deleteAllCartItems();
                    showTotal(0);
                    total = 0;
                    sharedPref.edit().putString("cartTotal", Amount.zero()).apply();
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
            totalStr = Amount.zero();
        tvTotal.setText(currency + totalStr);
    }

    private void loadItemCountAndTotal() {
        String total = sharedPref.getString("cartTotal", "");
        if (!total.isEmpty())
            showTotal(Double.parseDouble(total));
        else
            tvTotal.setText(currency + Amount.zero());
    }

    private String getItemTotal(String itemPrice, int intQuantity) {
        double price = Double.parseDouble(itemPrice);
        double itemTotal = price * (double) intQuantity;
        DecimalFormat df = new DecimalFormat("#.00");
        String itemTotalStr = df.format(itemTotal);
        if (itemPrice.equals(Amount.zero()))
            itemTotalStr = Amount.zero();
        return currency + itemTotalStr;
    }

    private void toggleInsVisibility(int itemCount) {
        cart_instructions.setAlpha(0.5f);
        if (itemCount > 0)
            cart_instructions.setVisibility(View.GONE);
        else
            cart_instructions.setVisibility(View.VISIBLE);
    }

}
