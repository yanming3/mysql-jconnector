package com.yanming.handler;

import com.yanming.packet.*;
import com.yanming.support.MysqlMessageType;
import com.yanming.in.*;
import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.yanming.support.CapabilitiesFlags.CLIENT_PLUGIN_AUTH;
import static com.yanming.support.CapabilitiesFlags.CLIENT_SECURE_CONNECTION;

/**
 * Created by allan on 16/10/19.
 */
public class MysqlDecoder extends ByteToMessageDecoder {
    private final static Logger logger = LoggerFactory.getLogger(MysqlDecoder.class);

    private enum State {
        DECODE_RESPONSE,
        DECODE_HAND_SHAKE,
    }

    private State state = State.DECODE_HAND_SHAKE;

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
        if (in.readableBytes() < 4) {
            return;
        }

        int packetLength = in.getUnsignedMediumLE(in.readerIndex());
        if (in.readableBytes() < 4 + packetLength) {
            return;
        }
        while (in.isReadable()) {
            switch (state) {
                case DECODE_HAND_SHAKE:
                    HandShakePacket handShakePacket = new HandShakePacket(in);
                    if (!handShakePacket.decode()) {
                        return;
                    }
                    out.add(handShakePacket.getBody());
                    this.state = State.DECODE_RESPONSE;
                    break;

                case DECODE_RESPONSE:
                    if (!decodeResponse(in, out)) {
                        return;
                    }
                    break;
            }
        }
    }


    private boolean decodeResponse(ByteBuf in, List<Object> out) {
        short packetType = in.getUnsignedByte(in.readerIndex() + 4);
        switch (packetType) {
            case 0x00:
                OkPacket okPacket = new OkPacket(in);
                if (okPacket.decode()) {
                    out.add(okPacket.getBody());
                    return true;
                }
                break;
            case 0xfe:
                EofPacket eofPacket = new EofPacket(in);
                if (eofPacket.decode()) {
                    out.add(eofPacket.getBody());
                    return true;
                }
                break;
            case 0xff:
                ErrorPacket errorPacket = new ErrorPacket(in);
                if (errorPacket.decode()) {
                    out.add(errorPacket.getBody());
                    return true;
                }
                break;
            default:
                return decodeOther(in, out);
        }
        return false;
    }

    private boolean decodeOther(ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        int packetLen = in.readUnsignedMediumLE();
        int packetNo = in.readUnsignedByte();
        int startIndex = in.readerIndex();
        int columCount = (int) BufferUtils.readEncodedLenInt(in);
        int readBytes = in.readerIndex() - startIndex;
        if (readBytes < packetLen) {
            in.skipBytes(packetLen - readBytes);
        }

        if (!in.isReadable()) {
            resetReaderIndex(in);
            return false;
        }
        /**
         * 列元数据信息,每个列一个包,以EOF或OK包结束;
         * 列数据,每行一个包,以EOF或OK包结束;
         * 因此当接收到两个EOF(或OK)包之后,表示接收到了一个完成的resultset返回
         */
        int eofPacket = 0;
        while (eofPacket < 2 && in.isReadable()) {
            if (in.readableBytes() > 5) {
                packetLen = in.readUnsignedMediumLE();//3个字节
                packetNo = in.readUnsignedByte();//1个字节
                short flag = in.readUnsignedByte();//1个字节
                if (flag == 0xfe || flag == 0x00) {
                    eofPacket++;
                }
                if (in.readableBytes() >= packetLen - 1) {
                    in.skipBytes(packetLen - 1);
                }
            } else {
                this.resetReaderIndex(in);
                return false;
            }
        }
        this.resetReaderIndex(in);
        LengthPacket lengthPacket=new  LengthPacket(in);
        lengthPacket.decode();

        ResultSetPacket resultSetPacket = new ResultSetPacket(in,lengthPacket.getBody());
        resultSetPacket.decode();
        out.add(resultSetPacket.getBody());
        return true;
    }


    private void resetReaderIndex(ByteBuf in) {
        in.resetReaderIndex();
    }
}
