package com.example.stocktrack;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String uuidString;
    private String name;
    private String category;
    private String barcode;
    private int quantity;
    private String unitType;
    private double buyingPrice;
    private double sellingPrice;
    private String imagePath;

    public Product() {
    }

    public Product(String name, String category, String barcode, int quantity, 
                   String unitType, double buyingPrice, double sellingPrice) {
        this.name = name;
        this.category = category;
        this.barcode = barcode;
        this.quantity = quantity;
        this.unitType = unitType;
        this.buyingPrice = buyingPrice;
        this.sellingPrice = sellingPrice;
    }

    protected Product(Parcel in) {
        uuidString = in.readString();
        name = in.readString();
        category = in.readString();
        barcode = in.readString();
        quantity = in.readInt();
        unitType = in.readString();
        buyingPrice = in.readDouble();
        sellingPrice = in.readDouble();
        imagePath = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    // Getters and Setters
    public String getId() {
        return uuidString;
    }

    public void setId(String uuidString) {
        this.uuidString = uuidString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public double getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isLowStock(int threshold) {
        return quantity <= threshold;
    }

    public boolean isSoldOut() {
        return quantity == 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuidString);
        dest.writeString(name);
        dest.writeString(category);
        dest.writeString(barcode);
        dest.writeInt(quantity);
        dest.writeString(unitType);
        dest.writeDouble(buyingPrice);
        dest.writeDouble(sellingPrice);
        dest.writeString(imagePath);
    }
}

