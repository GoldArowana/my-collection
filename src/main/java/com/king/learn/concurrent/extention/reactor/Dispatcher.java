package com.king.learn.concurrent.extention.reactor;

public class Dispatcher {
    private final Demultiplexer demultiplexer;
    private final int requestID;
    private final Integer resourceID;

    public Dispatcher(Demultiplexer demultiplexer, int requestID, Integer resourceID) {
        this.demultiplexer = demultiplexer;
        this.requestID = requestID;
        this.resourceID = resourceID;
    }

    public RequestHandler createRequestHandler() {
        return new RequestHandler(this, requestID, resourceID);
    }

    public synchronized void freeResource() {
        demultiplexer.returnResource(resourceID);
    }

}
