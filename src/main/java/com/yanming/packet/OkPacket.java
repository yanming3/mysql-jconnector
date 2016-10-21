package com.yanming.packet;

import com.yanming.in.OKMessage;
import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;

/**
 * Created by allan on 16/10/20.
 */
public class OkPacket extends MysqlPacket<OKMessage> {
    public OkPacket(ByteBuf in) {
        super(in);
    }

    @Override
    OKMessage decodeBody0() {
        packet.skipBytes(1);//0x00表示OK packet
        long affectedNum = BufferUtils.readEncodedLenInt(packet);
        long lastInsertId = BufferUtils.readEncodedLenInt(packet);
        return new OKMessage(affectedNum, lastInsertId);
    }
}
