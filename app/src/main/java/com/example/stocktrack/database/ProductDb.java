package com.example.stocktrack.database;

import com.example.stocktrack.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ProductDb {
    private static ProductDb instance;
    private List<Product> products;
    private AtomicLong nextId;

    private ProductDb() {
        products = new ArrayList<>();
        nextId = new AtomicLong(1);
    }

    public static synchronized ProductDb getInstance() {
        if (instance == null) {
            instance = new ProductDb();
        }
        return instance;
    }

    public void addProduct(Product product) {
        product.setId(nextId.toString());
        products.add(product);
    }

    public void updateProduct(Product product) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == product.getId()) {
                products.set(i, product);
                break;
            }
        }
    }

    public void deleteProduct(String productId) {  // Changed from long to int
        products.removeIf(p -> p.getId() == productId);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public Product getProductById(String id) {  // Changed from long to int
        for (Product product : products) {
            if (product.getId() == id) {
                return product;
            }
        }
        return null;
    }
}


