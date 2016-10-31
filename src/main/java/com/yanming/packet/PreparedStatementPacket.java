package com.yanming.packet;

import com.yanming.in.PreparedStatementMessage;
import com.yanming.support.BufferUtils;
import com.yanming.support.FieldType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allan on 16/10/25.
 */
public class PreparedStatementPacket extends MysqlPacket<PreparedStatementMessage> {

    private long serverStatementId;

    private int numParams;

    private int numColumns;

    public PreparedStatementPacket(ByteBuf in) {
        super(in);
    }

    @Override
    PreparedStatementMessage decodeBody0() {
        packet.skipBytes(1);//00,一个字节
        this.serverStatementId = packet.readUnsignedIntLE();//statement id,4 bytes
        this.numColumns = packet.readUnsignedShortLE();
        this.numParams = packet.readUnsignedShortLE();
        packet.skipBytes(1 + 2);//00,保留字;2个字节的warnng count

        PreparedStatementMessage message = new PreparedStatementMessage(serverStatementId, numColumns, numParams, packetNo);

        if (numParams > 0) {
            FieldType[] paramTypes = new FieldType[numParams];
            List<String> params = new ArrayList<>();
            int i = 0;
            for (; ; ) {
                if (BufferUtils.isEOFPacket(packet) || BufferUtils.isOKPacket(packet)) {
                    skipPacket(1);
                    break;
                }
                FieldPacket fieldPacket = new FieldPacket(packet);
                fieldPacket.decode();
                params.add(fieldPacket.getBody().getName());
                paramTypes[i++] = fieldPacket.getBody().getType();
            }
            message.setParams(params);
            message.setParamTypes(paramTypes);
        }

        if (numColumns > 0) {
            FieldType[] columTypes = new FieldType[numColumns];
            int i = 0;
            List<String> columns = new ArrayList<>();
            for (; ; ) {
                if (BufferUtils.isEOFPacket(packet) || BufferUtils.isOKPacket(packet)) {
                    skipPacket(1);
                    break;
                }
                FieldPacket fieldPacket = new FieldPacket(packet);
                fieldPacket.decode();
                columns.add(fieldPacket.getBody().getName());
                columTypes[i++] = fieldPacket.getBody().getType();
            }
            message.setColumns(columns);
            message.setColumTypes(columTypes);
        }

        return message;
    }

    private void skipPacket(int count) {
        for (int i = 0; i < count; i++) {
            int packetLen = packet.readUnsignedMediumLE();
            packet.skipBytes(1 + packetLen);
        }
    }

    public long getServerStatementId() {
        return serverStatementId;
    }

    public int getNumParams() {
        return numParams;
    }
}
