package com.yanming.packet;

import com.yanming.in.EOFMessage;
import io.netty.buffer.ByteBuf;

/**
 * Created by allan on 16/10/20.
 */
public class EofPacket extends MysqlPacket<EOFMessage> {

    public EofPacket(ByteBuf in) {
        super(in);
    }

    @Override
    EOFMessage decodeBody0() {
        return new EOFMessage();
    }
}
