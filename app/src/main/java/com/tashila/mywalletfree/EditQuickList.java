package com.tashila.mywalletfree;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AlertDialog;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EditQuickList extends AppCompatActivity {
    SharedPreferences sharedPref;
    public static final String TAG = "EditQuickList";
    boolean MyWalletPro;
    int maxNoOfItems;

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

        //load list
        loadItems();
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
            int currentNoOfItems = ((ViewGroup)findViewById(R.id.qlContainer)).getChildCount();
            if (currentNoOfItems < maxNoOfItems)
                dialogAddQuickItem.show(getSupportFragmentManager(), "add quick item");
            else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.pro_feature)
                        .setMessage(R.string.add_more_quick_items)
                        .setIcon(android.R.drawable.ic_dialog_info)
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

    public void addItem(String item, String price, boolean fromOnCreate) {
        //save
        if (!fromOnCreate) {
            String fullQuickListStr = sharedPref.getString("fullQuickListStr", null);
            fullQuickListStr = fullQuickListStr + item + "~~~" + price + "~~~";
            if (fullQuickListStr.contains("null"))
                fullQuickListStr = fullQuickListStr.replace("null", "");
            sharedPref.edit().putString("fullQuickListStr", fullQuickListStr).apply();
        }

        //show
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout qlContainer = findViewById(R.id.qlContainer);
        final View sampleQuickItem = inflater.inflate(R.layout.sample_quicklist_item, null);
        TextView tvItem = sampleQuickItem.findViewById(R.id.itemName);
        TextView tvPrice = sampleQuickItem.findViewById(R.id.itemPrice);
        ImageView ivRemove = sampleQuickItem.findViewById(R.id.itemRemove);
        tvItem.setText(item);
        tvPrice.setText(price);
        ivRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(sampleQuickItem);
            }
        });
        qlContainer.addView(sampleQuickItem);
        qlContainer.invalidate();

        //to notify wallet screen
        sharedPref.edit().putBoolean("quickItemsChanged", true).apply();
    }

    public void removeItem(final View quickItem) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.delete_item_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TextView tvItemName = quickItem.findViewById(R.id.itemName);
                        TextView tvItemPrice = quickItem.findViewById(R.id.itemPrice);
                        String itemName = tvItemName.getText().toString();
                        String itemPrice = tvItemPrice.getText().toString();
                        String removingPart = itemName + "~~~" + itemPrice + "~~~";
                        String fullString = sharedPref.getString("fullQuickListStr", null);
                        fullString = fullString.replace(removingPart, "");
                        sharedPref.edit().putString("fullQuickListStr", fullString).apply();
                        quickItem.setVisibility(View.GONE);
                        sharedPref.edit().putBoolean("quickItemsChanged", true).apply();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void loadItems() {
        String fullQuickListStr = sharedPref.getString("fullQuickListStr", null);
        if (fullQuickListStr != null) {
            String[] fullQuickList = fullQuickListStr.split("~~~");
            for (int i = 0; i < fullQuickList.length; i = i + 2) {
                if (!fullQuickList[i].isEmpty())
                    addItem(fullQuickList[i], fullQuickList[i + 1], true);
            }
        }
    }
}




















