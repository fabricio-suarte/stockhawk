package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.DetailActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * The Stocks remote view service implementation.
 */

public class StocksRemoteViewService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StocksRemoteViewsFactory();
    }

    //The RemoteViewsFactory implementation
    class StocksRemoteViewsFactory implements RemoteViewsFactory {

        private Cursor data;
        private DecimalFormat dollarFormat;
        private DecimalFormat dollarFormatWithPlus;
        private DecimalFormat percentageFormat;

        @Override
        public void onCreate() {

            this.dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            this.dollarFormatWithPlus = dollarFormat;
            this.dollarFormatWithPlus.setPositivePrefix("+$");

            this.percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            this.percentageFormat.setMaximumFractionDigits(2);
            this.percentageFormat.setMinimumFractionDigits(2);
            this.percentageFormat.setPositivePrefix("+");
        }

        @Override
        public void onDataSetChanged() {
            if (data != null) {
                data.close();
            }

            // This method is called by the app hosting the widget (e.g., the launcher)
            // However, our ContentProvider is not exported so it doesn't have access to the
            // data. Therefore we need to clear (and finally restore) the calling identity so
            // that calls use our process and permission
            final long identityToken = Binder.clearCallingIdentity();

            this. data = getContentResolver().query(Contract.Quote.URI,
                                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                                null, null, Contract.Quote.COLUMN_SYMBOL);

            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if(this.data != null) {
                this.data.close();

                data = null;
            }
        }

        @Override
        public int getCount() {

            if(data == null)
                return 0;

            return data.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION ||
                    data == null || !data.moveToPosition(position)) {
                return null;
            }

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item_quote);

            String symbol = this.data.getString(Contract.Quote.POSITION_SYMBOL);
            views.setTextViewText(R.id.symbol, symbol );
            views.setTextViewText(R.id.price,
                    dollarFormat.format(this.data.getFloat(Contract.Quote.POSITION_PRICE)));

            float rawAbsoluteChange = this.data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = this.data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            if (rawAbsoluteChange > 0) {
                views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }

            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            Context context = getApplicationContext();
            if (PrefUtils.getDisplayMode(context)
                    .equals(context.getString(R.string.pref_display_mode_absolute_key))) {

                views.setTextViewText(R.id.change, change);

            } else {
                views.setTextViewText(R.id.change, percentage);
            }

            //Set the fill intent for this item
            final Intent fillInIntent = new Intent();
            fillInIntent.putExtra(DetailActivity.EXTRA_STOCK_SYMBOL, symbol);

            views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.widget_list_item_quote);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (this.data.moveToPosition(position))
                return data.getLong(Contract.Quote.POSITION_ID);

            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

}
