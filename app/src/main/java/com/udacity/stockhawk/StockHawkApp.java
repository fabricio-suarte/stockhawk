package com.udacity.stockhawk;

import android.app.Application;

public class StockHawkApp extends Application {

    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
