package com.tashila.mywalletfree;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart_items_table")
public class CartItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String itemName;
    private String itemPrice;
    private int quantity;

    public CartItem(String itemName, String itemPrice, int quantity) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
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
}
