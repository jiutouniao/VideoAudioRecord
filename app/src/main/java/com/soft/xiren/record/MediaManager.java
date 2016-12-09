package com.soft.xiren.record;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;

import com.soft.xiren.record.util.LogUtil;

import java.io.File;

/**
 * Created by shaobing on 2016/12/3.
 */

public class MediaManager {

    private static  MediaManager mMediaManager;
    private MediaRecorder mediaRecorder;


    public static MediaManager getInstance(){
            if(mMediaManager== null){
                mMediaManager = new MediaManager();
            }
        return mMediaManager;
    }
    /**
     * 先开始摄像头，后开始录制
     * 开始录制视频
     * @param mCamera 摄像头变量
     * @param holder   surfaceView
     * @param mCameraSize   选中的摄像头尺寸
     * @param mRotation     根据摄像头获取的偏转角度
     */
    public void startVideo(Camera mCamera, SurfaceHolder holder,Camera.Size mCameraSize,int mRotation,String path) {
        try {
            //防止空指针
            if(path==null || path.isEmpty() ||mCamera==null  ||holder==null ||mCameraSize==null){
                return;
            }
            File file = new File(path);
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdir();
            }
            if(file.exists()){
                file.delete();
            }
            mediaRecorder = new MediaRecorder();
            mCamera.unlock();
            mediaRecorder.setCamera(mCamera);
            mediaRecorder.setPreviewDisplay(holder.getSurface());
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            if (null != path) {
                mediaRecorder.setOutputFile(path);
            }
            mediaRecorder.setVideoSize(mCameraSize.width, mCameraSize.height);
            Log.i("CameraGLView","mediaRecorder  : "+mCameraSize.width +"   "+mCameraSize.height);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            //旋转的角度不能够大于360
            mediaRecorder.setOrientationHint(mRotation%360);
            LogUtil.d("mRotation  :"+(mRotation%360));
            mediaRecorder.prepare();
            mediaRecorder.start();
        }catch (Exception e){
            LogUtil.e(getClass() +" startVideo  ",e);
            stopVideo();
        }
    }

    /**
     * 先停止录制视频后释放摄像头
     * 停止录制视频
     */
    public void stopVideo(){
        try {
            if(null != mediaRecorder){
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        }catch ( Exception e){
            LogUtil.e(getClass() +"stopVideo  ",e);
        }
    }


}
