package com.udacity.stockhawk.message;

/**
 * A kind of binary decision tree node for getting a proper user message feedback
 */

public abstract class MessageNode {

    //region attributes

    private MessageNode failNode;
    private MessageNode sucessNode;
    private String message;

    //endregion

    //region constructor

    MessageNode(MessageNode failNode,
                       MessageNode successNode) {
        this.failNode = failNode;
        this.sucessNode = successNode;
    }

    //endregion

    //region public methods

    MessageNode getFailNode() {
        return  this.failNode;
    }
    MessageNode getSuccessNode() {
        return this.sucessNode;
    }

    boolean isParent() {
        return (this.failNode != null || this.sucessNode != null);
    }

    public abstract String getMessage();

    public abstract boolean isConditionTrue();

    /**
     * Go throughout the tree and get the result message
     * @param root the initial node
     * @return String
     */
    public static String getResultMessage(MessageNode root) {

        if(root == null)
            return null;

        String message;

        if(root.isParent()) {
            if(root.isConditionTrue()) {
                message = getResultMessage(root.getSuccessNode());
            }
            else {
                message = getResultMessage(root.getFailNode());
            }
        }
        else {
            message = root.getMessage();
        }

        return message;
    }

    //endregion

    //region private aux methods


    //endregion
}
