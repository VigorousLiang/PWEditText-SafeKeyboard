package com.vigorous.safetykeyboard.base;


import android.app.Application;

import com.vigorous.safetykeyboard.jni.IJniInterface;

/**
 * Created by liangshuai on 2017/8/22.
 */

public class BaseApplication extends Application {
    static {
        System.loadLibrary("securityKey");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!IJniInterface.iJNIE()) {
            throw new RuntimeException();
        }
    }
}
