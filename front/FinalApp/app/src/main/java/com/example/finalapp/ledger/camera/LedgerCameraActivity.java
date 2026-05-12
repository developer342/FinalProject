package com.example.finalapp.ledger.camera;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.example.finalapp.R;
import com.example.finalapp.ledger.parse.ui.OcrInputActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LedgerCameraActivity extends AppCompatActivity {

    private Button btnCapture;
    private ProgressBar progress;
    private TextView tvStatus;

    private File currentPhotoFile;
    private Uri currentPhotoUri;

    // 권한 요청
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else toast("카메라 권한이 필요합니다.");
            });

    // 카메라 결과
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && currentPhotoUri != null) {
                    runLoading(true, "이미지 처리 중...");
                    try {
                        Bitmap sampled = decodeSampledBitmapFromUri(currentPhotoUri, 2200);
                        Bitmap rotated = rotateBitmapIfNeeded(sampled, currentPhotoFile.getAbsolutePath());

                        // ★ OCR 친화 전처리(그레이스케일 + 대비/밝기 + 간단 이진화)
                        Bitmap preprocessed = enhanceForOcr(rotated);
                        if (rotated != preprocessed) rotated.recycle();

                        File compressed = saveCompressed(preprocessed, currentPhotoFile);
                        preprocessed.recycle();

                        Uri finalUri = Uri.fromFile(compressed);
                        runLoading(false, "완료");
                        launchOcr(finalUri);
                    } catch (Exception e) {
                        runLoading(false, "이미지 처리 실패");
                        toast("이미지 처리 실패");
                    }
                } else {
                    runLoading(false, "촬영 취소");
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_camera);

        btnCapture = findViewById(R.id.btnCapture);
        progress   = findViewById(R.id.progress);
        tvStatus   = findViewById(R.id.tvStatus);

        btnCapture.setOnClickListener(v -> ensurePermissionAndCapture());
    }

    /** 권한 확인 후 촬영 시작 */
    private void ensurePermissionAndCapture() {
        if (Build.VERSION.SDK_INT >= 23) {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera();
        }
    }

    /** 카메라 인텐트 실행(EXTRA_OUTPUT) */
    private void startCamera() {
        try {
            currentPhotoFile = createTempImageFile();
            // ★ 매니페스트와 동일해야 함: ${applicationId}.fileprovider.ledger
            currentPhotoUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider.ledger", currentPhotoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            runLoading(true, "카메라 실행 중...");
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            runLoading(false, "파일 생성 실패");
            toast("파일 생성 실패");
        }
    }

    /** 임시 파일 생성(cache/images) */
    private File createTempImageFile() throws IOException {
        File dir = new File(getCacheDir(), "images");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("cache/images 생성 실패");
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return new File(dir, "IMG_" + ts + ".jpg");
    }

    /** 다운샘플 디코딩 */
    private Bitmap decodeSampledBitmapFromUri(Uri uri, int maxLongSide) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(is, null, bounds);
        }
        int inSample = 1;
        int longSide = Math.max(bounds.outWidth, bounds.outHeight);
        while (longSide / inSample > maxLongSide) inSample *= 2;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = Math.max(1, inSample);
        try (InputStream is2 = getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(is2, null, opts);
        }
    }

    /** EXIF 회전 보정 */
    private Bitmap rotateBitmapIfNeeded(Bitmap src, String absolutePath) throws IOException {
        ExifInterface exif = new ExifInterface(absolutePath);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int degree = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) degree = 90;
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) degree = 180;
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) degree = 270;
        if (degree == 0) return src;

        Matrix m = new Matrix();
        m.postRotate(degree);
        Bitmap rotated = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
        src.recycle();
        return rotated;
    }

    /** OCR 전처리: 그레이스케일 → 대비/밝기 보정 → 간단 이진화 */
    private Bitmap enhanceForOcr(Bitmap src) {
        // 1) 그레이스케일 + 대비/밝기(ColorMatrix)
        float contrast = 1.25f; // 1.0=그대로, ↑ 더 또렷
        float brightness = 10f; // -255~+255
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0); // gray
        ColorMatrix c2 = new ColorMatrix(new float[]{
                contrast, 0, 0, 0, brightness,
                0, contrast, 0, 0, brightness,
                0, 0, contrast, 0, brightness,
                0, 0, 0, 1, 0
        });
        cm.postConcat(c2);

        Bitmap step1 = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(step1);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, p);

        // 2) 간단한 전역 임계값 이진화(가벼운 버전)
        // 평균 밝기 구하고 threshold 적용
        int w = step1.getWidth(), h = step1.getHeight();
        int[] pixels = new int[w * h];
        step1.getPixels(pixels, 0, w, 0, 0, w, h);

        long sum = 0;
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
            int y = (r * 299 + g * 587 + b * 114) / 1000; // luma
            sum += y;
        }
        int avg = (int)(sum / pixels.length);
        int th = Math.max(100, Math.min(180, avg + 10)); // 너무 과도해지지 않게 클램프

        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
            int y = (r * 299 + g * 587 + b * 114) / 1000;
            int v = (y >= th) ? 0xFFFFFFFF : 0xFF000000; // 흑/백
            pixels[i] = v;
        }
        Bitmap bin = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bin.setPixels(pixels, 0, w, 0, 0, w, h);

        step1.recycle();
        return bin;
    }

    /** JPEG 재압축(품질 85) */
    private File saveCompressed(Bitmap bmp, File target) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(target, false)) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
        }
        return target;
    }

    /** OCR 화면으로 이동 */
    private void launchOcr(Uri imageUri) {
        Intent i = new Intent(this, OcrInputActivity.class);
        i.putExtra(OcrInputActivity.EXTRA_IMAGE_URI, imageUri.toString());
        startActivity(i);
    }

    /** 로딩/상태 표시 */
    private void runLoading(boolean loading, String status) {
        btnCapture.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        tvStatus.setText(status);
    }

    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}