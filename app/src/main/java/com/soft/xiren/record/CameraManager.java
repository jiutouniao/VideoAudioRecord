package com.soft.xiren.record;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.soft.xiren.record.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 主要对相机预览进行处理
 */
public class CameraManager {

    private static final boolean DEBUG = false; // TODO set false on release
    private static final String TAG = "CameraGLView";
    private static CameraManager mCameraManager;
    //主要进行预览的主要是 480 *640
    private static final int MIN_HEIGHT =480;
    private static final int CAMERA_ID = 0;
    public static Camera mCamera;
    //判断是否是前置摄像头
    private  static boolean mIsFrontFace;
    public static  int IMAGE_HEIGHT = 720;
    public static  int IMAGE_WIDTH = 1280;
    public static synchronized CameraManager getInstance() {
        if (mCameraManager == null) {
            mCameraManager = new CameraManager();
        }
        return mCameraManager;
    }

    /**
     * 根据设置的相机尺寸进行预览
     * @param width
     * @param height
     */
    public Camera init(Activity activity, final int width, final int height, OnCameraListener mListener) {
        if (DEBUG) Log.v(TAG, "startPreview:");
        if (mCamera == null) {
            // This is a sample project so just use 0 as camera ID.
            // it is better to selecting camera is available

            try {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                LogUtil.d("CAMERA_FACING_BACK   :"+Camera.CameraInfo.CAMERA_FACING_BACK);
                final Camera.Parameters params = mCamera.getParameters();
                final List<String> focusModes = params.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                } else {
                    if (DEBUG) Log.i(TAG, "Camera does not support autofocus");
                }
                // let's try fastest frame rate. You will get near 60fps, but your device become hot.
                final List<int[]> supportedFpsRange = params.getSupportedPreviewFpsRange();
//					final int n = supportedFpsRange != null ? supportedFpsRange.size() : 0;
//					int[] range;
//					for (int i = 0; i < n; i++) {
//						range = supportedFpsRange.get(i);
//						Log.i(TAG, String.format("supportedFpsRange(%d)=(%d,%d)", i, range[0], range[1]));
//					}
                final int[] max_fps = supportedFpsRange.get(supportedFpsRange.size() - 1);
                Log.i(TAG, String.format("fps:%d-%d", max_fps[0], max_fps[1]));
                params.setPreviewFpsRange(max_fps[0], max_fps[1]);
                params.setRecordingHint(true);
//                // request closest supported preview size
//                final Camera.Size closestSize = getClosestSupportedSize(
//                        params.getSupportedPreviewSizes(), width, height);
//                // request closest picture size for an aspect ratio issue on Nexus7
//                final Camera.Size pictureSize = getClosestSupportedSize(
//                        params.getSupportedPictureSizes(), width, height);
                final Camera.Size pictureSize =getClosestSupportedSize(params.getSupportedPreviewSizes(),params.getSupportedPictureSizes(),width, height);
                params.setPreviewSize(pictureSize.width, pictureSize.height);
                params.setPictureSize(pictureSize.width, pictureSize.height);
                // rotate camera preview according to the device orientation


                setRotation(activity,params,mListener);
                mCamera.setParameters(params);
                // get the actual preview size
                final Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                Log.i(TAG, String.format("previewSize(%d, %d)", previewSize.width, previewSize.height));
                // adjust view size with keeping the aspect ration of camera preview.
                // here is not a UI thread and we should request parent view to execute.
//                final SurfaceTexture st = parent.getSurfaceTexture();
//                st.setDefaultBufferSize(previewSize.width, previewSize.height);
//                mCamera.setPreviewTexture(st);
                //encoder
                if(mListener!=null){
                    mListener.OnCameraSize(pictureSize);
                }
                IMAGE_WIDTH = pictureSize.width;
                IMAGE_HEIGHT = pictureSize.height;
                Log.i(TAG, "pictureSize  : "+pictureSize.width +"   "+pictureSize.height);
//                Log.i(TAG,"closestSize  : "+closestSize.width +"   "+closestSize.height);
                Log.i(TAG,"Sizes  : "+new Gson().toJson(params.getSupportedPictureSizes() ) +"\n"+new Gson().toJson( params.getSupportedPreviewSizes()));
            }catch (final Exception e) {
                Log.e(TAG, "startPreview:", e);
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
                if(mListener!=null){
                    mListener.OnPermisson(false);
                }
            }

        }

