package com.venomvendor.sms.deduplicate.core;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class DeDuplicateApp extends Application {
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        File myFile = new File(Environment.getExternalStorageDirectory() + "/deduplicate.tar");
        if (myFile.exists()) {
            myFile.delete();
        }
        try {
            myFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
