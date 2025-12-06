package com.example.stocktrack;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;

public class PhotoFragment extends DialogFragment {
    private static final String ARG_PHOTO_PATH = "photo_path";

    public static PhotoFragment newInstance(File photoFile) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PHOTO_PATH, photoFile);

        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_photo, null);

        ImageView photoView = v.findViewById(R.id.zoomed_photo);

        File photoFile = (File) getArguments().getSerializable(ARG_PHOTO_PATH);
        if (photoFile != null && photoFile.exists()) {
            Activity activity = requireActivity();
            Point size = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(size);

            Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), size.x, size.y);
            photoView.setImageBitmap(bitmap);
        }

        return new AlertDialog.Builder(requireActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
