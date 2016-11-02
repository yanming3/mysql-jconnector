package com.yanming.handler;

import com.yanming.parser.EofParser;
import com.yanming.parser.ErrorParser;
import com.yanming.parser.FieldParser;
import com.yanming.parser.OkParser;
import com.yanming.response.EofResponse;
import com.yanming.server.parser.response.*;
import com.yanming.support.BufferUtils;
import com.yanming.support.FieldType;
import com.yanming.support.MysqlField;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.yanming.support.BufferUtils.RESPONSE_ERROR;
import static com.yanming.support.BufferUtils.RESPONSE_OK;

/**
 * 处理prepared,接口文档参见
 * http://dev.mysql.com/doc/internals/en/com-stmt-prepare-response.html
 */
public class PreparedDecoder extends ByteToMessageDecoder {

    enum State {
        PREPARE,
        PARAMETER,
        PARAMETER_EOF,
        COLUMN,
        COLUMN_EOF,
        END_PREPARE,
        RS_HEADER,
        RS_COLUMN,
        RS_COLUMN_EOF,
        RS_DATA,
        RESPONSE
    }

    private State state = State.PREPARE;

    private int expectedParameterPacket = 0, remainingParameterPacket = 0;

    private int expectedColumnPacket = 0, remainingColumnPacket = 0;

    private PreparedResponse preparedResponse;

