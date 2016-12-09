package com.soft.xiren.record.util;

import android.content.Context;
import android.view.OrientationEventListener;

/**
 * 时时监测屏幕方向是否发生改变
 * @author wilson.xiong
 */
public class MyOrientationDetector extends OrientationEventListener {

    //默认是竖屏
    private int  mScreenOrientation  =0;

    public int  getScreenOrientation(){
        return mScreenOrientation;
    }

    public MyOrientationDetector(Context context) {
        super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        LogUtil.d("onOrientationChanged    :"+orientation);
        //如果屏幕旋转被打开，则设置屏幕可旋转
        //0-57度 125-236度 306-360度  这些区间范围内为竖屏
        //58-124度 237-305度  这些区间范围内为横屏
       if((orientation == -1 || (orientation >= 0) && (orientation <= 57)) ||(orientation >= 306 && orientation <= 360)){
           mScreenOrientation = 0;
       }else if((orientation >=58) && (orientation <= 124)  ){
           mScreenOrientation = 90;
       }else if((orientation >=125) && (orientation <= 236)  ){
           mScreenOrientation =180;
       }else if((orientation >=237) && (orientation <= 305)  ){
           mScreenOrientation = 270;
       }
//        if ((orientation == -1 || (orientation >= 0) && (orientation <= 57)) || ((orientation >= 125) && (orientation <= 236)) || (orientation >= 306 && orientation <= 360)) {
//            mScreenOrientation =( orientation == -1)?0:orientation;//竖屏
//        } else if ((orientation >= 58 && orientation <= 124) || ((orientation >= 237 && orientation <= 305))) {
//            mScreenOrientation = orientation;//横屏
//        }
//        //          mOrientation = orientation;
    }

}