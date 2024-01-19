package com.darkness.sparkwomen;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;

public class FlashlightUtility {

    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean isFlashOn = false;
    private Handler handler;
    private Runnable runnable;

    public FlashlightUtility(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void toggleFlash() {
        try {
            isFlashOn = !isFlashOn;
            mCameraManager.setTorchMode(mCameraId, isFlashOn);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startBlinking(int interval) {
        if (handler == null) handler = new Handler();
        if (runnable == null) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    toggleFlash();
                    handler.postDelayed(this, interval);
                }
            };
        }
        handler.post(runnable);
    }

    public void stopBlinking() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
            if (isFlashOn) {  // Turn off the flashlight if it's on
                toggleFlash();
            }
        }
    }
}
