package com.example.appdraw.community;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import java.io.OutputStream;

public class FullScreenImageActivity extends AppCompatActivity {
    private String imageUrl;
    private Bitmap currentBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        imageUrl = getIntent().getStringExtra("IMAGE_URL");
        ImageView ivFullscreen = findViewById(R.id.iv_fullscreen);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("data:image")) {
                byte[] decodedBytes = Base64.decode(imageUrl.split(",")[1], Base64.DEFAULT);
                currentBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                Glide.with(this).load(currentBitmap).into(ivFullscreen);
            } else {
                Glide.with(this).load(imageUrl).into(ivFullscreen);
            }
        }

        findViewById(R.id.btn_close_fullscreen).setOnClickListener(v -> finish());
        findViewById(R.id.btn_download).setOnClickListener(v -> downloadImage());
    }

    private void downloadImage() {
        ImageView ivFullscreen = findViewById(R.id.iv_fullscreen);
        Bitmap bitmapToSave = currentBitmap;

        if (bitmapToSave == null && ivFullscreen != null
                && ivFullscreen.getDrawable() instanceof android.graphics.drawable.BitmapDrawable) {
            bitmapToSave = ((android.graphics.drawable.BitmapDrawable) ivFullscreen.getDrawable()).getBitmap();
        }

        if (bitmapToSave == null) {
            Toast.makeText(this, "Đang tải ảnh, vui lòng thử lại sau giây lát...", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String filename = "AppDraw_" + System.currentTimeMillis() + ".jpg";
            OutputStream fos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = getContentResolver().openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString();
                java.io.File image = new java.io.File(imagesDir, filename);
                fos = new java.io.FileOutputStream(image);
            }
            bitmapToSave.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            if (fos != null)
                fos.close();
            Toast.makeText(this, "Đã lưu ảnh vào thư viện", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
