package com.soft.xiren.record;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.soft.xiren.record.util.DateTimeUtils;
import com.soft.xiren.record.util.DisplayMetricsUtil;
import com.soft.xiren.record.util.LogUtil;
import com.soft.xiren.record.util.MyOrientationDetector;
import com.soft.xiren.record.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 描述：录像姐main控制
 * 作者：shaobing
 * 时间： 2016/11/28 17:08
 */
public class VideoActivity extends Activity implements View.OnClickListener,SurfaceHolder.Callback {

    public static final String OUTPUT_PATH = "output_path";
    private static final String TAG = "VideoActivity";
    //图像预览
    private SurfaceView mCameraGLView;
    private SurfaceHolder surfaceHolder;
    //录像按钮
    private ImageView mRecordButton;
    //使用按钮
    private TextView mUseButton;
    //重新开始录制按钮
    private TextView mReStartButton;
    //定时时间
    private TextView tvTimerTxt;
    //播放录像
    private ImageView ivPlay;

    private RelativeLayout rlytRecord;

    private RelativeLayout rlytFinish;

    private RelativeLayout rlytCamera;

    private ImageView customCameraShow;
    //记录当前的时间
    private long mCurrentTime;
    private boolean isRecording = false;
    //权限判断
    private boolean isPermisson;
    private Camera mCamera;
    private Camera.Size mCameraSize;
    private int mRotation;
    private String filePath = "";
    //判断横竖屏
    MyOrientationDetector myOrientationDetector;

