package com.app.instaassist.base;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import androidx.multidex.MultiDexApplication;

import com.app.instaassist.R;
import com.liulishuo.filedownloader.FileDownloader;

public class Base extends MultiDexApplication {


    private static Context sContext;
    private static Base sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        FileDownloader.setupOnApplicationOnCreate(this);


        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel("download-notification", getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH));
        }
    }



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sContext = base;
    }

    public static Base getInstance() {
        return sApplication;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


}
