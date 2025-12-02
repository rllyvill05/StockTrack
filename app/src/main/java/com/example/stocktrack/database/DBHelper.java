package com.example.stocktrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.stocktrack.Product;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "stocktracker.db";
    private static final int DB_VERSION = 1;

    private Context mContext;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uuidString TEXT," +
                "name TEXT," +
                "category TEXT," +
                "barcode TEXT," +
                "quantity INTEGER," +
                "unit_type TEXT," +
                "buying_price REAL," +
                "selling_price REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS products");
        onCreate(db);
    }

    // INSERT
    public void insertProduct(Product p) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("uuidString", p.getId().toString());
        cv.put("name", p.getName());
        cv.put("category", p.getCategory());
        cv.put("barcode", p.getBarcode());
        cv.put("quantity", p.getQuantity());
        cv.put("unit_type", p.getUnitType());
        cv.put("buying_price", p.getBuyingPrice());
        cv.put("selling_price", p.getSellingPrice());

        db.insert("products", null, cv);
        db.close();
    }

    // GET ALL
    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM products", null);

        if (c.moveToFirst()) {
            do {
                Product p = new Product(
                        c.getString(2),  // name
                        c.getString(3),  // category
                        c.getString(4),  // barcode
                        c.getInt(5),     // quantity
                        c.getString(6),  // unit_type
                        c.getDouble(7),  // buying_price
                        c.getDouble(8)   // selling_price
                );
                // Set the UUID from the database
                p.setId(c.getString(1));
                list.add(p);
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return list;
    }

    // GET SINGLE PRODUCT BY ID
    public Product getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Product product = null;

        Cursor c = db.query("products",
                null, // columns - null means all columns
                "id=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (c.moveToFirst()) {
            product = new Product(
                    c.getString(2),  // name
                    c.getString(3),  // category
                    c.getString(4),  // barcode
                    c.getInt(5),     // quantity
                    c.getString(6),  // unit_type
                    c.getDouble(7),  // buying_price
                    c.getDouble(8)   // selling_price
            );
            // Set the UUID from the database
            product.setId(c.getString(1));
        }

        c.close();
        db.close();
        return product;
    }

    public File getPhotoFile(Product product) {
        File filesDir = mContext.getFilesDir();
        if (filesDir == null) {
            return null;
        }
        return new File(filesDir, product.getId().toString() + ".jpg");
    }

    // UPDATE
    public void updateProduct(Product p) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("uuidString", p.getId().toString());
        cv.put("name", p.getName());
        cv.put("category", p.getCategory());
        cv.put("barcode", p.getBarcode());
        cv.put("quantity", p.getQuantity());
        cv.put("unit_type", p.getUnitType());
        cv.put("buying_price", p.getBuyingPrice());
        cv.put("selling_price", p.getSellingPrice());

        db.update("products", cv, "uuidString=?", new String[]{p.getId().toString()});
        db.close();
    }

    // DELETE
    public void deleteProduct(String uuid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("products", "uuidString=?", new String[]{uuid});
        db.close();
    }
}