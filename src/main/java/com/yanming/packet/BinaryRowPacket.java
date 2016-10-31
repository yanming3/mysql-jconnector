package com.yanming.packet;

import com.yanming.support.BufferUtils;
import com.yanming.support.FieldType;
import io.netty.buffer.ByteBuf;

/**
 * Created by allan on 16/10/27.
 */
public class BinaryRowPacket extends MysqlPacket<byte[][]> {

    private int colNums;

    private FieldType[] types;

    public BinaryRowPacket(ByteBuf in, FieldType[] types, int colNums) {
        super(in);
        this.colNums = colNums;
        this.types = types;
    }

    @Override
    byte[][] decodeBody0() {
        byte[][] row = new byte[colNums][];
        packet.skipBytes(1);//00
        int nullCount = (colNums + 9) / 8;
        int nullPosition = packet.readerIndex();
        int bit = 4;//前两个bit为保留位,从第3bit开始判断,如果为1,表示对应字段为null

        packet.skipBytes(nullCount);
        for (int i = 0; i < colNums; i++) {
            if ((packet.getByte(nullPosition) & bit) != 0) {//null
                row[i] = null;
            } else {
                readColumn(i, row);
            }
            if (((bit <<= 1) & 255) == 0) {
                bit = 1;
                nullPosition++;
            }
        }
        return row;
    }

    private void readColumn(int columnIndex, byte[][] row) {
        FieldType t = types[columnIndex];

        switch (t) {
            case NULL:
                break; // for dummy binds

            case TINY:
                row[columnIndex] = getBytes(1);
                break;

            case SHORT:
            case YEAR:
                row[columnIndex] = getBytes(2);
                break;
            case LONG:
            case INT24:
                row[columnIndex] = getBytes(4);
                break;
            case LONGLONG:
                row[columnIndex] = getBytes(8);
                break;
            case FLOAT:
                row[columnIndex] = getBytes(4);
                break;
            case DOUBLE:
                row[columnIndex] = getBytes(8);
                break;
            case TIME:
                int length = (int) packet.readUnsignedByte();
                row[columnIndex] = getLenBytes(length);
                break;
            case DATE:
            case DATETIME:
            case TIMESTAMP:
                length = (int) packet.readUnsignedByte();
                row[columnIndex] = getLenBytes(length);
                break;
            case TINY_BLOB:
            case MEDIUM_BLOB:
            case LONG_BLOB:
            case BLOB:
            case VAR_STRING:
            case VARCHAR:
            case STRING:
            case DECIMAL:
            case NEW_DECIMAL:
            case GEOMETRY:
            case BIT:
                row[columnIndex] = BufferUtils.readEncodedLenBytes(packet);
                break;
            default:
                throw new RuntimeException("unknown field type" + t);

        }
    }

    private byte[] getLenBytes(int len) {
        byte[] column = new byte[len+1];
        column[0]=(byte)len;
        packet.readBytes(column,1,len);
        return column;
    }
    private byte[] getBytes(int len) {
        byte[] column = new byte[len];
        packet.readBytes(column);
        return column;
    }
}