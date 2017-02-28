package com.udacity.stockhawk.message;

import android.content.Context;
import android.text.TextUtils;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;

/**
 * Out of date error message
 */

public class OutOfDateErrorMessage extends MessageNode {

    private Context context;

    public OutOfDateErrorMessage(Context context) {
        super(null, null);
        this.context = context;
    }

    @Override
    public String getMessage() {

        String lastSyncError = PrefUtils.getLastSyncError(this.context);
        String outOfDateError = this.context.getString(R.string.error_out_of_date);

        String finalMessage;
        if(!TextUtils.isEmpty(lastSyncError)) {

            String format = context.getString(R.string.error_out_of_date_plus_sync_error);
            finalMessage =
                    String.format(format, outOfDateError, lastSyncError);
        }
        else{
            finalMessage = outOfDateError;
        }

        return finalMessage;
    }

    @Override
    public boolean isConditionTrue() {
        return false;
    }
}
