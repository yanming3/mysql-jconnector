package com.yanming.handler;

import com.yanming.ConnectionManager;
import com.yanming.request.AuthRequest;
import com.yanming.request.CommandRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by allan on 16/11/1.
 */
public class MysqlEncoder extends ChannelOutboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(MysqlEncoder.class);

    private ConnectionManager manager;


    public MysqlEncoder(ConnectionManager manager) {
        this.manager = manager;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf sendPacket = ctx.alloc().buffer();
        if (msg instanceof CommandRequest) {
            CommandRequest cmd = (CommandRequest) msg;
            try {
                encode((CommandRequest) msg, sendPacket);
                ctx.write(sendPacket);
            } finally {
                cmd.getData().release();
            }
        } else if (msg instanceof AuthRequest) {
            encode((AuthRequest) msg, sendPacket);
            ctx.write(sendPacket);
        } else {
            throw new RuntimeException("unknown request media type!" + msg);
        }
    }

    protected void encode(CommandRequest msg, ByteBuf out) {
        ByteBuf data = msg.getData();
        out.writeMediumLE(1 + data.readableBytes());
        out.writeByte(msg.getSequenceNo());
        out.writeByte(msg.getCmd().code());
        out.writeBytes(data);
    }

    protected void encode(AuthRequest msg, ByteBuf out) {
        out.writeMediumLE(msg.getBody().readableBytes());
        out.writeByte(msg.getSequenceNo());
        out.writeBytes(msg.getBody());
    }

}
