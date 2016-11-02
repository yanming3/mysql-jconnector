package com.yanming.request;

import io.netty.util.concurrent.Promise;

/**
 * Created by allan on 16/11/1.
 */
public abstract class ClientRequest {
    private final int sequenceNo;
    private final Promise promise;

    public ClientRequest(int sequenceNo, Promise promise) {
        this.sequenceNo = sequenceNo;
        this.promise = promise;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public Promise<Object> getPromise() {
        return promise;
    }
}
