package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.github.mikephil.charting.renderer.scatter.ChevronUpShapeRenderer;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.StockHawkApp;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.HistoryDataTranslator;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.util.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {

            //Register default Stocks if not initialized...
            if(! PrefUtils.isInitialized(context)) {

                registerDefaultStocks(context);
                PrefUtils.setAsInitialized(context);
            }

            //Get current registered stocks
            Cursor cursor = context.getContentResolver().query(
                    Contract.Symbol.URI,
                    Contract.Symbol.SYMBOL_COLUMNS.toArray(new String[] {}),
                    null,
                    null,
                    null);

            if (cursor == null || cursor.getCount() == 0) {
                return;
            }

            Set<String> stockSymbols = new HashSet<>( cursor.getCount() );
            while (cursor.moveToNext()) {
                stockSymbols.add( cursor.getString(Contract.Symbol.POSITION_NAME) );
            }

            cursor.close();

            String[] stockArray = stockSymbols.toArray( new String[] {});
            Map<String, Stock> quotes = YahooFinance.get(stockArray);

            Iterator<String> iterator = stockSymbols.iterator();

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            String symbol = null;
            while (iterator.hasNext()) {
                symbol = iterator.next();

                Stock stock = quotes.get(symbol);
                StockQuote quote = stock.getQuote();

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();

                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x
                List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                JSONArray histJsonArray = new JSONArray();
                JSONObject histJsonObj;

                for (HistoricalQuote it : history) {

                    histJsonObj = HistoryDataTranslator.getJSON(it);
                    histJsonArray.put(histJsonObj);
                }

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);

                quoteCV.put(Contract.Quote.COLUMN_HISTORY, histJsonArray.toString());

                quoteCVs.add(quoteCV);

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(StockHawkApp.ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

            PrefUtils.setLastSuccessfulSyncDate(context);

        }
        catch (IOException exception) {
            String error = context.getString(R.string.error_server_connection);

            PrefUtils.setLastSyncError(context, error);
        }
        catch (Exception exception) {
            String error = context.getString(R.string.error_unknown);
            PrefUtils.setLastSyncError(context, error);
        }
    }

    private static void schedulePeriodic(Context context) {

        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        if (Utility.networkUp(context)) {

            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);

        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));

            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            scheduler.schedule(builder.build());
        }
    }

    //region private aux methods

    private static void registerDefaultStocks(Context context) {

        Set<String> stockPref = PrefUtils.getDefaultStocks(context);

        Iterator<String> iterator = stockPref.iterator();
        ArrayList<ContentValues> symbolsCVs = new ArrayList<>();
        ContentValues cv;

        while (iterator.hasNext()) {
            cv = new ContentValues();
            cv.put(Contract.Symbol.COLUMN_NAME, iterator.next());

            symbolsCVs.add(cv);
        }
        context.getContentResolver()
                .bulkInsert(Contract.Symbol.URI,
                        symbolsCVs.toArray(new ContentValues[symbolsCVs.size()]));

    }

    //endregion

}
