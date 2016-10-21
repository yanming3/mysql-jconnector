package com.yanming.out;

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.Promise;

import java.io.Serializable;

/**
 * Created by allan on 16/10/18.
 */
public class HandshakeResponse implements Serializable{

    private static final long serialVersionUID = 6708897802323870936L;

    private final Promise<Object> promise;

    private final int sequenceNo;

    private final ByteBuf body;


    public HandshakeResponse(Promise<Object> promise, int sequenceNo, ByteBuf body) {
        this.promise = promise;
        this.sequenceNo = sequenceNo;
        this.body = body;
    }

    public Promise<Object> getPromise() {
        return promise;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public ByteBuf getBody() {
        return body;
    }
}
