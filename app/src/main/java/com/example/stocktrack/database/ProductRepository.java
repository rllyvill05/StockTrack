package com.example.stocktrack.database;

import android.content.Context;
import com.example.stocktrack.Product;
import java.util.List;
import java.util.UUID;

public class ProductRepository {
    private static ProductRepository instance;
    private final DBHelper dbHelper;

    private ProductRepository(Context context) {
        // Use application context to avoid memory leaks
        Context appContext = context.getApplicationContext();
        dbHelper = new DBHelper(appContext);
    }

    public static synchronized ProductRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ProductRepository(context);
        }
        return instance;
    }

    public static synchronized ProductRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ProductRepository not initialized. Call getInstance(Context) first.");
        }
        return instance;
    }

    public void addProduct(Product product) {
        // Generate UUID if not already set
        if (product.getId() == null) {
            product.setId(UUID.randomUUID().toString());
        }
        dbHelper.insertProduct(product);
    }

    public void updateProduct(Product product) {
        dbHelper.updateProduct(product);
    }

    public void deleteProduct(String productId) {
        dbHelper.deleteProduct(productId);
    }

    public List<Product> getAllProducts() {
        return dbHelper.getAllProducts();
    }
}