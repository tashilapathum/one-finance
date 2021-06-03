package com.tantalum.financejournal.cart;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart_items_table")
public class CartItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String itemName;
    private String itemPrice;
    private int quantity;
    private String itemTotal;
    private boolean isChecked;

    public CartItem(String itemName, String itemPrice, int quantity, String itemTotal, boolean isChecked) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.itemTotal = itemTotal;
        this.isChecked = isChecked;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getItemTotal() {
        return itemTotal;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
