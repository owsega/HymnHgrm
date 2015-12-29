package com.owsega.odevotional;

import android.app.Application;

import com.owsega.odevotional.model.HymnHelper;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        HymnHelper.initDefaultHymn(getString(R.string.default_hymn));
    }
}
