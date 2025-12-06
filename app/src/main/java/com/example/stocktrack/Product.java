package com.example.stocktrack;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String uuidString;
    private String name;
    private String category;
    private String barcode;
    private int quantity;
    private String storedLoc;
    private double buyingPrice;
    private double sellingPrice;
    private String imagePath;

    public Product() {
        this.uuidString = java.util.UUID.randomUUID().toString();
    }

    public Product(String name, String category, String barcode, int quantity, 
                   String storedLoc, double buyingPrice, double sellingPrice, String imagePath) {
        this.uuidString = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.category = category;
        this.barcode = barcode;
        this.quantity = quantity;
        this.storedLoc = storedLoc;
        this.buyingPrice = buyingPrice;
        this.sellingPrice = sellingPrice;
        this.imagePath = imagePath;
    }

    protected Product(Parcel in) {
        uuidString = in.readString();
        name = in.readString();
        category = in.readString();
        barcode = in.readString();
        quantity = in.readInt();
        storedLoc = in.readString();
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

    //ID getter and setter
    public String getId() { return uuidString; }

    public void setId(String uuidString) {
        this.uuidString = uuidString;
    }


    //Name getter and setter
    public String getName() {return name; }

    public void setName(String name) { this.name = name; }

    //Category getter and setter
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    //Barcode getter and setter
    public String getBarcode() {
        return barcode;
    }
    public void setBarcode(String barcode) {
        if (barcode != null) {
            this.barcode = barcode.trim();
        } else {
            this.barcode = null;
        }
    }

    //Quantity getter and setter
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    //Stored Location getter and setter
    public String getStoredLoc() {
        return storedLoc;
    }
    public void setStoredLoc(String storedLoc) {
        this.storedLoc = storedLoc;
    }


    //Buying Price getter and setter
    public double getBuyingPrice() {
        return buyingPrice;
    }
    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }


    //Selling Price getter and setter
    public double getSellingPrice() {
        return sellingPrice;
    }
    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }


    //Image Path getter and setter
    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }


    //Other methods
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
        dest.writeString(storedLoc);
        dest.writeDouble(buyingPrice);
        dest.writeDouble(sellingPrice);
        dest.writeString(imagePath);
    }
}

