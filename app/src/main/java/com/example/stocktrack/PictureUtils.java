package com.example.stocktrack;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        // Step 1: Decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Step 2: Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, destWidth, destHeight);

        // Step 3: Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap decodedBitmap = BitmapFactory.decodeFile(path, options);

        if (decodedBitmap == null) {
            return null; // Could not decode the file
        }

        // Step 4: Rotate the bitmap if necessary based on EXIF data
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return rotateImage(decodedBitmap, orientation);
        } catch (IOException e) {
            e.printStackTrace();
            return decodedBitmap; // Return the un-rotated bitmap if EXIF fails
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (reqHeight == 0 || reqWidth == 0) {
            return inSampleSize;
        }

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImage(Bitmap source, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return source; // No rotation needed
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        if (rotatedBitmap != source) {
            source.recycle();
        }
        return rotatedBitmap;
    }
}
