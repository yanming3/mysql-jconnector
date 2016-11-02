package com.yanming.request;

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.Promise;

/**
 * Created by allan on 16/10/18.
 */
public class AuthRequest extends ClientRequest {

    private final ByteBuf body;


    public AuthRequest(Promise promise, int sequenceNo, ByteBuf body) {
        super(sequenceNo, promise);
        this.body = body;
    }


    public ByteBuf getBody() {
        return body;
    }
}
