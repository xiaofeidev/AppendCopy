package com.github.xiaofei_dev.appendcopy.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;

import com.github.xiaofei_dev.appendcopy.R;
import com.github.xiaofei_dev.appendcopy.backstage.CopyService;
import com.github.xiaofei_dev.appendcopy.util.ToastUtil;

public final class MainActivity extends Activity {

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
        ToastUtil.showToast(getApplicationContext(),getString(R.string.begin));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        moveTaskToBack(true);
//        Toast.makeText(getApplicationContext(),R.string.begin,Toast.LENGTH_LONG).show();
        ToastUtil.showToast(getApplicationContext(),getString(R.string.begin));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(MainActivity.this,CopyService.class);
        stopService(intent);
        moveTaskToBack(true);
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
