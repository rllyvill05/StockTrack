package com.example.stocktrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ScanFragment extends Fragment {

    private TextView btnLight;
    private TextView btnScanImage;
    private TextView btnHistory;
    private FrameLayout cameraPreviewContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scan_product_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        btnLight = view.findViewById(R.id.btn_light);
        btnScanImage = view.findViewById(R.id.btn_scan_image);
        btnHistory = view.findViewById(R.id.btn_history);
        cameraPreviewContainer = view.findViewById(R.id.camera_preview_container);

        // Set up click listeners
        btnLight.setOnClickListener(v -> {
            // Toggle flashlight - implement camera functionality
            Toast.makeText(getContext(), "Light toggle", Toast.LENGTH_SHORT).show();
        });

        btnScanImage.setOnClickListener(v -> {
            // Use implicit intent to scan image from gallery
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(intent, 1002);
            }
        });

        btnHistory.setOnClickListener(v -> {
            // Show scan history
            Toast.makeText(getContext(), "Scan history", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initialize camera preview when fragment becomes visible
        // This would typically involve setting up CameraX or Camera2 API
    }

    @Override
    public void onPause() {
        super.onPause();
        // Release camera resources when fragment is paused
    }
}