        return  mCamera;
    }

    /**
     * 设置预览hoder
     * @param holder
     */
    public void setPreview(SurfaceHolder holder){
        try {
            if (mCamera != null) {
                // start camera preview display
                mCamera.setPreviewDisplay(holder);
            }
        }catch (Exception e){
            LogUtil.d("setPreview",e);
        }
    }

    /**
     * 开始预览
     */
    public void start(){
        try {
            mCamera.startPreview();
        }catch ( Exception e){
           LogUtil.e(getClass() +"start  ",e);
        }

    }

    /**
     * 停止预览界面静止
     */
    public void stop(){
        try {
            mCamera.stopPreview();
        }catch ( Exception e){
            LogUtil.e(getClass() +"stopPreview  ",e);
        }
    }

    /**
     *1.先从小到大排序，然后按照比例排序
     * 1.前提条件所有的相机的图片和预览都存在4:3比例
     * 计算和自己设置测尺寸最接近的比例
     * @param supportedSizes
     * @param requestedWidth
     * @param requestedHeight
     * @return
     */
    private  Camera.Size getClosestSupportedSize(List<Camera.Size> supportedSizes, final int requestedWidth, final int requestedHeight) {
        //从小到大排序
        Collections.sort(supportedSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(final Camera.Size lhs, final Camera.Size rhs) {
                return  new Integer(lhs.height).compareTo(rhs.height);
            }
        });
        Log.i(TAG, "SupportedSize  : "+new Gson().toJson(supportedSizes));
        //按照比例排序
        Collections.sort(supportedSizes, new Comparator<Camera.Size>() {

            private float diff(final Camera.Size size) {
                float number = Math.abs(requestedWidth/((float)requestedHeight) - size.width/((float)size.height));
                return number;
            }
            @Override
            public int compare(final Camera.Size lhs, final Camera.Size rhs) {
                return  new Float(diff(lhs)).compareTo(diff(rhs ));
            }
        });
        Camera.Size size =supportedSizes.get(0) ;
        for(int i= 0;i<supportedSizes.size();i++){
            if(supportedSizes.get(i).height>=MIN_HEIGHT){
                size =supportedSizes.get(i);
                break;
            }
        }
        Log.i(TAG, "getClosestSupportedSize  : "+new Gson().toJson(supportedSizes));
        return size;
    }


    /**
     * 计算出两组list中相同的数值，并且将相同的数值进行比较
     * @param supportedSizes
     * @param requestedWidth
     * @param requestedHeight
     * @return
     */
    private static  Camera.Size getClosestSupportedSize(List<Camera.Size> supportedSizes,List<Camera.Size> supportedSizes2, final int requestedWidth, final int requestedHeight) {

        //获取相匹配的尺寸
        List<Camera.Size> result = new ArrayList<>();
        for(int i= 0;i<supportedSizes.size();i++){
            for(int j=0;j<supportedSizes2.size();j++){
                if(supportedSizes.get(i).height==supportedSizes2.get(j).height && supportedSizes.get(i).width==supportedSizes2.get(j).width){
                    result.add(supportedSizes.get(i));
                }
            }
        }
        Log.i(TAG, "result  : "+new Gson().toJson(result));
        //从小到大排序
        Collections.sort(result, new Comparator<Camera.Size>() {
            @Override
            public int compare(final Camera.Size lhs, final Camera.Size rhs) {
                return  new Integer(lhs.height).compareTo(rhs.height);
            }
        });

        //将最接近的尺寸进行匹配
        Collections.sort(result, new Comparator<Camera.Size>() {

            private float diff(final Camera.Size size) {
                float number = Math.abs(requestedHeight/((float)requestedWidth) - size.height/((float)size.width));
                System.out.println(" diff  : "+number +"  "+requestedWidth+"    "+"   "+requestedHeight+"   "+size.height +"    "+size.width);

                return number;
            }

            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                // TODO Auto-generated method stub
                return new Float(diff(lhs)).compareTo(diff(rhs));
            }

        });
        Log.i(TAG, "getClosestSupportedSize  : "+new Gson().toJson(result));
        Camera.Size size =result.get(0) ;
        for(int i= 0;i<result.size();i++){
            if(result.get(i).height>=MIN_HEIGHT){
                size =result.get(i);
                break;
            }
        }
        return size;
    }

    /**
     * rotate preview screen according to the device orientation
     * @param params
     */
    public   int setRotation(Activity activity,final Camera.Parameters params, OnCameraListener mListener) {
        int degrees = 0;
        try {
            if (DEBUG) Log.v(TAG, "setRotation:");
            final Display display = ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            final int rotation = display.getRotation();
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }
            // get whether the camera is front camera or back camera
            final Camera.CameraInfo info =
                    new Camera.CameraInfo();
            Camera.getCameraInfo(CAMERA_ID, info);
            mIsFrontFace = (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
            if (mIsFrontFace) {	// front camera
                degrees = (info.orientation + degrees) % 360;
                degrees = (360 - degrees) % 360;  // reverse
            } else {  // back camera
                degrees = (info.orientation - degrees + 360) % 360;
            }
            // apply rotation setting
            mCamera.setDisplayOrientation(degrees);
            // XXX This method fails to call and camera stops working on some devices.
//			params.setRotation(degrees);
            if(mListener!=null){
                mListener.OnCameraRotation(degrees);
            }
        }catch (Exception e){
            LogUtil.d("setRotation",e);
        }
        return degrees;
    }


    /**
     * stop camera preview
     */
    public  void stopPreview() {
        try {
            if (DEBUG) Log.v(TAG, "stopPreview:");
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }        }catch ( Exception e){
            LogUtil.e(getClass() +"stopPreview  ",e);
        }
    }

    /**
     * 相机的接口回掉
     */
    public interface OnCameraListener {
        void OnPermisson(boolean flag);

        void OnCameraSize(Camera.Size size);

        void OnCameraRotation(int rotation);
    }

}
