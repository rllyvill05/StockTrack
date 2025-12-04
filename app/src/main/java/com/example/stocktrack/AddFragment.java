    package com.example.stocktrack;

    import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
    import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
    import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
    import com.google.mlkit.vision.barcode.common.Barcode;

    import android.Manifest;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.net.Uri;
    import android.os.Bundle;
    import android.provider.MediaStore;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.Spinner;
    import android.widget.Toast;
    import android.util.Log;

    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AlertDialog;
    import androidx.core.content.ContextCompat;
    import androidx.core.content.FileProvider;
    import androidx.fragment.app.Fragment;

    import com.example.stocktrack.database.ProductRepository;
    import com.google.mlkit.vision.barcode.common.Barcode;
    import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
    import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
    import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

    import java.io.File;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.util.UUID;

    public class AddFragment extends Fragment {

        private EditText etName, etCategory, etBarcode, etQuantity, etStoredLoc, etBuyingPrice, etSellingPrice;
        private String imagePath;
        private LinearLayout layoutImagePicker;
        private ImageView ivImagePlaceholder;
        private Button btnAddProduct;
        private Uri selectedImageUri;
        private File photoFile;
        private Product currentProduct;

        // Activity result launchers
        private ActivityResultLauncher<Intent> galleryLauncher;
        private ActivityResultLauncher<Uri> cameraLauncher;
        private ActivityResultLauncher<String> cameraPermissionLauncher;

        private GmsBarcodeScanner scanner;
        private ImageButton btnScanBarcode;


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

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
            return inflater.inflate(R.layout.add_product_fragment, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Initialize views
            etName = view.findViewById(R.id.tv_label_name);
            etCategory = view.findViewById(R.id.tv_label_category);
            etBarcode = view.findViewById(R.id.tv_label_barcode);
            etQuantity = view.findViewById(R.id.tv_label_quantity);
            etStoredLoc = view.findViewById(R.id.tv_stored_location);
            etBuyingPrice = view.findViewById(R.id.tv_label_buying_price);
            etSellingPrice = view.findViewById(R.id.tv_label_selling_price);
            layoutImagePicker = view.findViewById(R.id.layout_image_picker);
            ivImagePlaceholder = view.findViewById(R.id.iv_image_placeholder);
            btnAddProduct = view.findViewById(R.id.btn_add_product);

            btnScanBarcode = view.findViewById(R.id.iv_barcode);
            btnScanBarcode.setOnClickListener(v -> {


                scanner.startScan()
                        .addOnSuccessListener(barcode -> {
                            Log.d("AddFragment", "Barcode scanned: " + barcode.getRawValue());
                            String scannedValue = barcode.getRawValue();
                            etBarcode.setText(scannedValue);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Scan canceled", Toast.LENGTH_SHORT).show();
                        });
            });

            // Set click listener for image picker
            layoutImagePicker.setOnClickListener(v -> showImagePickerDialog());

            // Set click listener for add button
            btnAddProduct.setOnClickListener(v -> saveProduct());
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

        private void saveProduct() {
            // Validate inputs
            String name = etName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String barcode = etBarcode.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();
            String storedLoc = etStoredLoc.getText().toString().trim();
            String buyingPriceStr = etBuyingPrice.getText().toString().trim();
            String sellingPriceStr = etSellingPrice.getText().toString().trim();
            String imageUriStr = null;
            if (photoFile != null && photoFile.exists()) {
                imageUriStr = photoFile.getAbsolutePath();
            } else if (selectedImageUri != null) { // Fallback, though less likely with current logic
                imageUriStr = selectedImageUri.toString();
            }

            if (name.isEmpty()) {
                etName.setError("Name is required");
                return;
            }

            int quantity = 0;
            if (!quantityStr.isEmpty()) {
                try {
                    quantity = Integer.parseInt(quantityStr);
                } catch (NumberFormatException e) {
                    etQuantity.setError("Invalid quantity");
                    return;
                }
            }

            double buyingPrice = 0.0;
            if (!buyingPriceStr.isEmpty()) {
                try {
                    buyingPrice = Double.parseDouble(buyingPriceStr);
                } catch (NumberFormatException e) {
                    etBuyingPrice.setError("Invalid price");
                    return;
                }
            }

            double sellingPrice = 0.0;
            if (!sellingPriceStr.isEmpty()) {
                try {
                    sellingPrice = Double.parseDouble(sellingPriceStr);
                } catch (NumberFormatException e) {
                    etSellingPrice.setError("Invalid price");
                    return;
                }
            }

            // Create product
            Product product = new Product(name, category, barcode, quantity, storedLoc, buyingPrice, sellingPrice,  imageUriStr);
            product.setId(UUID.randomUUID().toString());
            product.setId(currentProduct.getId()); // Use the UUID we created earlier
            product.setImagePath(imageUriStr);


            // Save to database
            ProductRepository repository = ProductRepository.getInstance(requireContext());
            repository.addProduct(product);

            Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();

            // Clear form and navigate back
            clearForm();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToListFragment();
            }
        }

        private void clearForm() {
            etName.setText("");
            etCategory.setText("");
            etBarcode.setText("");
            etQuantity.setText("");
            etStoredLoc.setText("");
            etBuyingPrice.setText("");
            etSellingPrice.setText("");
            ivImagePlaceholder.setImageResource(android.R.drawable.ic_menu_gallery);
            ivImagePlaceholder.setScaleType(ImageView.ScaleType.FIT_CENTER);
            selectedImageUri = null;
            currentProduct = new Product();
            currentProduct.setId(UUID.randomUUID().toString());
        }
    }