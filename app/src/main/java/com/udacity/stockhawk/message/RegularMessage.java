package com.udacity.stockhawk.message;

/**
 * Represents an given regular message
 */

public class RegularMessage extends MessageNode {

    String message;

    public RegularMessage(String message) {
        super(null, null);

        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public boolean isConditionTrue() {
        return false;
    }
}
