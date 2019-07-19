package com.example.librarycameraapi2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Camera2 extends TextureView implements TextureView.SurfaceTextureListener {

    private static final SparseArray ORIENTATION = new SparseArray();
    private CameraDevice cameraDevice;
    private Context context;
    private boolean isRecording; // to record video
    private Activity activity;
    private File videoFolder;
    private String videoFileName;
    private String mCameraId;
    private HandlerThread backGroundHandlerThread;
    private Handler bachGroundHandler;
    private Size previewSize;
    private CaptureRequest.Builder captureRequestBuidler;
    private boolean isFlashSupported; // to check flash in camera






    static {
        ORIENTATION.append(Surface.ROTATION_0, 0);
        ORIENTATION.append(Surface.ROTATION_90, 90);
        ORIENTATION.append(Surface.ROTATION_180, 180);
        ORIENTATION.append(Surface.ROTATION_270, 270);
    }

    public Camera2(Context context) {
        super(context);
    }

    public Camera2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Camera2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public Camera2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Camera2 camera2) {
        this.context = context;
        activity = (Activity) context;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    private CameraDevice.StateCallback mCameraDeviceStateCallbac = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            camera = cameraDevice;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            cameraDevice.close();
        }
    };




    //call in onResum at Activity
    // deviceOrientation = getWindowManager().getDefaultDisplay().getRotation()
    private void setUpCamera(int width, int height, int deviceOrientation) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                == CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                int totalRotation = getDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = totalRotation == 90 || totalRotation == 270;
                int rotatedWith = width;
                int rotatedHeight = height;
                if (swapRotation){
                    rotatedWith = height;
                    rotatedHeight = width;
                }
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWith, rotatedHeight);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void connectCamera(boolean isPermission){
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED){
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallbac, bachGroundHandler);
                }else{
                    activity.requestPermissions(new String[]{Manifest.permission.CAMERA}, 111);
                }
            }else{
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallbac, bachGroundHandler);
            }
        }catch (Exception e){

        }
    }

    private void recordVideo(){
        if (isRecording){
            isRecording = false;

        }else{

        }
    }

    private void startPreview(){
        SurfaceTexture texture = Camera2.this.getSurfaceTexture();
        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface previewSurface = new Surface(texture);

        try {
            captureRequestBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuidler.addTarget(previewSurface);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private static class CompareSizeByArea implements Comparator<Size>{

        @Override
        public int compare(Size o1, Size o2) {
            return Long.signum((long) o1.getWidth() * o1.getHeight() /
                    (long) o2.getWidth() * o2.getHeight());
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int with, int height){
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeList){
            if (option.getHeight() == option.getWidth() * height / with &&
            option.getWidth() >= with && option.getHeight() >= height){
                sizeList.add(option);
            }
        }
        if (sizeList.size() > 0){
            return Collections.min(sizeList, new CompareSizeByArea());
        }else{
            return choices[0];
        }
    }


    private void startBackgroundThread(){
        backGroundHandlerThread = new HandlerThread("LibraryCameraAPI2");
        backGroundHandlerThread.start();
        bachGroundHandler = new Handler(backGroundHandlerThread.getLooper());
    }


    // call at onPause in activity
    private void stopBackgroundThread(){
        backGroundHandlerThread.quitSafely();
        try {
            backGroundHandlerThread.join();
            backGroundHandlerThread = null;
            bachGroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int getDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation){

        int orientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = (int) ORIENTATION.get(deviceOrientation);
        return (orientation + deviceOrientation + 360) % 360;
    }

    private void creatVideoFolder(){
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        videoFolder = new File(file, "CPRVideo");
        if (!videoFolder.exists()){
            videoFolder.mkdirs();
        }
    }
    private File createVideoFileName() throws IOException {
        String timeSave = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name = "VIDEO_" + timeSave;
        File videoFile = File.createTempFile(name, ".mp4", videoFolder);
        videoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

}
