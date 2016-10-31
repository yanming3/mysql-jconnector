package com.yanming.packet;

import com.yanming.ColumnInfo;
import com.yanming.support.BufferUtils;
import com.yanming.support.FieldType;
import io.netty.buffer.ByteBuf;

/**
 * Created by allan on 16/10/25.
 */
public class FieldPacket extends MysqlPacket<ColumnInfo> {
    public FieldPacket(ByteBuf in) {
        super(in);
    }

    @Override
    ColumnInfo decodeBody0() {
        ColumnInfo info = new ColumnInfo();
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
        info.setName(name);
        info.setType(FieldType.valueOf(type));
        return info;
    }
}
