package com.softartdev.conwaysgameoflife;

import android.app.Application;

import com.softartdev.conwaysgameoflife.util.CrashlyticsTree;

import timber.log.Timber;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashlyticsTree());
    }
}
