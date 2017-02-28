package com.udacity.stockhawk.message;

import android.content.Context;
import com.udacity.stockhawk.util.Utility;

/**
 * Is there connectivity
 */

class IsThereConnectivity extends MessageNode {

    private Context context;

    public IsThereConnectivity(Context context, MessageNode failNode, MessageNode successNode) {
        super(failNode, successNode);

        this.context = context;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public boolean isConditionTrue() {
        return Utility.networkUp(context);
    }
}
