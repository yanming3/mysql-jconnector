package com.yanming.packet;

import com.yanming.in.ResultSetMessage;
import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allan on 16/10/20.
 */
public class ResultSetPacket extends MysqlPacket<ResultSetMessage> {

    private int columCount;

    public ResultSetPacket(ByteBuf in, int columCount) {
        super(in);
        this.columCount = columCount;
    }

    @Override
    ResultSetMessage decodeBody0() {
        List<String> column = new ArrayList<>();
        List<String[]> data = new ArrayList<>();
        for (int i = 0; i < this.columCount; i++) {
            FieldPacket fieldPacket = new FieldPacket(this.packet);
            fieldPacket.decode();
            column.add(fieldPacket.getBody());
        }
       // skipPacket(1);

        for (; ; ) {
            if (BufferUtils.isEOFPacket(packet) || BufferUtils.isOKPacket(packet)) {
                break;
            }
            RowPacket rowPacket = new RowPacket(packet);
            rowPacket.decode();
            data.add(rowPacket.getBody());
        }
        return new ResultSetMessage(column, data);
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


    class FieldPacket extends MysqlPacket<String> {
        public FieldPacket(ByteBuf in) {
            super(in);
        }

        @Override
        String decodeBody0() {
            String catalog = BufferUtils.readEncodedLenString(packet);
            String schema = BufferUtils.readEncodedLenString(packet);
            String table = BufferUtils.readEncodedLenString(packet);
            String orgTable = BufferUtils.readEncodedLenString(packet);
            String name = BufferUtils.readEncodedLenString(packet);
            String orgName = BufferUtils.readEncodedLenString(packet);
            long len = BufferUtils.readEncodedLenInt(packet);
            int charsetIndex = packet.readUnsignedShortLE();
            long columnLen = packet.readUnsignedIntLE();
            int type = packet.readUnsignedByte();
            int flags = packet.readUnsignedShortLE();
            int decimals = packet.readUnsignedByte();
            packet.skipBytes(2);
            return name;
        }
    }

    class RowPacket extends MysqlPacket<String[]> {
        public RowPacket(ByteBuf in) {
            super(in);
        }

        @Override
        String[] decodeBody0() {
            String[] row = new String[columCount];
            for (int i = 0; i < columCount; i++) {
                String value = null;
                short firstByte = packet.getUnsignedByte(packet.readerIndex());
                if (firstByte != 0xfb) {//0xfb表示NULL
                    value = BufferUtils.readEncodedLenString(packet);
                }
                else{
                    packet.skipBytes(1);
                }
                row[i] = value;
            }
            return row;
        }
    }
}
