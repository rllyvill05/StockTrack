package com.example.stocktrack;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.stocktrack.database.DBHelper;

import java.io.File;

public class ProductDetailFragment extends Fragment {

    private static final String ARG_PRODUCT = "product";

    private Product product;
    private TextView tvProductName;
    private TextView tvProductCategory;
    private TextView tvProductBarcode;
    private TextView tvProductQuantity;
    private TextView tvProductUnitType;
    private TextView tvProductBuyingPrice;
    private TextView tvProductSellingPrice;
    private ImageView ivProductImage;
    private File ivPhotoFile;
    private Button btnEditProduct;

    private static final int REQUEST_PHOTO = 0;

    public static ProductDetailFragment newInstance(Product product) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = getArguments().getParcelable(ARG_PRODUCT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_product_detail, container, false);

        ivProductImage = v.findViewById(R.id.iv_product_image);
        
        DBHelper dbHelper = new DBHelper(requireContext());
        ivPhotoFile = dbHelper.getPhotoFile(product);

        ivProductImage.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        updatePhotoView();
                        ivProductImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        ivProductImage.setOnClickListener(view -> {
            if (ivPhotoFile != null && ivPhotoFile.exists()) {
                PhotoFragment.newInstance(ivPhotoFile)
                        .show(getParentFragmentManager(), "PhotoDialog");
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(requireActivity(),
                    requireContext().getPackageName() + ".fileprovider",
                    ivPhotoFile);
            requireActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvProductName = view.findViewById(R.id.tv_product_name);
        tvProductCategory = view.findViewById(R.id.tv_product_category);
        tvProductBarcode = view.findViewById(R.id.tv_product_barcode);
        tvProductQuantity = view.findViewById(R.id.tv_product_quantity);
        tvProductUnitType = view.findViewById(R.id.tv_product_unit_type);
        tvProductBuyingPrice = view.findViewById(R.id.tv_product_buying_price);
        tvProductSellingPrice = view.findViewById(R.id.tv_product_selling_price);
        btnEditProduct = view.findViewById(R.id.btn_edit_product);

        // Load and display product data
        if (product != null) {
            displayProductData();
        }

        // Set up edit button click listener
        btnEditProduct.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToEditFragment(product);
            }
        });
    }

    private void updatePhotoView() {
        if (ivPhotoFile == null || !ivPhotoFile.exists()) {
            ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
            ivProductImage.setContentDescription(
                    getString(R.string.no_image_found_description));
            return;
        }

        int width = ivProductImage.getWidth();
        int height = ivProductImage.getHeight();

        if (width == 0 || height == 0) {
            // Dimensions not ready yet
            return;
        }

        Bitmap bitmap = PictureUtils.getScaledBitmap(
                ivPhotoFile.getPath(), width, height);
        ivProductImage.setImageBitmap(bitmap);
        ivProductImage.setContentDescription(
                getString(R.string.no_image_found_description));
    }

    private void displayProductData() {
        if (product == null) return;

        // Set text values
        tvProductName.setText(product.getName() != null ? product.getName() : "N/A");
        tvProductCategory.setText(product.getCategory() != null ? product.getCategory() : "N/A");
        tvProductBarcode.setText(product.getBarcode() != null ? product.getBarcode() : "N/A");
        tvProductQuantity.setText(String.valueOf(product.getQuantity()));
        tvProductUnitType.setText(product.getStoredLoc() != null ? product.getStoredLoc() : "pcs");
        tvProductBuyingPrice.setText(String.format("₱%.2f", product.getBuyingPrice()));
        tvProductSellingPrice.setText(String.format("₱%.2f", product.getSellingPrice()));

        // The image is loaded via updatePhotoView() in onCreateView.
    }
}
