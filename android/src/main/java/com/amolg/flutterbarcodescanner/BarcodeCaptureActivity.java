package com.amolg.flutterbarcodescanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BarcodeCaptureActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_HANDLE_CAMERA_PERM = 2;
    public static final String BarcodeObject = "Barcode";

    private PreviewView mPreview;
    private ImageView imgViewBarcodeCaptureUseFlash;
    private ImageView imgViewSwitchCamera;

    public static int SCAN_MODE = SCAN_MODE_ENUM.QR.ordinal();

    public enum SCAN_MODE_ENUM {
        QR,
        BARCODE,
        DEFAULT
    }

    enum USE_FLASH {
        ON,
        OFF
    }

    private int flashStatus = USE_FLASH.OFF.ordinal();
    private int cameraFacing = CameraSelector.LENS_FACING_BACK;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            setContentView(R.layout.barcode_capture);

            String buttonText = "";
            try {
                buttonText = getIntent().getStringExtra("cancelButtonText");
            } catch (Exception e) {
                buttonText = "Cancel";
            }

            Button btnBarcodeCaptureCancel = findViewById(R.id.btnBarcodeCaptureCancel);
            btnBarcodeCaptureCancel.setText(buttonText != null ? buttonText : "Cancel");
            btnBarcodeCaptureCancel.setOnClickListener(this);

            imgViewBarcodeCaptureUseFlash = findViewById(R.id.imgViewBarcodeCaptureUseFlash);
            imgViewBarcodeCaptureUseFlash.setOnClickListener(this);
            imgViewBarcodeCaptureUseFlash.setVisibility(FlutterBarcodeScannerPlugin.isShowFlashIcon ? View.VISIBLE : View.GONE);

            imgViewSwitchCamera = findViewById(R.id.imgViewSwitchCamera);
            imgViewSwitchCamera.setOnClickListener(this);

            mPreview = findViewById(R.id.preview);

            cameraExecutor = Executors.newSingleThreadExecutor();

            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().build();
            if (SCAN_MODE == SCAN_MODE_ENUM.QR.ordinal()) {
                options = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build();
            } else if (SCAN_MODE == SCAN_MODE_ENUM.BARCODE.ordinal()) {
                options = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build();
            }
            barcodeScanner = BarcodeScanning.getClient(options);

            int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (rc == PackageManager.PERMISSION_GRANTED) {
                startCameraSource();
            } else {
                requestCameraPermission();
            }

        } catch (Exception e) {
            Log.e("BarcodeCaptureActivity", "onCreate: ", e);
        }
    }

    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCameraSource();
            return;
        }

        DialogInterface.OnClickListener listener = (dialog, id) -> finish();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Allow permissions")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    private void startCameraSource() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("BarcodeCaptureActivity", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            return;
        }
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(mPreview.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraFacing)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            @SuppressLint("UnsafeOptInUsageError")
            android.media.Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                barcodeScanner.process(image)
                        .addOnSuccessListener(barcodes -> {
                            for (Barcode barcode : barcodes) {
                                onBarcodeDetected(barcode);
                                break;
                            }
                        })
                        .addOnFailureListener(e -> Log.e("BarcodeCaptureActivity", "Barcode scanning failed", e))
                        .addOnCompleteListener(task -> imageProxy.close());
            } else {
                imageProxy.close();
            }
        });

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            if (camera.getCameraInfo().hasFlashUnit()) {
                camera.getCameraControl().enableTorch(flashStatus == USE_FLASH.ON.ordinal());
            }
        } catch (Exception e) {
            Log.e("BarcodeCaptureActivity", "Use case binding failed", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.imgViewBarcodeCaptureUseFlash) {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                if (flashStatus == USE_FLASH.OFF.ordinal()) {
                    flashStatus = USE_FLASH.ON.ordinal();
                    imgViewBarcodeCaptureUseFlash.setImageResource(R.drawable.ic_barcode_flash_on);
                    camera.getCameraControl().enableTorch(true);
                } else {
                    flashStatus = USE_FLASH.OFF.ordinal();
                    imgViewBarcodeCaptureUseFlash.setImageResource(R.drawable.ic_barcode_flash_off);
                    camera.getCameraControl().enableTorch(false);
                }
            } else {
                Toast.makeText(this, "Unable to access flashlight", Toast.LENGTH_SHORT).show();
            }
        } else if (i == R.id.btnBarcodeCaptureCancel) {
            FlutterBarcodeScannerPlugin.onBarcodeScanReceiver("-1");
            finish();
        } else if (i == R.id.imgViewSwitchCamera) {
            cameraFacing = (cameraFacing == CameraSelector.LENS_FACING_FRONT) ?
                    CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
            bindCameraUseCases();
        }
    }

    private void onBarcodeDetected(Barcode barcode) {
        if (barcode != null && barcode.getRawValue() != null && !barcode.getRawValue().isEmpty()) {
            if (FlutterBarcodeScannerPlugin.isContinuousScan) {
                FlutterBarcodeScannerPlugin.onBarcodeScanReceiver(barcode.getRawValue());
            } else {
                Intent data = new Intent();
                data.putExtra(BarcodeObject, barcode.getRawValue());
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();
            }
        }
    }
}