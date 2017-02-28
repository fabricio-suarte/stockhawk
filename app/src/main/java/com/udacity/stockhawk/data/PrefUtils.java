package com.udacity.stockhawk.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.udacity.stockhawk.R;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class PrefUtils {

    private static final long  MILLISECONDS_IN_A_DAY = 1000 * 60 * 60 * 24;

    private PrefUtils() {
    }

    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        return prefs.getStringSet(stocksKey, new HashSet<String>());
    }

    private static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = getStocks(context);

        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, stocks);
        editor.apply();
    }

    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
    }

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static String getDetailsDisplayMode(Context context) {
        String key = context.getString(R.string.pref_detail_display_mode_key);
        String defaultValue = context.getString(R.string.pref_detail_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

    public static void toggleDetailsDisplayMode(Context context) {
        String key = context.getString(R.string.pref_detail_display_mode_key);
        String graphKey = context.getString(R.string.pref_detail_display_mode_graph_key);
        String listKey = context.getString(R.string.pref_detail_display_mode_list_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDetailsDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(graphKey)) {
            editor.putString(key, listKey);
        } else {
            editor.putString(key, graphKey);
        }

        editor.apply();
    }

    @SuppressLint("ApplySharedPref")
    public static void setLastSuccessfulSyncDate(Context context) {
        String key = context.getString(R.string.last_successful_sync_date_key);

        long currentDateTime = new Date().getTime();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //it's ok to use "commit", because this method should be called from an async thread
        prefs.edit().putLong(key, currentDateTime ).commit();

    }

    public static boolean isOutOfDate(Context context) {

        String key = context.getString(R.string.last_successful_sync_date_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long lastSync = prefs.getLong(key, 0);
        long currentDateTime = new Date().getTime();

        long diff = (currentDateTime - lastSync) / MILLISECONDS_IN_A_DAY;

        return (diff >= 1);
    }

    public static boolean isOnSyncStatusError(Context context) {

        String key = context.getString(R.string.error_on_last_sync_key);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getBoolean(key, false);
    }

    public static String getLastSyncError(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.error_on_last_sync_message_key);

        return sp.getString(key, "");
    }

    public static void setLastSyncError(Context context, String error) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.error_on_last_sync_message_key);

        //It's ok commit! it will be called from a method that is executed in a background thread
        sp.edit().putString(key, error).commit();
    }

}
