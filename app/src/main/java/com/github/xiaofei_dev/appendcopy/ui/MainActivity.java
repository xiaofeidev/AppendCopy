package com.github.xiaofei_dev.appendcopy.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.github.xiaofei_dev.appendcopy.R;
import com.github.xiaofei_dev.appendcopy.backstage.CopyService;
import com.github.xiaofei_dev.appendcopy.util.ToastUtil;

public final class MainActivity extends Activity {

    public static int OVERLAY_PERMISSION_REQ_CODE = 110;
    private static final String TAG = "MainActivity";

    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        clipBoardMonitor();

    }

    @Override
    protected void onStart() {
        super.onStart();
        moveTaskToBack(true);
//        Toast.makeText(getApplicationContext(),R.string.begin,Toast.LENGTH_LONG).show();

        if(Build.VERSION.SDK_INT >= 23){
            if(Settings.canDrawOverlays(this)){
//                //有悬浮窗权限则开启服务
//                clipBoardMonitor();
//                ToastUtil.showToast(this,getString(R.string.begin));
                //有悬浮窗权限则只弹出提示消息
                ToastUtil.showToast(getApplicationContext(),getString(R.string.begin));
            }else {
                //没有悬浮窗权限,去开启悬浮窗权限
//                ToastUtil.showToast(this,"您需要授予应用在其他应用的上层显示的权限才可正常使用");
                Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent1, OVERLAY_PERMISSION_REQ_CODE);
            }
        }else {
            //
        }

        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(Build.VERSION.SDK_INT>=23) {
                        if (Settings.canDrawOverlays(MainActivity.this)) {
                            Toast.makeText(getApplicationContext(),"获取权限成功！应用可以正常使用了",Toast.LENGTH_SHORT).show();
//                            Log.d(TAG, "获取权限成功！应用可以正常使用了");
//                    moveTaskToBack(true);
//                    //悬浮窗权限获取成功，开启服务
//                    clipBoardMonitor();
                        } else {
                            ToastUtil.showToast(getApplicationContext(),"获取权限失败，应用将无法工作");
//                            Log.d(TAG, "获取权限失败，应用将无法工作");
//                    moveTaskToBack(true);
                            finish();
                        }
                    }
                }
            }, 0);
        }
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        moveTaskToBack(true);
//
//        if(Build.VERSION.SDK_INT >= 23){
//            if(Settings.canDrawOverlays(this)){
//                //有悬浮窗权限则只弹出提示消息
//                ToastUtil.showToast(getApplicationContext(),getString(R.string.begin));
//            }else {
//                //没有悬浮窗权限,去开启悬浮窗权限
//                ToastUtil.showToast(this,"应用的悬浮窗权限被拒绝，请重启应用以获取");
////                try{
////                    Intent  intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
////                    startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
////                }catch (Exception e)
////                {
////                    e.printStackTrace();
////                }
//            }
//        }else {
//            //
//        }
//
//        Log.d(TAG, "onRestart: ");
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(MainActivity.this,CopyService.class);
        stopService(intent);
        moveTaskToBack(true);
        Log.d(TAG, "onDestroy: ");
    }

    /**
     *desc：此方法设置监听剪贴板变化，如有新的剪贴内容就启动主活动
     */
    private void clipBoardMonitor(){
        final ClipboardManager clipBoard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                ClipData clipData = clipBoard.getPrimaryClip();
                ClipData.Item item = clipData.getItemAt(0);
                //String text = item.getText().toString();//万一复制到剪贴板的不是纯文本，此方法将导致程序崩溃
                String text = item.coerceToText(MainActivity.this).toString();//
                //下面的条件判断是为了防止与淘宝淘口令的冲突问题。
                if(text.equals("")){
                    return;
                }

                Intent intent = new Intent(MainActivity.this,CopyService.class);
                intent.putExtra("TEXT",text);
                MainActivity.this.startService(intent);
            }
        });
    }
}
