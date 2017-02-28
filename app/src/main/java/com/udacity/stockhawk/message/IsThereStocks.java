package com.udacity.stockhawk.message;

import android.content.Context;

import com.udacity.stockhawk.data.PrefUtils;

/**
 * Is there stocks node
 */

class IsThereStocks  extends MessageNode{

    private Context context;

    IsThereStocks(Context context, MessageNode failNode, MessageNode successNode) {
        super(failNode, successNode);

        this.context = context;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public boolean isConditionTrue() {
        return PrefUtils.getStocks(this.context).size() > 0;
    }
}
