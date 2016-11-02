package com.yanming.handler;

import com.yanming.ConnectionManager;
import com.yanming.response.EofResponse;
import com.yanming.resultset.ResultSetResponse;
import com.yanming.parser.*;
import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yanming.support.BufferUtils.*;

import java.util.List;

/**
 * Created by allan on 16/10/19.
 */
public class MysqlDecoder extends ByteToMessageDecoder {
    private final static Logger logger = LoggerFactory.getLogger(MysqlDecoder.class);

    enum State {
        /**
         * The client is connecting
         */
        CONNECTING,
        RESPONSE,
        FIELD,
        FIELD_EOF,
        ROW
    }


    private State state = State.CONNECTING;

    private ConnectionManager manager;

    private int expectedFieldPackets = 0, remainingFieldPackets = 0;


    public MysqlDecoder(ConnectionManager manager) {
        this.manager = manager;
    }

    /**
     * 包的格式为:包体长度(3个字节)+序号(1个字节)+包体
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (isReadable(in)) {
            switch (state) {
                case CONNECTING:
                    HandShakeParser handShakeParser = new HandShakeParser(in);
                    if (!handShakeParser.decode()) {
                        return;
                    }
                    out.add(handShakeParser.getBody());
                    this.state = State.RESPONSE;
                    break;

                case RESPONSE:
                    if (!decodeResponse(in, out)) {
                        return;
                    }
                    break;
                case FIELD:
                    FieldParser packet = new FieldParser(in, 0);
                    packet.decode();
                    out.add(packet.getBody());

                    this.remainingFieldPackets--;
                    if (this.remainingFieldPackets == 0) {
                        this.state = State.FIELD_EOF;
                    }
                    return;
                case FIELD_EOF:
                    EofParser fieldPacket = new EofParser(in, EofResponse.Type.FIELD);
                    if (!fieldPacket.decode()) {
                        return;
                    }
                    out.add(fieldPacket.getBody());
                    this.state = State.ROW;
                    return;
                case ROW:
                    if (BufferUtils.isEOFPacket(in)) {
                        EofParser rowPacket = new EofParser(in, EofResponse.Type.ROW);
                        if (!rowPacket.decode()) {
                            return;
                        }
                        out.add(rowPacket.getBody());
                        this.state = State.RESPONSE;
                        this.expectedFieldPackets = 0;
                        return;
                    }
                    RowParser rowParser = new RowParser(in, this.expectedFieldPackets);
                    if (!rowParser.decode()) {
                        return;
                    }
                    out.add(rowParser.getBody());
                    return;
                default:
                    throw new IllegalStateException("Unkown decoder state " + state);
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
                    return true;
                }


                break;

            case RESPONSE_ERROR:
                ErrorParser errorParser = new ErrorParser(in);
                if (errorParser.decode()) {
                    out.add(errorParser.getBody());
                    return true;
                }
                break;
            default:
                return decodeResultSet(in, out);
        }
        return false;
    }


    /**
     * 接口规范请参考
     * http://dev.mysql.com/doc/internals/en/com-query-response.html#packet-ProtocolText::Resultset
     *
     * @param in
     * @param out
     * @return
     */
    private boolean decodeResultSet(ByteBuf in, List<Object> out) {
        int packetLen = in.readUnsignedMediumLE();
        int packetNo = in.readUnsignedByte();

        this.expectedFieldPackets = (int) BufferUtils.readEncodedLenInt(in);
        this.remainingFieldPackets = this.expectedFieldPackets;
        state = State.FIELD;

        out.add(new ResultSetResponse(packetLen, packetNo, this.expectedFieldPackets));
        return true;
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
}
