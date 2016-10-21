package com.yanming.packet;

import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;

/**
 * Created by allan on 16/10/20.
 */
public class LengthPacket extends MysqlPacket<Integer> {

    public LengthPacket(ByteBuf in) {
        super(in);
    }

    @Override
    Integer decodeBody0() {
        return (int) BufferUtils.readEncodedLenInt(packet);
    }
}
