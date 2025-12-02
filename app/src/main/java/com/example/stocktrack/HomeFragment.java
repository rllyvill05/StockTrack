package com.example.stocktrack;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private TextView tvItemsCount;
    private TextView tvLowStockCount;
    private TextView tvSoldOutCount;
    private LinearLayout layoutAddCard;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel - use activity scope to access shared repository
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Initialize views
        tvItemsCount = view.findViewById(R.id.tv_items_count);
        tvLowStockCount = view.findViewById(R.id.tv_low_stock_count);
        tvSoldOutCount = view.findViewById(R.id.tv_sold_out_count);
//        layoutAddCard = view.findViewById(R.id.layout_add_card);

        // Set up click listener for add card (only if layoutAddCard exists)
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
        viewModel.getItemsCount().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "Items count updated: " + count);
            tvItemsCount.setText(String.valueOf(count));
        });

        viewModel.getLowStockCount().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "Low stock count updated: " + count);
            tvLowStockCount.setText(String.valueOf(count));
        });

        viewModel.getSoldOutCount().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "Sold out count updated: " + count);
            tvSoldOutCount.setText(String.valueOf(count));
        });

        // Load data
        viewModel.loadSummaryData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (viewModel != null) {
            viewModel.loadSummaryData();
        }
    }
}