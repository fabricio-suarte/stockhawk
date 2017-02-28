package com.udacity.stockhawk.message;

import android.content.Context;

/**
 * Is there data node
 */

class IsThereData extends MessageNode{

    private Context context;
    private int qtdItems;

    IsThereData(Context context, int qtdItems, MessageNode failNode, MessageNode successNode) {
        super(failNode, successNode);

        this.context = context;
        this.qtdItems = qtdItems;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public boolean isConditionTrue() {
        return this.qtdItems > 0;
    }
}
