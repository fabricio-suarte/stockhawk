package com.udacity.stockhawk.message;

import android.content.Context;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;

/**
 * Builds the binary decision tree for messaging feedback
 */

public final class MessageDecisionTreeBuilder {

    public static MessageNode buildTree(Context context, int qtdItems) {

        MessageNode connectivityIsThereData = getConnectivityIsThereDataTree(context, qtdItems);
        MessageNode noConnectivityIsThereData = getNoConnectivityIsThereDataTree(context, qtdItems);

        //returns the root node
        return new IsThereConnectivity(context, noConnectivityIsThereData, connectivityIsThereData);

    }

    private static MessageNode getConnectivityIsUpToDateTree(Context context) {

        //Leaves
        MessageNode noErrorNode = new RegularMessage("");
        MessageNode outOfDateErrorNode = new OutOfDateErrorMessage(context);

        return new IsUpToDate(context, outOfDateErrorNode, noErrorNode);
    }

    private static MessageNode getNoConnectivityIsUpToDateTree(Context context) {

        String message = context.getString(R.string.status_no_connectivity);
        MessageNode noConnectivityNode = new RegularMessage(message);
        MessageNode outOfDateErrorNode = new OutOfDateErrorMessage(context);

        return new IsUpToDate(context, outOfDateErrorNode, noConnectivityNode);
    }

    private static MessageNode getIsThereStocksTree(Context context) {

        //Leaves
        String message = context.getString(R.string.error_no_stocks);
        MessageNode noStocksError = new RegularMessage(message);

        MessageNode lastSyncError = new LastSyncErrorMessage(context);

        return new IsThereStocks(context, noStocksError, lastSyncError);
    }

    private static MessageNode getConnectivityIsThereDataTree(Context context, int qtdItems) {

        MessageNode isUpToDateNode = getConnectivityIsUpToDateTree(context);
        MessageNode isThereStocksNode = getIsThereStocksTree(context);

        return new IsThereData(context, qtdItems, isThereStocksNode, isUpToDateNode);
    }

    private static MessageNode getNoConnectivityIsThereDataTree(Context context, int qtdItems) {

        MessageNode isUpToDateNode = getNoConnectivityIsUpToDateTree(context);

        String message  = context.getString(R.string.status_no_connectivity);
        MessageNode noConnectivityStatus = new RegularMessage(message);

        return new IsThereData(context, qtdItems, noConnectivityStatus, isUpToDateNode);
    }

}