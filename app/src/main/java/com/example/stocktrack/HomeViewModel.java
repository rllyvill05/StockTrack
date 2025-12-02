package com.example.stocktrack;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stocktrack.database.ProductRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private MutableLiveData<Integer> itemsCount = new MutableLiveData<>(0);
    private MutableLiveData<Integer> lowStockCount = new MutableLiveData<>(0);
    private MutableLiveData<Integer> soldOutCount = new MutableLiveData<>(0);
    private ProductRepository repository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = ProductRepository.getInstance(application);
    }

    public LiveData<Integer> getItemsCount() {
        return itemsCount;
    }

    public LiveData<Integer> getLowStockCount() {
        return lowStockCount;
    }

    public LiveData<Integer> getSoldOutCount() {
        return soldOutCount;
    }

    public void loadSummaryData() {
        List<Product> products = repository.getAllProducts();
        int totalItems = products.size();
        int lowStock = 0;
        int soldOut = 0;

        for (Product product : products) {
            if (product.isSoldOut()) {
                soldOut++;
            } else if (product.isLowStock(5)) { // Threshold of 5 for low stock
                lowStock++;
            }
        }

        itemsCount.setValue(totalItems);
        lowStockCount.setValue(lowStock);
        soldOutCount.setValue(soldOut);
    }
}