package com.smewise.camera2;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;


public class CameraApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
