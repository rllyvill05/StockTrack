package com.example.stocktrack;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
    private TextView productName;
    private TextView productCategory;
    private TextView productBarcode;
    private TextView productQuantity;
    private TextView productUnitType;
    private TextView productBuyingPrice;
    private TextView productSellingPrice;
    private ImageView productImage;
    private File ivPhotoFile;

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

        productImage = v.findViewById(R.id.iv_product_image);
        
        DBHelper dbHelper = new DBHelper(requireContext());
        ivPhotoFile = dbHelper.getPhotoFile(product);

        productImage.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        updatePhotoView();
                        productImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        productImage.setOnClickListener(view -> {
            if (ivPhotoFile != null && ivPhotoFile.exists()) {
                Log.d("PhotoDialog", "Photo file exists: " + ivPhotoFile);
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
        productName = view.findViewById(R.id.tv_product_name);
        productCategory = view.findViewById(R.id.tv_product_category);
        productBarcode = view.findViewById(R.id.tv_product_barcode);
        productQuantity = view.findViewById(R.id.tv_product_quantity);
        productUnitType = view.findViewById(R.id.tv_product_unit_type);
        productBuyingPrice = view.findViewById(R.id.tv_product_buying_price);
        productSellingPrice = view.findViewById(R.id.tv_product_selling_price);
        Button btnEditProduct = view.findViewById(R.id.btn_edit_product);

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
            productImage.setImageResource(android.R.drawable.ic_menu_gallery);
            productImage.setContentDescription(
                    getString(R.string.no_image_found_description));
            return;
        }

        int width = productImage.getWidth();
        int height = productImage.getHeight();

        if (width == 0 || height == 0) {
            // Dimensions not ready yet
            return;
        }

        Bitmap bitmap = PictureUtils.getScaledBitmap(
        ivPhotoFile.getPath(), width, height);
        productImage.setImageBitmap(bitmap);
        productImage.setContentDescription(
                getString(R.string.no_image_found_description));
    }

    private void displayProductData() {
        if (product == null) return;

        productName.setText(product.getName() != null ? product.getName() : "N/A");
        productCategory.setText(product.getCategory() != null ? product.getCategory() : "N/A");
        productBarcode.setText(product.getBarcode() != null ? product.getBarcode() : "N/A");
        productQuantity.setText(String.valueOf(product.getQuantity()));
        productUnitType.setText(product.getStoredLoc() != null ? product.getStoredLoc() : "pcs");
        productBuyingPrice.setText(String.format("₱%.2f", product.getBuyingPrice()));
        productSellingPrice.setText(String.format("₱%.2f", product.getSellingPrice()));
    }
}
