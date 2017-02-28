package com.udacity.stockhawk.message;

import android.content.Context;
import com.udacity.stockhawk.data.PrefUtils;

/**
 * Last Sync error message
 */

public class LastSyncErrorMessage extends MessageNode {

    private Context context;

    public LastSyncErrorMessage(Context context) {
        super(null, null);

        this.context = context;
    }

    @Override
    public String getMessage() {
        String  lastSyncError = PrefUtils.getLastSyncError(context);

        return lastSyncError;
    }

    @Override
    public boolean isConditionTrue() {
        return false;
    }
}
