package com.udacity.stockhawk.message;

import android.content.Context;
import com.udacity.stockhawk.data.PrefUtils;

/**
 * Is Up to Date node
 */

class IsUpToDate extends MessageNode {
    private Context context;

    IsUpToDate(Context context, MessageNode left, MessageNode right) {
        super(left, right);
        this.context = context;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public boolean isConditionTrue() {
        return !PrefUtils.isOutOfDate(context);
    }
}