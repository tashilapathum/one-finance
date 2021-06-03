package com.tantalum.financejournal.quicklist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quick_items_table")
public class QuickItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String itemName;
    private String itemPrice;

    public QuickItem(String itemName, String itemPrice) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
    }

    public int getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public void setId(int id) {
        this.id = id;
    }
}
