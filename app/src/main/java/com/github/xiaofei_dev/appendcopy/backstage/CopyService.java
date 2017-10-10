package com.github.xiaofei_dev.appendcopy.backstage;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.xiaofei_dev.appendcopy.R;


public final class CopyService extends Service {

    /**
     * 控制悬浮图标
     */
    private final int ADD_VIEW = 0;
    private final int ADD_APPEND_VIEW = 1;

    private LinearLayout iconFloatView;
    private LinearLayout iconFloatAppendView;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private String text = "" ;
    private boolean isAddView;
    private final Handler mHandler = new Handler();
    private Runnable mAutoRemoveView;
    private int flag = 0;
    private StringBuilder mStringBuilder = new StringBuilder();
    private boolean isFrist = true;
    private static final String TAG = "CopyService";

    private ClipboardManager cmb;


    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager)(getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        initView();
        initLayoutParams();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
//        Log.d(TAG, "onStartCommand: " + "flag: " + flag +"   "+"isAddView: "+isAddView);

//        if(!isFrist){
//            isFrist = true;
//            return super.onStartCommand(intent, flags, startId);
//        }
        if(intent != null){
            text = intent.getStringExtra("TEXT");
        }else {
            return super.onStartCommand(intent, flags, startId);
        }
        if(flag == ADD_APPEND_VIEW){
            mStringBuilder.append(text);
        }else if(flag == ADD_VIEW && isAddView && isFrist){
            removeView();
            //已主动调用移除悬浮窗的情况下，取消自动延时移除悬浮窗
            mHandler.removeCallbacks(mAutoRemoveView);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getApplication().startService(intent);
                }
            },510);
        }else if(flag == ADD_VIEW && !isAddView && isFrist){
            addView();
        }else {
            //由提示悬浮窗将内容复制至剪贴板的情况下不做任何操作，只是重置 isFrist 的值为 true
            isFrist = true;

            return super.onStartCommand(intent, flags, startId);
        }

        cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("content", "");
        cmb.setPrimaryClip(data);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private synchronized void addView(){
        if(!isAddView){
            iconFloatView.clearAnimation();
            iconFloatView.setAlpha(0);
            iconFloatView.setVisibility(View.VISIBLE);
            iconFloatView.animate().alpha(1).setDuration(500)
                    .start();
            mWindowManager.addView(iconFloatView,mLayoutParams);
            isAddView = true;
            mHandler.postDelayed(mAutoRemoveView = new Runnable() {
                @Override
                public void run() {
                    if(isAddView){
                        removeView();
                    }
                }
            },2500);
        }

    }

    private synchronized void removeView(){
        if(isAddView){
//            iconFloatView.clearAnimation();
//            iconFloatView.setAlpha(1);
//            iconFloatView.setVisibility(View.VISIBLE);
//            iconFloatView.animate().alpha(0).setDuration(500)
//                    .start();
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if(isAddView){
//                        mWindowManager.removeView(iconFloatView);
//                        isAddView = false;
//                    }
//                }
//            },500);
            mWindowManager.removeView(iconFloatView);
            isAddView = false;
        }
    }

    /**
     *desc：添加拼接剪贴板内容提示悬浮窗
     */
    private void addAppendView(){
        mWindowManager.addView(iconFloatAppendView,mLayoutParams);
        flag = ADD_APPEND_VIEW;
        isFrist = false;
        Toast.makeText(getApplicationContext(),R.string.begin_listening_clip,Toast.LENGTH_SHORT).show();
    }

    /**
     *desc：移除拼接剪贴板内容提示悬浮窗
     */
    private void removeAppendView(){
//        iconFloatAppendView.clearAnimation();
//        iconFloatAppendView.setAlpha(1);
//        iconFloatAppendView.setVisibility(View.VISIBLE);
//        iconFloatAppendView.animate().alpha(0).setDuration(500)
//                .start();
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mWindowManager.removeView(iconFloatAppendView);
//            }
//        },500);
        mWindowManager.removeView(iconFloatAppendView);
        flag = ADD_VIEW;
//        isFrist = true;
        mStringBuilder = mStringBuilder.delete(0,mStringBuilder.length());
//        stopSelf();
    }



/**
 *desc：初始化两个悬浮窗并添加点击事件
 */
    private void initView(){
        iconFloatView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.floating_icon,null);
        iconFloatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView();
                //已主动调用移除悬浮窗方法的情况下，取消自动延时移除悬浮窗
                mHandler.removeCallbacks(mAutoRemoveView);

                mStringBuilder.append(text);
                addAppendView();
            }
        });

        iconFloatAppendView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.floating_icon_append,null);
        iconFloatAppendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, "onClick: " + mStringBuilder.toString());
                //将拼接好的内容复制至剪贴板
                cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("content", mStringBuilder.toString());
                cmb.setPrimaryClip(data);
                Toast.makeText(getApplicationContext(),R.string.success,Toast.LENGTH_SHORT).show();
                removeAppendView();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopSelf();
                    }
                },500);
            }
        });
    }

    /**
     *desc：初始化 mLayoutParams 设置其各字段的值
     */
    private void initLayoutParams(){
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        int screenHeight = point.y;

        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.gravity = Gravity.TOP|Gravity.END;
        /*if (Build.VERSION.SDK_INT > 24) {
            //7.0 以上的系统限制了 TOAST 类型悬浮窗的使用
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }*/
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.y = screenHeight/3;
        mLayoutParams.width = iconFloatView.findViewById(R.id.floating_icon).getLayoutParams().width;
        mLayoutParams.height = iconFloatView.findViewById(R.id.floating_icon).getLayoutParams().height;
    }
}
