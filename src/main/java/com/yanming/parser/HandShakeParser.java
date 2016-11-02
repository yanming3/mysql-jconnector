package com.yanming.parser;

import com.yanming.response.ServerGreeting;
import com.yanming.support.BufferUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import static com.yanming.support.CapabilitiesFlags.CLIENT_PLUGIN_AUTH;
import static com.yanming.support.CapabilitiesFlags.CLIENT_SECURE_CONNECTION;

/**
 * Created by allan on 16/10/20.
 */
public class HandShakeParser extends MysqlPacketParser<ServerGreeting> {

    private Logger logger = LoggerFactory.getLogger(HandShakeParser.class);

    private ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

    public HandShakeParser(ByteBuf in) {
        super(in);
    }

    @Override
    public ServerGreeting decodeBody0() {
        String pluginName = "";
        ByteBuf seed = null;
        ByteBuf buffer = null;

        int serverCapabilities;
        try {
            buffer = allocator.heapBuffer(20);
            seed = allocator.heapBuffer(20);
            short protocolVersion = packet.readUnsignedByte();
            BufferUtils.readNullString(packet, buffer);
            logger.debug("Mysql server version is {}", buffer.toString(Charset.forName("ASCII")));

            int threadId = packet.readIntLE();

            if (protocolVersion > 9) {
                packet.readBytes(seed, 8);
                packet.skipBytes(1);//filter:00
            } else {
                BufferUtils.readNullString(packet, seed);
            }
            serverCapabilities = packet.readUnsignedShortLE();//低两位

            if (packet.isReadable()) {//如果还有可读数据
                int serverCharsetIndex = packet.readUnsignedByte();
                int serverStatus = packet.readUnsignedShortLE();

                serverCapabilities |= packet.readUnsignedShortLE() << 16;//读取高两位


                short authPluginDataLength = 0;
                if ((serverCapabilities & CLIENT_PLUGIN_AUTH) != 0) {
                    authPluginDataLength = packet.readUnsignedByte();
                } else {
                    // read filler ([00])
                    packet.skipBytes(1);
                }
                packet.skipBytes(10);//忽略10个长度的保留字符00


                if ((serverCapabilities & CLIENT_SECURE_CONNECTION) != 0) {
                    buffer.clear();
                    // read string[$len] auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
                    BufferUtils.readNullString(packet, buffer);
                    seed.writeBytes(buffer);
                }


                if ((serverCapabilities & CLIENT_PLUGIN_AUTH) != 0) {
                    buffer.clear();
                    BufferUtils.readNullString(packet, buffer);
                    pluginName = buffer.toString(Charset.forName("utf-8"));
                }
            }
            ServerGreeting serverGreeting = new ServerGreeting(serverCapabilities, pluginName, ByteBufUtil.getBytes(seed));
            return serverGreeting;
        } finally {
            if (seed != null) {
                seed.release();
            }
            if (buffer != null) {
                buffer.release();
            }
        }
    }
}
