package com.example.stocktrack;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class EditFragment extends Fragment {

    private static final String ARG_PRODUCT = "product";
    private static final int REQUEST_IMAGE_PICK = 1003;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    private Product product;
    private LinearLayout layoutImagePicker;
    private ImageView ivImagePlaceholder;
    private Button btnSaveProduct, btnDeleteProduct;
    private Uri selectedImageUri;
    private File photoFile;
    private Product currentProduct;
    
    // EditText fields
    private EditText etName, etCategory, etBarcode, etQuantity, etStoredLoc, etBuyingPrice, etSellingPrice;
    private ListViewModel viewModel;
    private GmsBarcodeScanner scanner;
    private ImageButton btnScanBarcode;

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

        // Initialize the product with a UUID
        currentProduct = new Product();
        currentProduct.setId(UUID.randomUUID().toString());

        // Setup gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        displaySelectedImage(selectedImageUri);
                    }
                }
        );

        // Setup camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && photoFile != null) {
                        selectedImageUri = Uri.fromFile(photoFile);
                        displaySelectedImage(selectedImageUri);
                    }
                }
        );

        // Setup camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

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
        btnDeleteProduct = view.findViewById(R.id.btn_delete_product);

        // Initialize EditText fields
        etName = view.findViewById(R.id.tv_label_name);
        etCategory = view.findViewById(R.id.tv_label_category);
        etBarcode = view.findViewById(R.id.tv_label_barcode);
        etQuantity = view.findViewById(R.id.tv_label_quantity);
        etStoredLoc = view.findViewById(R.id.tv_stored_location);
        etBuyingPrice = view.findViewById(R.id.tv_label_buying_price);
        etSellingPrice = view.findViewById(R.id.tv_label_selling_price);

        btnScanBarcode = view.findViewById(R.id.iv_barcode);
        btnScanBarcode.setOnClickListener(v -> {
            scanner.startScan()
                    .addOnSuccessListener(barcode -> {
                        String scannedValue = barcode.getRawValue();
                        etBarcode.setText(scannedValue);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EditFragment", "Barcode scan failed: " + e.getMessage());
                        Toast.makeText(getContext(), "Scan failed. Check Google Play Services.", Toast.LENGTH_SHORT).show();
                    });
        });

        // Update button text for edit mode
        if (btnSaveProduct != null) {
            btnSaveProduct.setText(R.string.save);
        }
        if (btnDeleteProduct != null) {
            btnDeleteProduct.setText(R.string.delete);
        }
        // Load product data
        loadProductData();

        // Load product data if editing
        if (product != null) {
            loadProductData();
        }

        // Set up save button click listener
        if (btnSaveProduct != null) {
            btnSaveProduct.setOnClickListener(v -> {
                saveProduct();
            });
        }
        if (btnDeleteProduct != null) {
            btnDeleteProduct.setOnClickListener(v -> {
                viewModel.deleteProduct(product.getId());
                Toast.makeText(getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed();
            });
        }

        layoutImagePicker.setOnClickListener(v -> showImagePickerDialog());

//        // Set up image picker click listener
        if (layoutImagePicker != null) {
            layoutImagePicker.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_PICK);
                }
            });
        }
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Image Source")
                .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndOpen();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        photoFile = getPhotoFile();
        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            cameraLauncher.launch(photoUri);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File getPhotoFile() {
        File filesDir = requireContext().getFilesDir();
        return new File(filesDir, currentProduct.getId().toString() + ".jpg");
    }

    private void displaySelectedImage(Uri imageUri) {
        try {
            Bitmap bitmap;
            if (imageUri.getScheme().equals("file")) {
                // Image from camera
                bitmap = BitmapFactory.decodeFile(imageUri.getPath());
            } else {
                // Image from gallery
                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Save gallery image to app storage
                saveImageToFile(bitmap);
            }

            // Display the image
            ivImagePlaceholder.setImageBitmap(bitmap);
            ivImagePlaceholder.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToFile(Bitmap bitmap) {
        photoFile = getPhotoFile();
        try (FileOutputStream out = new FileOutputStream(photoFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (IOException e) {
            e.printStackTrace();
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
        if (etStoredLoc != null) {
            etStoredLoc.setText(product.getStoredLoc() != null ? product.getStoredLoc() : "");
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
                Log.d("IMAGE_PATH", product.getImagePath());
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
        String unitType = etStoredLoc.getText().toString().trim();
        String buyingPriceStr = etBuyingPrice.getText().toString().trim();
        String sellingPriceStr = etSellingPrice.getText().toString().trim();
        String imagePath = product.getImagePath();


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
        product.setStoredLoc(TextUtils.isEmpty(unitType) ? "pcs" : unitType);
        product.setBuyingPrice(buyingPrice);
        product.setSellingPrice(sellingPrice);
        product.setImagePath(imagePath);
        
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
