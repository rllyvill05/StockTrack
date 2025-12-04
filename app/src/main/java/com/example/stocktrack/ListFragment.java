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
    import android.view.animation.AnimationUtils;
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
    import java.util.List;

    public class ListFragment extends Fragment {

        private RecyclerView recyclerView;
        private ProductAdapter adapter;
        private ListViewModel viewModel;
        private TextView tvEmptyState;
        private SearchView searchView;
        private GmsBarcodeScanner scanner;


        private static final String TAG = "HomeFragment";
        private TextView tvItemsCount;
        private TextView tvLowStockCount;
        private TextView tvSoldOutCount;
        private LinearLayout layoutAddCard;
        private HomeViewModel homeviewModel;


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

            // Initialize ViewModel - use activity scope so it's shared with AddFragment
            viewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
            homeviewModel = new ViewModelProvider(this).get(HomeViewModel.class);


            tvItemsCount = view.findViewById(R.id.tv_items_count);
            tvLowStockCount = view.findViewById(R.id.tv_low_stock_count);
            tvSoldOutCount = view.findViewById(R.id.tv_sold_out_count);

            if (layoutAddCard != null) {
                layoutAddCard.setOnClickListener(v -> {
                    v.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    // Navigate to add fragment - handled by MainActivity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToAddFragment();
                    }
                });
            }

            // Observe data from ViewModel
            homeviewModel.getItemsCount().observe(getViewLifecycleOwner(), count -> {
                Log.d(TAG, "Items count updated: " + count);
                tvItemsCount.setText(String.valueOf(count));
            });

            homeviewModel.getLowStockCount().observe(getViewLifecycleOwner(), count -> {
                Log.d(TAG, "Low stock count updated: " + count);
                tvLowStockCount.setText(String.valueOf(count));
            });

            homeviewModel.getSoldOutCount().observe(getViewLifecycleOwner(), count -> {
                Log.d(TAG, "Sold out count updated: " + count);
                tvSoldOutCount.setText(String.valueOf(count));
            });

            homeviewModel.loadSummaryData();


            // Initialize views
            recyclerView = view.findViewById(R.id.recycler_view_products);
            tvEmptyState = view.findViewById(R.id.tv_empty_state);

            // Set up RecyclerView
            adapter = new ProductAdapter(new ArrayList<>(), product -> {
                // Handle item click - navigate to product detail fragment
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToProductDetail(product);
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);

            // Observe products from ViewModel
            viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
                adapter.updateProducts(products);
                updateEmptyState(products.isEmpty());
            });

            // Load products
            viewModel.loadProducts();
        }

        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.toolbar_menu, menu);

            // Set up search functionality
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
                            viewModel.filterProducts(newText);
                            return true;
                        }
                    });

                    // Handle the closing of the search view to restore the full list
                    searchView.setOnCloseListener(() -> {
                        viewModel.loadProducts(); // Or viewModel.filterProducts("");
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


                            viewModel.filterProducts(scannedCode);

                            if (searchView != null) {
                                searchView.setIconified(false);

                                searchView.setQuery(scannedCode, false);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // user canceled / error
                            // You might want to log this or show a toast
                        });

                return true;
            } else if (item.getItemId() == R.id.action_search) {
                // The SearchView is handled by the system, which expands it automatically.
                // You can return true to indicate you've handled the click.
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
            // Refresh data when fragment becomes visible
            if (viewModel != null) {
                viewModel.loadProducts();
            }
            if (homeviewModel != null) {
                viewModel.loadProducts();
            }
        }


    }

