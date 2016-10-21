package com.yanming.out;

import io.netty.util.concurrent.Promise;

import java.io.Serializable;

/**
 * Created by allan on 16/10/18.
 */
public class CommandMessage<T> implements Serializable {

    private static final long serialVersionUID = 5402454652066563619L;

    private final Promise<T> promise;

    private final int sequenceNo;

    private final int command;

    private final String extraData;

    public CommandMessage(int command, Promise<T> promise, String extraData) {
        this.command = command;
        this.promise = promise;
        this.extraData = extraData;
        this.sequenceNo = 0;
    }

    public CommandMessage(int command, Promise<T> promise, String extraData, int sequenceNo) {
        this.command = command;
        this.promise = promise;
        this.extraData = extraData;
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

    public String getExtraData() {
        return extraData;
    }
}
