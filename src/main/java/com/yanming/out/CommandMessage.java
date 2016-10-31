package com.yanming.out;

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.Promise;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by allan on 16/10/18.
 */
public class CommandMessage<T> implements Serializable {

    private static final long serialVersionUID = 5402454652066563619L;

    private final Promise<T> promise;

    private final int sequenceNo;

    private final int command;

    private final ByteBuf data;

    private Map<String, Object> extraData = new HashMap<>();

    public CommandMessage(int command, Promise<T> promise, ByteBuf data) {
        this.command = command;
        this.promise = promise;
        this.data = data;
        this.sequenceNo = 0;
    }

    public CommandMessage(int command, Promise<T> promise, ByteBuf data, int sequenceNo) {
        this.command = command;
        this.promise = promise;
        this.data = data;
        this.sequenceNo = sequenceNo;
    }


    public Promise<T> getPromise() {
        return promise;
    }


    public int getSequenceNo() {
        return sequenceNo;
    }

    public int getCommand() {
        return command;
    }

    public ByteBuf getData() {
        return data;
    }

    public void addExtraData(String key, Object value) {
        extraData.put(key, value);
    }

    public Object getExtraData(String key) {
        return extraData.get(key);
    }
}
