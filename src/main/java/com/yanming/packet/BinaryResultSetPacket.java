package com.yanming.packet;

import com.yanming.in.BinaryResultSetMessage;
import com.yanming.support.BufferUtils;
import com.yanming.support.FieldType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allan on 16/10/27.
 */
public class BinaryResultSetPacket extends MysqlPacket<BinaryResultSetMessage> {

    private int columCount;

    private boolean deprecatedEOF;

    private FieldType[] columnTypes;

    public BinaryResultSetPacket(ByteBuf in, int columCount, boolean deprecatedEOF) {
        super(in);
        this.columCount = columCount;
        this.deprecatedEOF = deprecatedEOF;
    }


    @Override
    BinaryResultSetMessage decodeBody0() {
        List<String> column = new ArrayList<>();
        List<byte[][]> data = new ArrayList<>();
        columnTypes=new FieldType[this.columCount];
        for (int i = 0; i < this.columCount; i++) {
            FieldPacket fieldPacket = new FieldPacket(this.packet);
            fieldPacket.decode();
            column.add(fieldPacket.getBody().getName());
            columnTypes[i] = fieldPacket.getBody().getType();
        }
        decodeBinaryRow(data);

        return new BinaryResultSetMessage(column, data);
    }

    /**
     * 二进值协议,具体参见
     * http://dev.mysql.com/doc/internals/en/binary-protocol-resultset-row.html#packet-ProtocolBinary::ResultsetRow
     *
     * @param data
     */
    private void decodeBinaryRow(List<byte[][]> data) {
        skipPacket(1);
        for (; ; ) {
            if (BufferUtils.isEOFPacket(packet)) {
                skipPacket(1);
                break;
            }

            BinaryRowPacket rowPacket = new BinaryRowPacket(packet, columnTypes, columCount);
            rowPacket.decode();
            data.add(rowPacket.getBody());
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
