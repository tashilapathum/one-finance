package com.tashila.mywalletfree;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class EditQuickList extends AppCompatActivity {
    SharedPreferences sharedPref;
    public static final String TAG = "EditQuickList";
    boolean MyWalletPro;
    int maxNoOfItems;
    private QuickListViewModel quickListViewModel;
    private QuickListAdapter quickListAdapter;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar toolbar;
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        //for pro
        MyWalletPro = sharedPref.getBoolean("MyWalletPro", false);
        maxNoOfItems = 5;

        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_quicklist);
            View layout = findViewById(R.id.rootLayout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
            toolbar = findViewById(R.id.toolbar);
            toolbar.setBackground(getDrawable(R.color.colorToolbarDark));
        } else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_quicklist);
            toolbar = findViewById(R.id.toolbar);
        }

        //toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            }
        });

        //Button button;
        FloatingActionButton actionButton = findViewById(R.id.actionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickFAB();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        quickListAdapter = new QuickListAdapter(this);
        recyclerView.setLayoutAnimation(new AnimationHandler().getSlideUpController());
        recyclerView.setAdapter(quickListAdapter);
        quickListViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(QuickListViewModel.class);
        quickListViewModel.getQuickItemsLiveData().observe(this, new Observer<List<QuickItem>>() {
            @Override
            public void onChanged(List<QuickItem> quickItems) {
                quickListAdapter.submitList(quickItems);
                if (quickItems.size() == 0)
                    showInstructions(true);
                else
                    showInstructions(false);
            }
        });
    }

    @Override //so the language change works with dark mode
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    private void onClickFAB() {
        final DialogAddQuickItem dialogAddQuickItem = new DialogAddQuickItem();
        if (MyWalletPro)
            dialogAddQuickItem.show(getSupportFragmentManager(), "add quick item");
        else {
            int currentNoOfItems = quickListAdapter.getItemCount();
            if (currentNoOfItems < maxNoOfItems)
                dialogAddQuickItem.show(getSupportFragmentManager(), "add quick item");
            else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.pro_feature)
                        .setMessage(R.string.add_more_quick_items)
                        .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(EditQuickList.this, UpgradeToPro.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).show();
            }
        }
    }

    public void addItemNEW(QuickItem quickItem) {
        quickListViewModel.insert(quickItem);
        sharedPref.edit().putBoolean("quickItemsChanged", true).apply();
    }

    public void editItem(QuickItem quickItem) {
        DialogAddQuickItem dialogAddQuickItem = new DialogAddQuickItem();
        Bundle bundle = new Bundle();
        bundle.putInt("itemID", quickItem.getId());
        bundle.putString("itemName", quickItem.getItemName());
        bundle.putString("itemPrice", quickItem.getItemPrice());
        dialogAddQuickItem.setArguments(bundle);
        dialogAddQuickItem.show(getSupportFragmentManager(), "edit quick item");
    }

    public void saveItem(QuickItem quickItem) {
        quickListViewModel.update(quickItem);
        sharedPref.edit().putBoolean("quickItemsChanged", true).apply();
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
    }

    public void removeItemNEW(final QuickItem quickItem) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.delete_item_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        quickListViewModel.delete(quickItem);
                        sharedPref.edit().putBoolean("quickItemsChanged", true).apply();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showInstructions(boolean show) {
        LinearLayout instructions = findViewById(R.id.instructions);
        if (show) {
            instructions.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        else {
            instructions.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}




















