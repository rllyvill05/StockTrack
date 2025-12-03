package com.example.stocktrack;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stocktrack.database.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public class ListViewModel extends ViewModel {

    private MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private List<Product> allProducts = new ArrayList<>();
    private ProductRepository repository;

    public ListViewModel() {
        repository = ProductRepository.getInstance();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public void loadProducts() {
        allProducts = repository.getAllProducts();
        products.setValue(allProducts);
    }

    public void addProduct(Product product) {
        repository.addProduct(product);
        loadProducts(); // Reload to update the list
    }

    public void updateProduct(Product product) {
        repository.updateProduct(product);
        loadProducts(); // Reload to update the list
    }

    public void deleteProduct(String productId) {
        repository.deleteProduct(productId);
        loadProducts(); // Reload to update the list
    }

    // In ListViewModel.java

    public void filterProducts(String query) {
        // --- THIS IS THE FIX ---
        // Always get the latest data from the repository before filtering.
        // This ensures that any products added/updated elsewhere are included.
        allProducts = repository.getAllProducts();

        if (query == null || query.trim().isEmpty()) {
            products.setValue(allProducts);
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<Product> filtered = new ArrayList<>();
        for (Product product : allProducts) {
            if ((product.getName() != null && product.getName().toLowerCase().contains(lowerQuery)) ||
                    (product.getCategory() != null && product.getCategory().toLowerCase().contains(lowerQuery)) ||
                    (product.getBarcode() != null && product.getBarcode().toLowerCase().contains(lowerQuery))) {
                filtered.add(product);
            }
        }
        products.setValue(filtered);
    }

}

