package com.yanming.parser;

import com.yanming.response.ResultSetMessage;
import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allan on 16/10/20.
 */
public class ResultSetParser extends MysqlPacketParser<ResultSetMessage> {

    private int columCount;

    private boolean deprecatedEOF;


    public ResultSetParser(ByteBuf in, int columCount, boolean deprecatedEOF) {
        super(in);
        this.columCount = columCount;
        this.deprecatedEOF = deprecatedEOF;
    }


    @Override
    public ResultSetMessage decodeBody0() {
        List<String> column = new ArrayList<>();
        List<String[]> data = new ArrayList<>();
        for (int i = 0; i < this.columCount; i++) {
            FieldParser fieldParser = new FieldParser(this.packet,0);
            fieldParser.decode();
            column.add(fieldParser.getBody().getField().getColumnName());
        }

        decodePlainRow(data);

        return new ResultSetMessage(column, data);
    }


    private void decodePlainRow(List<String[]> data) {
        skipPacket(1);
        for (; ; ) {
            if (BufferUtils.isEOFPacket(packet) || BufferUtils.isOKPacket(packet)) {
                skipPacket(1);
                break;
            }
            RowParser rowParser = new RowParser(packet,this.columCount);
            rowParser.decode();
            data.add(rowParser.getBody().getRow());
        }
    }

    @Override
    boolean isDecodeHeader() {
        return false;
    }

    private void skipPacket(int count) {
        for (int i = 0; i < count; i++) {
            int packetLen = packet.readUnsignedMediumLE();
            packet.skipBytes(1 + packetLen);
        }
    }
}
