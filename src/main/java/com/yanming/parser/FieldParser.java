package com.yanming.parser;

import com.yanming.support.FieldFlag;
import com.yanming.support.MysqlField;
import com.yanming.support.MysqlCharacterSet;
import com.yanming.resultset.ResultSetFieldResponse;
import com.yanming.support.BufferUtils;
import com.yanming.support.FieldType;
import io.netty.buffer.ByteBuf;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by allan on 16/10/25.
 */
public class FieldParser extends MysqlPacketParser<ResultSetFieldResponse> {
    private int index;

    public FieldParser(ByteBuf in, int index) {
        super(in);
        this.index = index;
    }

    @Override
    public ResultSetFieldResponse decodeBody0() {

        String catalogName = BufferUtils.readEncodedLenString(packet);
        String schemaName = BufferUtils.readEncodedLenString(packet);
        String tableLabel = BufferUtils.readEncodedLenString(packet);
        String tableName = BufferUtils.readEncodedLenString(packet);
        String columnLabel = BufferUtils.readEncodedLenString(packet);
        String columnName = BufferUtils.readEncodedLenString(packet);

        packet.skipBytes(1);//length of fixed-length fields [0c]
        int characterSetIndex = packet.readUnsignedShortLE();
        MysqlCharacterSet charSet = MysqlCharacterSet.findById(characterSetIndex);

        int columnLength = (int) packet.readUnsignedIntLE();
        int columnTypeId = packet.readUnsignedByte();
        FieldType columnType = FieldType.valueOf(columnTypeId);

        Set<FieldFlag> flags = toEnumSet(packet.readUnsignedShortLE());
        int colDecimals = packet.readUnsignedByte();
        packet.skipBytes(2);//filler [00] [00]

        MysqlField field = new MysqlField(index, catalogName, schemaName, tableLabel, tableName, columnType, columnLabel,
                columnName, columnLength, flags, colDecimals);
        ResultSetFieldResponse response = new ResultSetFieldResponse(packetLen, packetNo, field);
        return response;
    }

    private EnumSet<FieldFlag> toEnumSet(long vector) {
        EnumSet<FieldFlag> set = EnumSet.noneOf(FieldFlag.class);
        for (FieldFlag e : FieldFlag.values()) {
            if ((e.code() & vector) > 0) {
                set.add(e);
            }
        }
        return set;
    }
}
