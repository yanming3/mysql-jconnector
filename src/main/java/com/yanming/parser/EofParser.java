package com.yanming.parser;

import com.yanming.response.EofResponse;
import com.yanming.support.ServerStatus;
import io.netty.buffer.ByteBuf;

import java.util.EnumSet;

/**
 * 官方接口文档:http://dev.mysql.com/doc/internals/en/packet-EOF_Packet.html
 */
public class EofParser extends MysqlPacketParser<EofResponse> {

    private EofResponse.Type type;

    public EofParser(ByteBuf in, EofResponse.Type type) {
        super(in);
        this.type = type;
    }

    @Override
    public EofResponse decodeBody0() {
        packet.skipBytes(1);//0xFE
        int warnings = packet.readUnsignedShortLE();//2个字节
        int statusFlags = packet.readUnsignedShortLE();//状态码
        EnumSet<ServerStatus> statuses = toEnumSet(statusFlags);
        return new EofResponse(this.packetLen, this.packetNo, warnings, statuses, type);
    }


    private EnumSet<ServerStatus> toEnumSet(long vector) {
        EnumSet<ServerStatus> set = EnumSet.noneOf(ServerStatus.class);
        for (ServerStatus e : ServerStatus.values()) {
            if ((e.code() & vector) > 0) {
                set.add(e);
            }
        }
        return set;
    }
}
