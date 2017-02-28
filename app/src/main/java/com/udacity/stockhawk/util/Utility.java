package com.udacity.stockhawk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.udacity.stockhawk.R;

/**
 * Generic helper for utility methods
 */

public final class Utility {

    /**
     * Returns if there is connectivity or not
     * @param context
     * @return boolean
     */
    public static boolean networkUp(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * Set the toolbar
     * @param activity
     * @param displayHomeAsUpEnabled
     */
    public static void setToolbar(AppCompatActivity activity, boolean displayHomeAsUpEnabled) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        if(toolbar != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled);
        }
    }

    /**
     * Set the toolbar
     * @param activity
     */
    public static void setToolbar(AppCompatActivity activity) {
        setToolbar(activity, false);
    }
}