    private boolean isPlaying;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_video4_3);
        initView();
        initData();
        initEvent();
    }
    /**
     * 初始化控件
     */
    private void initView(){
        mCameraGLView = (SurfaceView)findViewById(R.id.cameraView);
        mRecordButton = (ImageView) findViewById(R.id.record_button);
        mUseButton = (TextView) findViewById(R.id.use_button);
        mReStartButton = (TextView)findViewById(R.id.restart_button);
        tvTimerTxt = (TextView) findViewById(R.id.tv_countdown);
        ivPlay = (ImageView) findViewById(R.id.iv_play);
        rlytRecord = (RelativeLayout) findViewById(R.id.record_view);
        rlytFinish = (RelativeLayout) findViewById(R.id.finish_view);
        rlytCamera = (RelativeLayout) findViewById(R.id.rlyt_camera);
        customCameraShow = (ImageView) findViewById(R.id.custom_camera_show);
    }

    private void initData(){
        float width = DisplayMetricsUtil.getDisplayWidth(VideoActivity.this);
        float height = DisplayMetricsUtil.getDisplayHeight(VideoActivity.this);
        mCamera =  CameraManager.getInstance().init(VideoActivity.this, (int)height, (int)width, new CameraManager.OnCameraListener() {
            @Override
            public void OnPermisson(boolean flag) {
                ToastUtil.showLongToast(VideoActivity.this,"摄像头权限拒绝");
                isPermisson = true;
            }
            @Override
            public void OnCameraSize(Camera.Size size) {
                LogUtil.d("OnCameraSize  "+size.width+"  "+size.height );
                mCameraSize =size;
            }
            @Override
            public void OnCameraRotation(int rotation) {
                mRotation = rotation;
            }
        });
        RecorderManager.getInstanse().init(new RecorderManager.OnAudioPermissionListener() {
            @Override
            public void OnAudioPermission(boolean flag) {
                ToastUtil.showLongToast(VideoActivity.this,"录音权限拒绝");
                isPermisson = true;
            }
        });
        surfaceHolder = mCameraGLView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        if(mCameraSize!=null){
            surfaceHolder.setFixedSize(mCameraSize.width, mCameraSize.height);
        }
        surfaceHolder.addCallback(this);
//        filePath = getIntent().getStringExtra(ExtraConstant.EXTRA_OUTPUT_DATA);
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "/aaaaaa/"+System.currentTimeMillis() + ".mp4");
            LogUtil.d(file.getAbsolutePath());
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdir();
            }
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            filePath =file.getAbsolutePath();
        }catch (Exception e){
            LogUtil.d("filePath  ",e);
        }
    }

    private void initEvent(){
        mRecordButton.setOnClickListener(this);
        mUseButton.setOnClickListener(this);
        mReStartButton.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        myOrientationDetector = new MyOrientationDetector(this);
        myOrientationDetector.enable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myOrientationDetector.disable();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraManager.getInstance().stopPreview();
        MediaManager.getInstance().stopVideo();
    }
    //进行倒计时计数（最大3分钟）
    private CountDownTimer countDownTimer = new CountDownTimer(1000 * 60 * 3, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            tvTimerTxt.setText(DateTimeUtils.getMMSS(millisUntilFinished));
        }
        @Override
        public void onFinish() {
            //听着录音
            tvTimerTxt.setText("00:00");
            //模拟点击停止按钮
            mRecordButton.performClick();
        }
    };
    /**
     * 开启定时器
     */
    private void  startTimer(){
        if(countDownTimer!= null){
            countDownTimer.start();
        }
        tvTimerTxt.setText("3:00");
    }
    /**
     * 关闭定时器
     */
    private void stopTimer(){
        if(countDownTimer!= null){
            countDownTimer.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record_button:{

                if(!isRecording){
                    if(isPermisson){return;}
                    CameraManager.getInstance().start();
                    //在现有的基础上将屏幕旋转，保证旋转之后视频为正的
                    mRotation = CameraManager.getInstance().setRotation(VideoActivity.this,mCamera.getParameters(),null);
                    mRotation = mRotation+myOrientationDetector.getScreenOrientation();
                    MediaManager.getInstance().startVideo(mCamera,mCameraGLView.getHolder(),mCameraSize,mRotation,filePath);
                    mCurrentTime = System.currentTimeMillis();
                    handleView(0);
                    isRecording = true;
                    startTimer();
                }else{
                    if((System.currentTimeMillis()-mCurrentTime)<3000){
                        ToastUtil.showLongToast(this,"视频录制时间不能小于3秒");
                    }else{
                        //停止录像
                        isRecording = false;
                        LogUtil.d("btn_stop");
                        MediaManager.getInstance().stopVideo();
                        CameraManager.getInstance().stop();
                        handleView(1);
                        stopTimer();
                        try{
                            getBitmapsFromVideo(filePath);
                            LogUtil.d("getBitmapsFromVideo");

                        }catch ( Exception e){
                            LogUtil.d("getBitmapsFromVideo",e);
                        }
                    }
                }
                break;
            }
            case R.id.use_button:{
                // 使用
                setResult(RESULT_OK);
                finish();
                break;
            }
            case R.id.restart_button:{
                CameraManager.getInstance().start();
                //在现有的基础上将屏幕旋转，保证旋转之后视频为正的
                mRotation = CameraManager.getInstance().setRotation(VideoActivity.this,mCamera.getParameters(),null);
                mRotation = mRotation+myOrientationDetector.getScreenOrientation();
                MediaManager.getInstance().startVideo(mCamera,mCameraGLView.getHolder(),mCameraSize,mRotation,filePath);
                mCurrentTime = System.currentTimeMillis();
                handleView(0);
                isRecording = true;
                startTimer();
                break;
            }
            case R.id.iv_play:{
//                Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
//                intent.putExtra("videoPath", filePath);
//                startActivity(intent);
                isPlaying = true;
                break;
            }

        }
    }
    /**
     * 控制控件
     * @param state
     */
    private  void handleView(int state){
        switch (state){
            case 0:{
                //开始
                rlytRecord.setVisibility(View.VISIBLE);
                rlytFinish.setVisibility(View.GONE);
                mRecordButton.setImageResource(R.drawable.btn_ic_suspend);
                ivPlay.setVisibility(View.GONE);
                customCameraShow.setVisibility(View.GONE);
                break;
            }
            case 1:{
                //停止
                rlytRecord.setVisibility(View.GONE);
                rlytFinish.setVisibility(View.VISIBLE);
                mRecordButton.setImageResource(R.drawable.btn_ic_recording);
                ivPlay.setVisibility(View.VISIBLE);
                customCameraShow.setVisibility(View.VISIBLE);
                break;
            }
            case 2:{
                //开始
                rlytRecord.setVisibility(View.VISIBLE);
                rlytFinish.setVisibility(View.GONE);
                mRecordButton.setImageResource(R.drawable.btn_ic_recording);
                ivPlay.setVisibility(View.GONE);
                customCameraShow.setVisibility(View.GONE);
                break;
            }
        }
    }


    /**
     * 获取缩略图
     * @param videoPath
     * @throws Exception
     */
    private void getBitmapsFromVideo(String videoPath) throws Exception {
        if (StringUtils.isEmpty(videoPath)) {
            return;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        Bitmap bitmap = retriever.getFrameAtTime(1000 * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        customCameraShow.setImageBitmap(bitmap);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtil.d("surfaceCreated");
        CameraManager.getInstance().setPreview(holder);
        CameraManager.getInstance().start();
        isPlaying = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (!isPlaying ){
            handleView(2);
        }
        try {
            isRecording = false;
            MediaManager.getInstance().stopVideo();
            CameraManager.getInstance().stop();
            stopTimer();
            if(tvTimerTxt!=null){
                tvTimerTxt.setText("3:00");
            }
        }catch (Exception e){
            LogUtil.d("surfaceDestroyed",e);
        }
    }
}




