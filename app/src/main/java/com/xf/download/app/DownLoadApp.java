package com.xf.download.app;

import android.app.Application;

import com.xf.download.dao.DaoManager;
import com.xf.utils.Lg;

public class DownLoadApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DaoManager.initeDao(this);
        Lg.init(this);
    }
}
