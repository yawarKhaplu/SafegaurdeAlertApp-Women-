package com.darkness.sparkwomen;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureImageService extends Service {

    private static final String TAG = "CaptureImageService";
    private final IBinder binder = new LocalBinder();

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Check if there is a capture request
        if (intent != null && intent.hasExtra(MediaStore.EXTRA_OUTPUT)) {
            Uri imageUri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
            captureImage(imageUri);
        } else {
            Log.d(TAG, "No capture request received");
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);



            cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (Exception e) {
                Log.e(TAG, "Error binding camera use cases", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureImage(Uri imageUri) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        try {
            startActivity(takePictureIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting camera activity", e);
        }
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // ... (unchanged)

        try {
            cameraProvider.unbindAll();
//            cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

            // ... (unchanged)
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera use cases", e);
        }
    }

    // ... (unchanged)

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    public class LocalBinder extends Binder {
        CaptureImageService getService() {
            return CaptureImageService.this;
        }
    }
}
