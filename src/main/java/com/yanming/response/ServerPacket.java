package com.yanming.response;

/**
 * Created by allan on 16/10/31.
 */
public abstract class ServerPacket {
    private final int packetLength;
    private final int packetNumber;

    public ServerPacket(int packetLength, int packetNumber) {
        this.packetLength = packetLength;
        this.packetNumber = packetNumber;
    }

    public int getPacketLength() {
        return packetLength;
    }

    public int getPacketNumber() {
        return packetNumber;
    }
}