    private ExecutedResult executedResult;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (true) {
            switch (state) {
                case PREPARE:
                    if (!isReadable(in)) {
                        return;
                    }
                    decodeHeader(in, out);
                    break;
                case PARAMETER:
                    if (!isReadable(in)) {
                        return;
                    }
                    decodeParameter(in, out);
                    break;
                case PARAMETER_EOF:
                    if (!isReadable(in)) {
                        return;
                    }
                    EofParser paraPacket = new EofParser(in, EofResponse.Type.PS_PARAMETER);
                    if (!paraPacket.decode()) {
                        return;
                    }
                    //out.add(paraPacket.getBody());
                    if (expectedColumnPacket > 0) {
                        this.state = State.COLUMN;
                    } else {
                        this.state = State.END_PREPARE;
                    }
                    break;
                case COLUMN:
                    if (!isReadable(in)) {
                        return;
                    }
                    decodeColumn(in, out);
                    break;
                case COLUMN_EOF:
                    if (!isReadable(in)) {
                        return;
                    }
                    EofParser colPacket = new EofParser(in, EofResponse.Type.PS_COLUMN);
                    if (!colPacket.decode()) {
                        return;
                    }
                    this.state = State.END_PREPARE;
                    break;
                case END_PREPARE:
                    out.add(preparedResponse);
                    this.preparedResponse = null;
                    this.state = State.RESPONSE;
                    return;
                case RESPONSE:
                    if (!isReadable(in)) {
                        return;
                    }
                    if (decodeResponse(in, out)) {
                        return;
                    }
                    break;
                case RS_HEADER:
                    if (!isReadable(in)) {
                        return;
                    }
                    decodeRsHeader(in, out);
                    return;
                case RS_COLUMN:
                    if (!isReadable(in)) {
                        return;
                    }
                    decodeRsColumn(in, out);
                    return;
                case RS_COLUMN_EOF:
                    if (!isReadable(in)) {
                        return;
                    }
                    EofParser rsColPacket = new EofParser(in, EofResponse.Type.PS_COLUMN);
                    if (!rsColPacket.decode()) {
                        return;
                    }
                    this.state = State.RS_DATA;
                    break;
                case RS_DATA:
                    if (!isReadable(in)) {
                        return;
                    }
                    if (BufferUtils.isEOFPacket(in)) {
                        EofParser rowPacket = new EofParser(in, EofResponse.Type.ROW);
                        if (!rowPacket.decode()) {
                            return;
                        }
                        out.add(executedResult);
                        this.executedResult = null;
                        this.state = State.PREPARE;

                        this.expectedParameterPacket = this.expectedColumnPacket = 0;
                        return;
                    }
                    decodeData(in, out);
                    return;
            }
        }

    }

    private boolean decodeResponse(ByteBuf in, List<Object> out) {
        short packetType = in.getUnsignedByte(in.readerIndex() + 4);
        switch (packetType) {
            case RESPONSE_OK:
                OkParser okParser = new OkParser(in);
                if (okParser.decode()) {
                    out.add(okParser.getBody());
                    this.state=State.PREPARE;
                    return true;
                }

                break;

            case RESPONSE_ERROR:
                ErrorParser errorParser = new ErrorParser(in);
                if (errorParser.decode()) {
                    out.add(errorParser.getBody());
                    this.state=State.PREPARE;
                    return true;
                }
                break;
            default:
                this.state = State.RS_HEADER;
                break;

        }
        return false;
    }

    private void decodeHeader(ByteBuf in, List<Object> out) {
        int packetLen = in.readUnsignedMediumLE();
        int packetNo = in.readUnsignedByte();
        in.skipBytes(1);//0x00,一个字节
        long serverStatementId = in.readUnsignedIntLE();//statement id,4 bytes
        this.expectedColumnPacket = this.remainingColumnPacket = in.readUnsignedShortLE();
        this.expectedParameterPacket = this.remainingParameterPacket = in.readUnsignedShortLE();
        in.skipBytes(1);//[00] filler,保留字
        int warnings = in.readUnsignedShortLE();

        this.preparedResponse = new PreparedResponse(packetLen, packetNo, serverStatementId, expectedColumnPacket, expectedParameterPacket);
        if (expectedParameterPacket > 0) {
            this.state = State.PARAMETER;
        } else if (expectedColumnPacket > 0) {
            this.state = State.COLUMN;
        } else {
            this.state = State.END_PREPARE;
        }
    }

    private void decodeParameter(ByteBuf in, List<Object> out) {
        FieldParser fieldParser = new FieldParser(in, 0);
        fieldParser.decode();
        MysqlField field = fieldParser.getBody().getField();
        preparedResponse.addParameter(field);
        this.remainingParameterPacket--;
        if (this.remainingParameterPacket == 0) {
            this.state = State.PARAMETER_EOF;
        }
    }

    private void decodeColumn(ByteBuf in, List<Object> out) {
        FieldParser fieldParser = new FieldParser(in, 0);
        fieldParser.decode();
        MysqlField field = fieldParser.getBody().getField();
        preparedResponse.addColumn(field);
        this.remainingColumnPacket--;
        if (this.remainingColumnPacket == 0) {
            this.state = State.COLUMN_EOF;
        }
    }

    private void decodeRsHeader(ByteBuf in, List<Object> out) {
        int packetLen = in.readUnsignedMediumLE();
        int packetNo = in.readUnsignedByte();
        this.expectedColumnPacket = this.remainingColumnPacket = (int) BufferUtils.readEncodedLenInt(in);
        this.executedResult = new ExecutedResult(packetLen, packetNo, expectedColumnPacket);
        this.state = State.RS_COLUMN;
    }

    private void decodeRsColumn(ByteBuf in, List<Object> out) {
        FieldParser fieldParser = new FieldParser(in, 0);
        fieldParser.decode();
        MysqlField field = fieldParser.getBody().getField();
        executedResult.addColumn(field);
        this.remainingColumnPacket--;
        if (this.remainingColumnPacket == 0) {
            this.state = State.RS_COLUMN_EOF;
        }
    }

    private void decodeData(ByteBuf in, List<Object> out) {
        in.skipBytes(4);
        byte[][] row = new byte[expectedColumnPacket][];
        in.skipBytes(1);//00
        int nullCount = (expectedColumnPacket + 9) / 8;
        int nullPosition = in.readerIndex();
        int bit = 4;//前两个bit为保留位,从第3bit开始判断,如果为1,表示对应字段为null

        in.skipBytes(nullCount);
        for (int i = 0; i < expectedColumnPacket; i++) {
            if ((in.getByte(nullPosition) & bit) != 0) {//null
                row[i] = null;
            } else {
                readColumn(in, i, row);
            }
            if (((bit <<= 1) & 255) == 0) {
                bit = 1;
                nullPosition++;
            }
        }
        executedResult.addRecord(row);
    }

    /**
     * 是否有可读的包
     *
     * @param in
     * @return
     */
    private boolean isReadable(ByteBuf in) {
        if (in.readableBytes() < 4) {
            return false;
        }

        int packetLength = in.getUnsignedMediumLE(in.readerIndex());
        if (in.readableBytes() < 4 + packetLength) {
            return false;
        }
        return true;
    }

    private void readColumn(ByteBuf in, int columnIndex, byte[][] row) {
        FieldType t = executedResult.getColumns().get(columnIndex).getColumnType();

        switch (t) {
            case NULL:
                break; // for dummy binds

            case TINY:
                row[columnIndex] = BufferUtils.readBytes(in, 1);
                break;

            case SHORT:
            case YEAR:
                row[columnIndex] = BufferUtils.readBytes(in, 2);
                break;
            case LONG:
            case INT24:
                row[columnIndex] = BufferUtils.readBytes(in, 4);
                break;
            case LONGLONG:
                row[columnIndex] = BufferUtils.readBytes(in, 8);
                break;
            case FLOAT:
                row[columnIndex] = BufferUtils.readBytes(in, 4);
                break;
            case DOUBLE:
                row[columnIndex] = BufferUtils.readBytes(in, 8);
                break;
            case TIME:
                int length = (int) in.readUnsignedByte();
                row[columnIndex] = BufferUtils.readLenBytes(in, length);
                break;
            case DATE:
            case DATETIME:
            case TIMESTAMP:
                length = (int) in.readUnsignedByte();
                row[columnIndex] = BufferUtils.readLenBytes(in, length);
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
                row[columnIndex] = BufferUtils.readEncodedLenBytes(in);
                break;
            default:
                throw new RuntimeException("unknown field type" + t);

        }
    }


    protected void waitResponse() {
        this.state = State.RESPONSE;
    }
}
