package com.king.learn.collection.myconcurrent.reactor;

public class RequestHandler extends Thread {
    private final Dispatcher dispatcher;
    private final int requestID;
    private final Integer resourceID;

    public RequestHandler(Dispatcher dispatcher, int requestID, Integer resourceID) {
        this.dispatcher = dispatcher;
        this.requestID = requestID;
        this.resourceID = resourceID;
    }

    @Override
    public void run() {
        try {
            Utils.sleep(1000);
            System.out.println("Request No. " + requestID + " has been resolved by resource " + resourceID);
        } finally {
            dispatcher.freeResource();
        }
    }
}
