package com.darkness.sparkwomen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PictureService extends AppCompatActivity {

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    String myLocation;
    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera); //  file is named "camera.xml"

        cameraExecutor = Executors.newSingleThreadExecutor();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        // Start the camera and capture photo after a short delay
        startCamera();
        startCameraWithDelay();
    }

    private void startCameraWithDelay() {
        // Introduce a delay before capturing the photo
        final long delayMillis = 1000; // 1 seconds
        new android.os.Handler().postDelayed(() -> {
            takePhoto();
            // Schedule the camera to close after 3 seconds
            new android.os.Handler().postDelayed(() -> {
                closeCamera();
                finish();
            }, 3000);
        }, delayMillis);
    }

    private void closeCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            Log.d("Camera", "Camera closed successfully");
            // Optionally, you can finish the activity here or perform other actions.
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // Check if camera is available
                if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    PreviewView previewView = findViewById(R.id.previewView);

                    // Set up the preview
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());

                    // Set up the image capture
                    imageCapture = new ImageCapture.Builder().build();

                    // Set up the camera
                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                    // Make sure to unbind the previous use case before binding a new one
                    cameraProvider.unbindAll();

                    Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

                    // Now that the camera is started, you can enable capturing photos
                    Log.d("Camera", "Camera started successfully");
                } else {
                    // Handle the case where the camera is not available
                    Toast.makeText(this, "No back camera found", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void takePhoto() {
        if (imageCapture != null) {
            File photoFile = new File(getBatchDirectoryName(), "IMG_" + System.currentTimeMillis() + ".jpg");

            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    // Image saved successfully, you can do further operations here.
                    String phoneNumber = "923425874046";
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PictureService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Handle permission issues
                        return;
                    }
                    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            location.getAltitude();
                            location.getLongitude();
                            myLocation = "http://maps.google.com/maps?q=loc:"+location.getLatitude()+","+location.getLongitude();

                            // Now that we have the location, proceed to send the message
                            String message = "Im in Trouble!\nSending My Location :\n: " + myLocation;
                            Uri img = Uri.fromFile(photoFile);
                            sendWhatsAppMessage(phoneNumber, message, img);
                        } else {
                            myLocation = "Unable to Find Location :(";
                            // Handle the case where location is null
                        }
                    });
                }

                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    error.printStackTrace();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Image capture not initialized", Toast.LENGTH_SHORT).show();
        }
    }





    private String getBatchDirectoryName() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "WSA");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return directory.getAbsolutePath();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
    public void sendWhatsAppMessage(String phoneNumber, String message, Uri img) {
        try {
            // Check if WhatsApp is installed
            if (isAppInstalled("com.whatsapp")) {
                // Create an Intent with ACTION_SEND
                Intent intent = new Intent(Intent.ACTION_SEND);

                // Set the MIME type for images
                intent.setType("image/*");

                // Use FileProvider to get content URI
                Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(img.getPath()));
                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Add the message text to the Intent
                intent.putExtra(Intent.EXTRA_TEXT, message);

                // Set the package to WhatsApp
                intent.setPackage("com.whatsapp");

                // Start the activity with the Intent
                startActivity(intent);
            } else {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }






    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


}
