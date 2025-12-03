package com.example.stocktrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.stocktrack.Product;

import java.io.File;
import java.util.ArrayList;

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
                "stored_loc TEXT," +
                "buying_price REAL," +
                "selling_price REAL," +
                "img_path TEXT" +")");
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
        cv.put("stored_loc", p.getStoredLoc());
        cv.put("buying_price", p.getBuyingPrice());
        cv.put("selling_price", p.getSellingPrice());
        cv.put("img_path", p.getImagePath());


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
                Product p = new Product();

                p.setId(c.getString(c.getColumnIndexOrThrow("uuidString")));
                p.setName(c.getString(c.getColumnIndexOrThrow("name")));
                p.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
                p.setBarcode(c.getString(c.getColumnIndexOrThrow("barcode")));
                p.setQuantity(c.getInt(c.getColumnIndexOrThrow("quantity")));
                p.setStoredLoc(c.getString(c.getColumnIndexOrThrow("stored_loc"))); // Correctly set stored_loc
                p.setBuyingPrice(c.getDouble(c.getColumnIndexOrThrow("buying_price")));
                p.setSellingPrice(c.getDouble(c.getColumnIndexOrThrow("selling_price")));
                p.setImagePath(c.getString(c.getColumnIndexOrThrow("img_path"))); // Correctly set img_path
                // Set the UUID from the database
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
            product = new Product();
            product.setId(c.getString(c.getColumnIndexOrThrow("uuidString")));
            product.setName(c.getString(c.getColumnIndexOrThrow("name")));
            product.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
            product.setBarcode(c.getString(c.getColumnIndexOrThrow("barcode")));
            product.setQuantity(c.getInt(c.getColumnIndexOrThrow("quantity")));
            product.setStoredLoc(c.getString(c.getColumnIndexOrThrow("stored_loc")));
            product.setBuyingPrice(c.getDouble(c.getColumnIndexOrThrow("buying_price")));
            product.setSellingPrice(c.getDouble(c.getColumnIndexOrThrow("selling_price")));
            product.setImagePath(c.getString(c.getColumnIndexOrThrow("img_path")));
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
        cv.put("stored_loc", p.getStoredLoc());
        cv.put("buying_price", p.getBuyingPrice());
        cv.put("selling_price", p.getSellingPrice());
        cv.put("img_path", p.getImagePath());

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