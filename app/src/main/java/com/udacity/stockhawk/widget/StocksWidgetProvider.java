package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.StockHawkApp;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Stocks widget provider
 */

public class StocksWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            /* Create an Intent to launch MainActivity */
            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, mainPendingIntent);

            // Set up the list view
            views.setRemoteAdapter(R.id.widget_list_view,
                    new Intent(context, StocksRemoteViewService.class));

            //Set the generic pending intent for all items. The specific intent
            // for each item should be set using "setOnClickFillInIntent".
            boolean twoPanelLayout = context.getResources().getBoolean(R.bool.twoPanelLayout);

            Intent clickItemIntent = !twoPanelLayout
                    ? new Intent(context, DetailActivity.class)
                    : new Intent(context, MainActivity.class);

            PendingIntent clickItemPendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickItemIntent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setPendingIntentTemplate(R.id.widget_list_view, clickItemPendingIntent);

            //Configure the empty view for the list view
            views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_text);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        if (StockHawkApp.ACTION_DATA_UPDATED.equals(intent.getAction())) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
        }
    }

}
