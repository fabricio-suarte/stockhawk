package com.udacity.stockhawk.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import yahoofinance.histquotes.HistoricalQuote;

/**
 * Translates the Yahoo financial HistoricalQuote object to a JSonObject
 */

public final class HistoryDataTranslator {

    private static final String LOG = HistoryDataTranslator.class.getCanonicalName();

    public static final String JSON_ATTRIBUTE_DATE = "date";
    public static final String JSON_ATTRIBUTE_OPEN = "open";
    public static final String JSON_ATTRIBUTE_HIGH = "high";
    public static final String JSON_ATTRIBUTE_LOW = "low";
    public static final String JSON_ATTRIBUTE_CLOSE = "close";
    public static final String JSON_ATTRIBUTE_VOLUME = "volume";

    public static JSONObject getJSON(HistoricalQuote historicalQuote) {

        if(historicalQuote == null)
            return null;

        JSONObject obj = new JSONObject();

        try {
            obj.put(JSON_ATTRIBUTE_DATE, historicalQuote.getDate().getTimeInMillis());
            obj.put(JSON_ATTRIBUTE_OPEN, historicalQuote.getOpen());
            obj.put(JSON_ATTRIBUTE_HIGH, historicalQuote.getHigh());
            obj.put(JSON_ATTRIBUTE_LOW, historicalQuote.getLow());
            obj.put(JSON_ATTRIBUTE_CLOSE, historicalQuote.getClose());
            obj.put(JSON_ATTRIBUTE_VOLUME, historicalQuote.getVolume());
        }
        catch (JSONException ex) {
            Log.e(LOG, "Some thing went wrong during JSON object creation! ", ex);
        }

        return obj;
    }
}
