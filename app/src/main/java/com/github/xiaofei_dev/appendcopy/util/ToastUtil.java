package com.github.xiaofei_dev.appendcopy.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by xiaofei on 2017/7/13.
 */

public final class ToastUtil {

    private static Toast toast;

    public static void showToast(Context context,
                                 String content) {
        if (toast == null) {
            toast = Toast.makeText(context,
                    content,
                    Toast.LENGTH_LONG);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
}
