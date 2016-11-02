package com.yanming.parser;

import com.yanming.resultset.ResultSetRowResponse;
import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;

/**
 * Created by allan on 16/10/31.
 */
public class RowParser extends MysqlPacketParser<ResultSetRowResponse> {
    private int colCount;
    public RowParser(ByteBuf in, int colCount) {
        super(in);
        this.colCount=colCount;
    }

    @Override
    public ResultSetRowResponse decodeBody0() {
        String[] row = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            String value = null;
            short firstByte = packet.getUnsignedByte(packet.readerIndex());
            if (firstByte != 0xFB) {//0xFB表示NULL
                value = BufferUtils.readEncodedLenString(packet);
            } else {
                packet.skipBytes(1);
            }
            row[i] = value;
        }
        ResultSetRowResponse response=new ResultSetRowResponse(packetLen,packetNo,row);
        return response;
    }
}
