package com.tantalum.financejournal.quicklist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quick_items_table")
public class QuickItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String itemName;
    private String itemPrice;
    private String category;

    public QuickItem(String itemName, String itemPrice, String category) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.category = category;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
