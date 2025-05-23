package com.tantalum.onefinance.quicklist;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tantalum.onefinance.Constants;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.pro.UpgradeHandler;
import com.tantalum.onefinance.pro.UpgradeToProActivity;

import java.util.List;

public class QuickListActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    public static final String TAG = "QuickListActivity";
    private QuickListViewModel quickListViewModel;
    private QuickListAdapter quickListAdapter;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        String theme = sharedPref.getString("theme", "light");
        if (!theme.equalsIgnoreCase("dark")) {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quicklist);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Button button;
        FloatingActionButton actionButton = findViewById(R.id.actionButton);
        actionButton.setOnClickListener(view -> onClickFAB());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        quickListAdapter = new QuickListAdapter(this);
        recyclerView.setAdapter(quickListAdapter);
        quickListViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(QuickListViewModel.class);
        quickListViewModel.getQuickItemsLiveData().observe(this, quickItems -> {
            quickListAdapter.submitList(quickItems);
            showInstructions(quickItems.isEmpty());
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
        if (UpgradeHandler.isProActive(this))
            dialogAddQuickItem.show(getSupportFragmentManager(), "add quick item");
        else {
            int currentNoOfItems = quickListAdapter.getItemCount();
            if (currentNoOfItems < Constants.FREE_QUICKLIST_LIMIT)
                dialogAddQuickItem.show(getSupportFragmentManager(), "add quick item");
            else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.reached_free_limit)
                        .setMessage(R.string.add_more_quick_items)
                        .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(QuickListActivity.this, UpgradeToProActivity.class);
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
        bundle.putString("itemCategory", quickItem.getCategory());
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




















