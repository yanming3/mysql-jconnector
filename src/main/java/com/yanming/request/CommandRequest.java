package com.yanming.request;

import com.yanming.support.Command;
import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.Promise;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by allan on 16/10/18.
 */
public class CommandRequest extends ClientRequest {

    private final Command cmd;

    private final ByteBuf data;

    private Map<String, Object> extraData = new HashMap<>();

    public CommandRequest(Command cmd, Promise promise, ByteBuf data) {
        this(cmd, promise, data, 0x00);
    }

    public CommandRequest(Command cmd, Promise promise, ByteBuf data, int sequenceNo) {
        super(sequenceNo, promise);
        this.cmd = cmd;
        this.data = data;
    }

    public Command getCmd() {
        return cmd;
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
