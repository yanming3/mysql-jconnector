package com.yanming.parser;

import com.yanming.response.OkResponse;
import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;

/**
 * Created by allan on 16/10/20.
 */
public class OkParser extends MysqlPacketParser<OkResponse> {
    public OkParser(ByteBuf in) {
        super(in);
    }

    @Override
    public OkResponse decodeBody0() {
        packet.skipBytes(1);//0x00表示OK parser
        long affectedNum = BufferUtils.readEncodedLenInt(packet);
        long lastInsertId = BufferUtils.readEncodedLenInt(packet);
        return new OkResponse(affectedNum, lastInsertId);
    }
}
