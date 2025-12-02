package com.example.stocktrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toolbar_menu, menu);


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

//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.toolbar_menu, menu);
//
//        // Set up search functionality
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        if (searchItem != null) {
//            searchView = (SearchView) searchItem.getActionView();
//            if (searchView != null) {
//                searchView.setQueryHint(getString(R.string.search_hint));
//                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextSubmit(String query) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onQueryTextChange(String newText) {
//                        // Filter products in real-time
//                        viewModel.filterProducts(newText);
//                        return true;
//                    }
//                });
//            }
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            // Handle share action
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
    }


}

