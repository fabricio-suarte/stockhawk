package com.udacity.stockhawk.message;

import android.content.Context;
import android.database.Cursor;

import com.udacity.stockhawk.data.Contract;
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

        Cursor cursor = this.context
                .getContentResolver().query(Contract.Symbol.URI,
                        Contract.Symbol.SYMBOL_COLUMNS.toArray(new String[] {}),
                        null,
                        null,
                        null);

        if(cursor == null)
            return false;

        cursor.close();

        return cursor.getCount() > 0;
    }
}
