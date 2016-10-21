package com.yanming.packet;

import io.netty.buffer.ByteBuf;


/**
 * Created by allan on 16/10/20.
 */
public abstract class MysqlPacket<T> {
    protected ByteBuf packet;
    protected int packetLen;
    protected int packetNo;
    private int startIndex;

    private T body;

    public MysqlPacket(ByteBuf packet) {
        this.packet = packet;
    }

    void decodeHeader() {
        if (isDecodeHeader()) {
            this.packetLen = packet.readUnsignedMediumLE();
            this.packetNo = packet.readUnsignedByte();
            this.startIndex = packet.readerIndex();
        }
    }


    void decodeBody() {
        this.body = decodeBody0();
        int readBytes = packet.readerIndex() - startIndex;
        if (readBytes < packetLen) {
            packet.skipBytes(packetLen - readBytes);
        }
    }

    public boolean decode() {
        decodeHeader();
        decodeBody();
        return true;
    }

    abstract T decodeBody0();

    boolean isDecodeHeader() {
        return true;
    }

    public int getPacketLen() {
        return packetLen;
    }

    public int getPacketNo() {
        return packetNo;
    }

    public T getBody() {
        return body;
    }
}
