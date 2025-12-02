package com.example.stocktrack;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;

public class EditFragment extends Fragment {

    private static final String ARG_PRODUCT = "product";
    private static final int REQUEST_IMAGE_PICK = 1003;

    private Product product;
    private LinearLayout layoutImagePicker;
    private ImageView ivImagePlaceholder;
    private Button btnSaveProduct;
    private Uri selectedImageUri;
    
    // EditText fields
    private EditText etName;
    private EditText etCategory;
    private EditText etBarcode;
    private EditText etQuantity;
    private EditText etUnitType;
    private EditText etBuyingPrice;
    private EditText etSellingPrice;
    
    private ListViewModel viewModel;

    public static EditFragment newInstance(Product product) {
        EditFragment fragment = new EditFragment();
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
        return inflater.inflate(R.layout.edit_item_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel - use activity scope so it's shared with ListFragment
        viewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);

        // Initialize views
        layoutImagePicker = view.findViewById(R.id.layout_image_picker);
        ivImagePlaceholder = view.findViewById(R.id.iv_image_placeholder);
        btnSaveProduct = view.findViewById(R.id.btn_add_product);
        
        // Update title for edit mode
//        android.widget.TextView tvTitle = view.findViewById(R.id.tv_edit_title);
//        if (tvTitle != null) {
//            tvTitle.setText("Edit Product");
//        }
        
        // Initialize EditText fields
        etName = view.findViewById(R.id.tv_label_name);
        etCategory = view.findViewById(R.id.tv_label_category);
        etBarcode = view.findViewById(R.id.tv_label_barcode);
        etQuantity = view.findViewById(R.id.tv_label_quantity);
        etUnitType = view.findViewById(R.id.tv_label_unit_type);
        etBuyingPrice = view.findViewById(R.id.tv_label_buying_price);
        etSellingPrice = view.findViewById(R.id.tv_label_selling_price);

        // Update button text for edit mode
        if (btnSaveProduct != null) {
            btnSaveProduct.setText(R.string.save);
        }

        // Load product data if editing
        if (product != null) {
            loadProductData();
        }

        // Set up image picker click listener
        if (layoutImagePicker != null) {
            layoutImagePicker.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_PICK);
                }
            });
        }

        // Set up save button click listener
        if (btnSaveProduct != null) {
            btnSaveProduct.setOnClickListener(v -> {
                saveProduct();
            });
        }
    }

    private void loadProductData() {
        if (product == null) return;

        // Populate EditText fields with product data
        if (etName != null) {
            etName.setText(product.getName() != null ? product.getName() : "");
        }
        if (etCategory != null) {
            etCategory.setText(product.getCategory() != null ? product.getCategory() : "");
        }
        if (etBarcode != null) {
            etBarcode.setText(product.getBarcode() != null ? product.getBarcode() : "");
        }
        if (etQuantity != null) {
            etQuantity.setText(String.valueOf(product.getQuantity()));
        }
        if (etUnitType != null) {
            etUnitType.setText(product.getUnitType() != null ? product.getUnitType() : "");
        }
        if (etBuyingPrice != null) {
            etBuyingPrice.setText(String.valueOf(product.getBuyingPrice()));
        }
        if (etSellingPrice != null) {
            etSellingPrice.setText(String.valueOf(product.getSellingPrice()));
        }

        // Load image if available
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(product.getImagePath());
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), imageUri);
                if (ivImagePlaceholder != null) {
                    ivImagePlaceholder.setImageBitmap(bitmap);
                }
                selectedImageUri = imageUri;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProduct() {
        // Validate required fields
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError("Product name is required");
            etName.requestFocus();
            return;
        }

        // Get other fields
        String category = etCategory.getText().toString().trim();
        String barcode = etBarcode.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String unitType = etUnitType.getText().toString().trim();
        String buyingPriceStr = etBuyingPrice.getText().toString().trim();
        String sellingPriceStr = etSellingPrice.getText().toString().trim();

        // Parse numeric values
        int quantity = 0;
        try {
            if (!TextUtils.isEmpty(quantityStr)) {
                quantity = Integer.parseInt(quantityStr);
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Invalid quantity");
            etQuantity.requestFocus();
            return;
        }

        double buyingPrice = 0.0;
        try {
            if (!TextUtils.isEmpty(buyingPriceStr)) {
                buyingPrice = Double.parseDouble(buyingPriceStr);
            }
        } catch (NumberFormatException e) {
            etBuyingPrice.setError("Invalid price");
            etBuyingPrice.requestFocus();
            return;
        }

        double sellingPrice = 0.0;
        try {
            if (!TextUtils.isEmpty(sellingPriceStr)) {
                sellingPrice = Double.parseDouble(sellingPriceStr);
            }
        } catch (NumberFormatException e) {
            etSellingPrice.setError("Invalid price");
            etSellingPrice.requestFocus();
            return;
        }

        // Update product
        product.setName(name);
        product.setCategory(TextUtils.isEmpty(category) ? "Uncategorized" : category);
        product.setBarcode(TextUtils.isEmpty(barcode) ? null : barcode);
        product.setQuantity(quantity);
        product.setUnitType(TextUtils.isEmpty(unitType) ? "pcs" : unitType);
        product.setBuyingPrice(buyingPrice);
        product.setSellingPrice(sellingPrice);
        
        if (selectedImageUri != null) {
            product.setImagePath(selectedImageUri.toString());
        }

        // Update product using ViewModel
        viewModel.updateProduct(product);

        Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate back
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), selectedImageUri);
                if (ivImagePlaceholder != null) {
                    ivImagePlaceholder.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

