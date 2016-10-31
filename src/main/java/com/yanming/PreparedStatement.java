package com.yanming;

import com.yanming.in.BinaryResultSetMessage;
import com.yanming.support.BufferUtils;
import com.yanming.support.Command;
import com.yanming.support.FieldType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by allan on 16/10/27.
 */
public class PreparedStatement {

    private final Connection conn;

    private final long serverStatementId;

    private final Object[] params;

    private final FieldType[] paramTypes;

    private final FieldType[] columTypes;

    private final int numParams;

    private final int sequenceNo;

    private byte[][] result;

    public PreparedStatement(Connection conn, long serverStatementId, int numParams, int sequenceNo, FieldType[] paramTypes, FieldType[] columTypes) {
        this.conn = conn;
        this.serverStatementId = serverStatementId;
        this.numParams = numParams;
        this.sequenceNo = sequenceNo;
        this.params = new Object[numParams];
        this.paramTypes = paramTypes;
        this.columTypes = columTypes;
    }

    public Future<Long> executeUpdate() {
        ByteBuf data = BufferUtils.newBuffer();
        data.writeIntLE((int) serverStatementId);//statement id
        data.writeByte(0x00);//flags
        data.writeIntLE(0x01);

         /* Reserve place for null-marker bytes */
        int parameterCount = params.length;
        int nullCount = (parameterCount + 7) / 8;
        int nullPosition = data.writerIndex();
        data.writeZero(nullCount);
        data.writeByte(0x01);//不发送类型信息到服务器

        byte[] nullBitsBuffer = new byte[nullCount];

        for (int i = 0; i < parameterCount; i++) {
            data.writeShortLE(paramTypes[i].code());
        }
        for (int i = 0; i < parameterCount; i++) {
            if (params[i] != null) {
                storeBinding(paramTypes[i], params[i], data);
            } else {
                nullBitsBuffer[i / 8] |= (1 << (i & 7));
            }
        }

        data.markWriterIndex();

        data.writerIndex(nullPosition);
        data.writeBytes(nullBitsBuffer);


        data.resetWriterIndex();

        final Promise<Long> p = conn.newPromise();
        conn.execCommand0(Command.STMT_EXECUTE.code(), data, 0, null).addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                if (future.isSuccess()) {
                    long affectdRows = (long) future.getNow();
                    p.trySuccess(affectdRows);
                } else {
                    p.tryFailure(future.cause());
                }
            }
        });
        return p;
    }

    public Future<List<String[]>> executeQuery() {
        ByteBuf data = BufferUtils.newBuffer();
        data.writeIntLE((int) serverStatementId);//statement id
        data.writeByte(0x00);//flags
        data.writeIntLE(0x01);

         /* Reserve place for null-marker bytes */
        int parameterCount = params.length;
        int nullCount = (parameterCount + 7) / 8;
        int nullPosition = data.writerIndex();
        data.writeZero(nullCount);
        data.writeByte(0x01);//不发送类型信息到服务器

        byte[] nullBitsBuffer = new byte[nullCount];

        for (int i = 0; i < parameterCount; i++) {
            data.writeShortLE(paramTypes[i].code());
        }
        for (int i = 0; i < parameterCount; i++) {
            if (params[i] != null) {
                storeBinding(paramTypes[i], params[i], data);
            } else {
                nullBitsBuffer[i / 8] |= (1 << (i & 7));
            }
        }

        data.markWriterIndex();

        data.writerIndex(nullPosition);
        data.writeBytes(nullBitsBuffer);


        data.resetWriterIndex();

        final Promise<List<String[]>> p = conn.newPromise();
        conn.execCommand0(Command.STMT_EXECUTE.code(), data, 0, null).addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                if (future.isSuccess()) {
                    List<String[]> result = new ArrayList<>();
                    BinaryResultSetMessage message = (BinaryResultSetMessage) future.getNow();
                    for (byte[][] record : message.getData()) {
                        String[] row = new String[message.getColumn().size()];
                        for (int i = 0; i < message.getColumn().size(); i++) {
                            if (record[i] == null) {
                                row[i] = null;
                            } else {
                                readColumn(record, i, row);
                            }
                        }
                        result.add(row);
                    }
                    p.trySuccess(result);
                } else {
                    p.tryFailure(future.cause());
                }
            }
        });
        return p;
    }

    private void storeBinding(FieldType t, Object value, ByteBuf packet) {
        switch (t) {

            case TINY:
                packet.writeByte((byte) value);
                return;
            case SHORT:
                packet.writeShortLE((int) value);
                return;
            case LONG:
                packet.writeIntLE((int) value);
                return;
            case LONGLONG:
                packet.writeLongLE((long) value);
                return;
            case FLOAT:
                float fvalue = (float) value;
                packet.writeIntLE(Float.floatToIntBits(fvalue));
                return;
            case DOUBLE:
                double dvalue = (double) value;
                packet.writeLongLE(Double.doubleToLongBits(dvalue));
                return;
            case TIME:
                storeTime(packet, (Time) value);
                return;
            case DATE:
            case DATETIME:
            case TIMESTAMP:
                storeDateTime(packet, (java.util.Date) value);
                return;
            case VAR_STRING:
            case STRING:
            case VARCHAR:
            case DECIMAL:
            case NEW_DECIMAL:
                if (value instanceof byte[]) {
                    BufferUtils.writeLenBytes(packet, (byte[]) value);
                } else {
                    BufferUtils.writeLenString(packet, (String) value);
                }

                return;
        }
    }

    /**
     * http://dev.mysql.com/doc/internals/en/binary-protocol-value.html#packet-ProtocolBinary::MYSQL_TYPE_TIME
     *
     * @param packet
     * @param value
     */
    private void storeTime(ByteBuf packet, Time value) {
        packet.writeByte(0x08);//长度为8
        packet.writeByte(0x00);
        packet.writeZero(4);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(value);
        packet.writeByte(calendar.get(Calendar.HOUR_OF_DAY));
        packet.writeByte(calendar.get(Calendar.MINUTE));
        packet.writeByte(calendar.get(Calendar.SECOND));
    }

    private void storeDateTime(ByteBuf packet, Date value) {
        byte len = 7;
        if (value instanceof Timestamp) {
            len = 11;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(value);
        packet.writeByte(len);
        packet.writeShortLE(calendar.get(Calendar.YEAR));
        packet.writeByte(calendar.get(Calendar.MONTH) + 1);
        packet.writeByte(calendar.get(Calendar.DAY_OF_MONTH));
        packet.writeByte(calendar.get(Calendar.HOUR_OF_DAY));
        packet.writeByte(calendar.get(Calendar.MINUTE));
        packet.writeByte(calendar.get(Calendar.SECOND));
        if (len == 11) {
            packet.writeIntLE(((Timestamp) value).getNanos() / 1000);
        }
    }


    public void setInt(int parameterIndex, int x) {
        if (parameterIndex > this.numParams - 1) {
            throw new IndexOutOfBoundsException();
        }
        this.params[parameterIndex] = x;
        this.paramTypes[parameterIndex] = FieldType.LONG;
    }

    public void setString(int parameterIndex, String x) {
        if (parameterIndex > this.numParams - 1) {
            throw new IndexOutOfBoundsException();
        }
        this.params[parameterIndex] = x;
        this.paramTypes[parameterIndex] = FieldType.VAR_STRING;
    }

    private void readColumn(byte[][] result, int columnIndex, String[] row) {
        FieldType t = columTypes[columnIndex];

        byte[] columnBytes = result[columnIndex];
        ByteBuf packet = Unpooled.wrappedBuffer(columnBytes);
        switch (t) {
            case NULL:
                break; // for dummy binds

            case TINY:
                row[columnIndex] = String.valueOf(packet.readUnsignedByte());
                break;

            case SHORT:
            case YEAR:
                row[columnIndex] = String.valueOf(packet.readUnsignedShortLE());
                break;
            case LONG:
            case INT24:
                row[columnIndex] = String.valueOf(packet.readUnsignedIntLE());
                break;
            case LONGLONG:
                row[columnIndex] = String.valueOf(packet.readLongLE());
                break;
            case FLOAT:
                float f = Float.intBitsToFloat((int) packet.readUnsignedIntLE());
                row[columnIndex] = String.valueOf(f);
                break;
            case DOUBLE:
                double d = Double.longBitsToDouble(packet.readLongLE());
                row[columnIndex] = String.valueOf(d);
                break;
            case TIME:
                int length = (int) packet.readUnsignedByte();
                if (length == 0) {
                    row[columnIndex] = null;
                    break;
                }
                packet.skipBytes(1);//is_negative (1) -- (1 if minus, 0 for plus)
                packet.skipBytes(4);
                int hours = packet.readUnsignedByte();
                int minutes = packet.readUnsignedByte();
                int seconds = packet.readUnsignedByte();
                if (length == 12) {
                    long microSec = packet.readUnsignedIntLE();
                    row[columnIndex] = String.format("%d:%d:%d.%d", hours, minutes, seconds, microSec);
                } else {
                    row[columnIndex] = String.format("%d:%d:%d", hours, minutes, seconds);
                }
                break;
            case DATE:
            case DATETIME:
            case TIMESTAMP:
                length = (int) packet.readUnsignedByte();
                if (length == 0) {
                    row[columnIndex] = null;
                    break;
                }
                int year = packet.readUnsignedShortLE();
                int month = packet.readUnsignedByte();
                int day = packet.readUnsignedByte();
                if (length == 4) {
                    row[columnIndex] = String.format("%d-%d-%d 00:00:00", year, month, day);
                    break;
                } else {
                    hours = packet.readUnsignedByte();
                    minutes = packet.readUnsignedByte();
                    seconds = packet.readUnsignedByte();
                    row[columnIndex] = String.format("%d-%d-%d %d:%d:%d", year, month, day, hours, minutes, seconds);
                }

                if (length == 11) {
                    long microSec = packet.readUnsignedIntLE();
                    row[columnIndex] = row[columnIndex] + "." + microSec;
                }

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
                row[columnIndex] = BufferUtils.readEncodedLenString(packet);
                break;
            default:
                throw new RuntimeException("unknown field type" + t);

        }
    }
}