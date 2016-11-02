package com.yanming.parser;

import com.yanming.response.ErrorResponse;
import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;

/**
 * Created by allan on 16/10/20.
 */
public class ErrorParser extends MysqlPacketParser<ErrorResponse> {

    public ErrorParser(ByteBuf in) {
        super(in);
    }

    @Override
    public ErrorResponse decodeBody0() {
        packet.skipBytes(1);//0xFF
        int errCode = packet.readUnsignedShortLE();
        packet.skipBytes(1);//忽略sql_state_marker
        String sqlState = BufferUtils.readString(packet, 5);
        byte[] errBytes = new byte[this.packetLen - 1 - 2 - 6];
        packet.readBytes(errBytes);
        String errMsg = BufferUtils.toString(errBytes);
        return new ErrorResponse(packetLen, packetNo, errCode, sqlState, errMsg);
    }
}
