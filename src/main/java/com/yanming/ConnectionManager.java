package com.yanming;

import com.yanming.handler.MysqlDuplexHandler;
import com.yanming.in.HandShakeMessage;
import com.yanming.handler.MysqlDecoder;
import com.yanming.out.HandshakeResponse;
import com.yanming.support.AuthenticationUtils;
import com.yanming.support.BufferUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yanming.support.CapabilitiesFlags.*;
import static com.yanming.support.CapabilitiesFlags.CLIENT_PLUGIN_AUTH;

/**
 * Created by allan on 16/10/18.
 */
public class ConnectionManager {

    private Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private final Bootstrap b = new Bootstrap();

    private final NioEventLoopGroup group = new NioEventLoopGroup();

    private Promise<Connection> handshakePromise;

    private String host;

    private int port;

    private byte[] user;

    private byte[] passwd;

    private byte[] db;

    private int clientParam;


    public ConnectionManager(final String host, final int port, final String user, final String passwd, final String db, final long timeoutMs) {
        this.host = host;
        this.port = port;
        this.user = BufferUtils.toBytes(user);
        this.passwd = BufferUtils.toBytes(passwd);
        this.db = BufferUtils.toBytes(db);
        assert (this.host != null);
        assert (this.user != null);
        assert (this.passwd != null);
        assert (this.db != null);

        b.group(group).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("logging",new LoggingHandler());
                ch.pipeline().addLast("decoder",new MysqlDecoder(ConnectionManager.this));

                MysqlDuplexHandler duplexHandler = new MysqlDuplexHandler(ConnectionManager.this);
                duplexHandler.setTimeoutMs(timeoutMs);
                ch.pipeline().addLast("duplexHandler",duplexHandler);
            }
        });

    }


    public Future<Connection> connect() {
        ChannelFuture f = b.connect(this.host, this.port);
        this.handshakePromise = f.channel().eventLoop().newPromise();
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    handshakePromise.tryFailure(future.cause());//连接失败
                }
            }
        });
        return this.handshakePromise;
    }

    public void onHandShakeSuccess(Connection conn) {
        this.handshakePromise.trySuccess(conn);
    }

    public void onHandShakeFail(Throwable cause) {
        this.handshakePromise.tryFailure(cause);
    }

    public HandshakeResponse doHandShake(HandShakeMessage packet, ByteBufAllocator allocator, EventExecutor eventExecutor) {
         clientParam = CLIENT_CONNECT_WITH_DB|CLIENT_PLUGIN_AUTH | CLIENT_LONG_PASSWORD | CLIENT_PROTOCOL_41 | CLIENT_TRANSACTIONS // Need this to get server status values
                | CLIENT_MULTI_RESULTS|CLIENT_SECURE_CONNECTION; // We always allow multiple result sets

        if ((packet.getServerCapabilities() & CLIENT_LONG_FLAG) != 0) {
            // We understand other column flags, as well
            clientParam |= CLIENT_LONG_FLAG;
        }
        if ((packet.getServerCapabilities() & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0) {
            clientParam |= CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA;
        }
        if ((packet.getServerCapabilities() & CLIENT_DEPRECATE_EOF) != 0) {
            clientParam |= CLIENT_DEPRECATE_EOF;
        }
       /* if ((packet.getServerCapabilities() & CLIENT_CONNECT_ATTRS) != 0) {
            clientParam |= CLIENT_CONNECT_ATTRS;
        }*/
        if ((packet.getServerCapabilities() & CLIENT_PLUGIN_AUTH) != 0) {
            ByteBuf response = allocator.buffer();
            response.writeIntLE(clientParam);//capability flags, CLIENT_PROTOCOL_41 always set
            response.writeInt((256 * 256 * 256) - 1);//数据包的最大大小
            response.writeByte(33);//字符集
            response.writeBytes(new byte[23]);

            response.writeBytes(this.user);//用户名
            response.writeByte(0x00);

            byte[] authResponse = AuthenticationUtils.getPlugin(packet.getPluginName()).process(this.passwd, packet.getSeed());

            if ((clientParam & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0) {
                // out lenenc-int length of auth-response and string[n] auth-response
                response.writeByte(authResponse.length);
                response.writeBytes(authResponse);
            } else if ((clientParam & CLIENT_SECURE_CONNECTION) != 0) {
                // out 1 byte length of auth-response and string[n] auth-response
                response.writeByte(authResponse.length);
                response.writeBytes(authResponse);
            } else {
                response.writeBytes(authResponse);
                response.writeByte(0x00);
            }
            if ((clientParam & CLIENT_CONNECT_WITH_DB) != 0) {
                response.writeBytes(this.db);
                response.writeByte(0x00);
            }

            if ((clientParam & CLIENT_PLUGIN_AUTH) != 0) {
                response.writeBytes(BufferUtils.toBytes(packet.getPluginName()));
                response.writeByte(0x00);
            }
            return new HandshakeResponse(eventExecutor.newPromise(), 0x01, response);
        }
        return null;
    }

    public boolean isEOFDeprecated(){
        return (this.clientParam& CLIENT_DEPRECATE_EOF) != 0;
    }

    public void close() {
        group.shutdownGracefully();
    }
}