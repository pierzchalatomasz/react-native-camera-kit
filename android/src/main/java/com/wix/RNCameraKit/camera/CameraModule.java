package com.wix.RNCameraKit.camera;

import android.hardware.Camera;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.wix.RNCameraKit.camera.commands.Capture;
import com.wix.RNCameraKit.camera.params.FlashModeParser;
import com.wix.RNCameraKit.camera.permission.CameraPermission;


public class CameraModule extends ReactContextBaseJavaModule {

    private final CameraPermission cameraPermission;
    private Promise checkPermissionStatusPromise;

    public CameraModule(ReactApplicationContext reactContext) {
        super(reactContext);
        cameraPermission = new CameraPermission();
        checkPermissionWhenActivityIsAvailable();
    }

    private void checkPermissionWhenActivityIsAvailable() {
        getReactApplicationContext().addLifecycleEventListener(new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                if (checkPermissionStatusPromise != null  && getCurrentActivity() != null) {
                    getCurrentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkPermissionStatusPromise.resolve(cameraPermission.checkAuthorizationStatus(getCurrentActivity()));
                            checkPermissionStatusPromise = null;
                        }
                    });
                }
            }

            @Override
            public void onHostPause() {

            }

            @Override
            public void onHostDestroy() {

            }
        });
    }

    @Override
    public String getName() {
        return "CameraModule";
    }

    @ReactMethod
    public void checkDeviceCameraAuthorizationStatus(Promise promise) {
        if (getCurrentActivity() == null) {
            checkPermissionStatusPromise = promise;
        } else {
            promise.resolve(cameraPermission.checkAuthorizationStatus(getCurrentActivity()));
        }
    }

    @ReactMethod
    public void requestDeviceCameraAuthorization(Promise promise) {
        cameraPermission.requestAccess(getCurrentActivity(), promise);
    }

    @SuppressWarnings("deprecation")
    @ReactMethod
    public void hasFrontCamera(Promise promise) {
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                promise.resolve(true);
                return;
            }
        }
    }

    @ReactMethod
    public void changeCamera(Promise promise) {
        CameraViewManager.instance().changeCamera();
        promise.resolve(true);
    }

    @ReactMethod
    public void setFlashMode(String mode, Promise promise) {
        CameraViewManager.instance().setFlashMode(new FlashModeParser().parse(mode));
        promise.resolve(true);
    }

    @ReactMethod
    public void hasFlashForCurrentCamera(Promise promise) {
        promise.resolve(CameraViewManager.instance().isFlashEnabled());
    }

    @ReactMethod
    public void getFlashMode(Promise promise) {
        promise.resolve(CameraViewManager.instance());
    }

    @ReactMethod
    public void capture(boolean saveToCameraRoll, final Promise promise) {
        new Capture(getReactApplicationContext(), saveToCameraRoll).execute(promise);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        cameraPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
