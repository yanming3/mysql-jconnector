package com.yanming.packet;

import com.yanming.in.ErrorMessage;
import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;

/**
 * Created by allan on 16/10/20.
 */
public class ErrorPacket extends MysqlPacket<ErrorMessage> {

    public ErrorPacket(ByteBuf in) {
        super(in);
    }

    @Override
    ErrorMessage decodeBody0() {
        packet.skipBytes(1);//0xff标记
        int errCode = packet.readUnsignedShortLE();
        packet.skipBytes(6);
        byte[] errBytes = new byte[this.packetLen-1-2-6];
        packet.readBytes(errBytes);
        String errMsg = BufferUtils.toString(errBytes);
        return new ErrorMessage(errCode, errMsg);
    }
}
