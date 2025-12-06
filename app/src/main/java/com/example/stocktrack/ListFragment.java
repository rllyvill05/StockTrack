package com.example.stocktrack;

import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private ListViewModel listViewModel;
    private HomeViewModel homeViewModel;
    private TextView tvEmptyState;
    private SearchView searchView;
    private GmsBarcodeScanner scanner;


    private static final String TAG = "HomeFragment";
    private TextView itemsCount, lowStockCount, soldOutCount;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E
                )
                .enableAutoZoom()
                .build();
        scanner = GmsBarcodeScanning.getClient(requireActivity(), options);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listViewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);


        itemsCount = view.findViewById(R.id.tv_items_count);
        lowStockCount = view.findViewById(R.id.tv_low_stock_count);
        soldOutCount = view.findViewById(R.id.tv_sold_out_count);


        LinearLayout itemsCountBox = view.findViewById(R.id.layout_items_count);
        LinearLayout lowStockCountBox = view.findViewById(R.id.layout_low_stock_count);
        LinearLayout soldOutCountBox = view.findViewById(R.id.layout_sold_out_count);

        itemsCountBox.setOnClickListener(v -> listViewModel.loadProducts());
        lowStockCountBox.setOnClickListener(v -> listViewModel.filterLowStockProducts());
        soldOutCountBox.setOnClickListener(v -> listViewModel.filterSoldOutProducts());


        homeViewModel.getItemsCount().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "Items count updated: " + count);
            itemsCount.setText(String.valueOf(count));
        });

        homeViewModel.getLowStockCount().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "Low stock count updated: " + count);
            lowStockCount.setText(String.valueOf(count));
        });

        homeViewModel.getSoldOutCount().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "Sold out count updated: " + count);
            soldOutCount.setText(String.valueOf(count));
        });

        recyclerView = view.findViewById(R.id.recycler_view_products);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        adapter = new ProductAdapter(new ArrayList<>(), new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToProductDetail(product);
                }
            }

            @Override
            public void onEditClick(Product product) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToEditFragment(product);
                }
            }

            @Override
            public void onSetLowStockClick(Product product) {
                product.setQuantity(1);
                listViewModel.updateProduct(product);
                homeViewModel.loadSummaryData();
            }

            @Override
            public void onSetSoldOutClick(Product product) {
                product.setQuantity(0);
                listViewModel.updateProduct(product);
                homeViewModel.loadSummaryData();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        listViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            adapter.updateProducts(products);
            updateEmptyState(products.isEmpty());
        });

        listViewModel.loadProducts();
        homeViewModel.loadSummaryData();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setQueryHint(getString(R.string.search_hint));
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // Hide the keyboard when the user submits the search
                        searchView.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        // Filter products in real-time as the user types
                        listViewModel.filterProducts(newText);
                        return true;
                    }
                });

                // Handle the closing of the search view to restore the full list
                searchView.setOnCloseListener(() -> {
                    listViewModel.loadProducts();
                    return false;
                });
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_scan) {
            scanner.startScan()
                    .addOnSuccessListener(barcode -> {
                        String scannedCode = barcode.getRawValue();
                        Log.d("SCAN", "Scanned: " + scannedCode);


                        listViewModel.filterProducts(scannedCode);

                        if (searchView != null) {
                            searchView.setIconified(false);

                            searchView.setQuery(scannedCode, false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // user canceled / error
                    });

            return true;
        } else if (item.getItemId() == R.id.action_search) {
            // The SearchView is handled by the system, which expands it automatically.
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh summary data when fragment becomes visible. The product list is managed by LiveData
        // and does not need to be manually reloaded here, which preserves the search/filter state.
        if (homeViewModel != null) {
            homeViewModel.loadSummaryData();
        }
    }


}
