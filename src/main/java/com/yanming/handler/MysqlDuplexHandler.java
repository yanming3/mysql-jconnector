package com.yanming.handler;

import com.yanming.Connection;
import com.yanming.ConnectionManager;
import com.yanming.exception.ConnectionException;
import com.yanming.exception.MysqlResponseException;
import com.yanming.in.*;
import com.yanming.out.CommandMessage;
import com.yanming.out.HandshakeResponse;
import com.yanming.packet.PreparedStatementPacket;
import com.yanming.support.Command;
import com.yanming.support.FieldType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * Created by allan on 16/10/18.
 */
public class MysqlDuplexHandler extends ChannelDuplexHandler {

    private Logger logger = LoggerFactory.getLogger(MysqlDuplexHandler.class);

    private static final class Entry {

        public final Promise<Object> promise;

        public final int command;

        public final long milliTime;

        public Entry(int command, Promise<Object> promise, long milliTime) {
            this.command = command;
            this.promise = promise;
            this.milliTime = milliTime;
        }

        public Entry(Promise<Object> promise, long milliTime) {
            this.command = -1;
            this.promise = promise;
            this.milliTime = milliTime;
        }
    }

    private final Deque<Entry> entryQ = new ArrayDeque<>();

    private ConnectionManager connectionManager;

    private String encoding = "UTF-8";

    private long timeoutMs;

    private ScheduledFuture<?> timeoutTask;

    private boolean handShaked = false;

    public MysqlDuplexHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    private void scheduleTimeoutTask(ChannelHandlerContext ctx) {
        if (timeoutMs > 0 && timeoutTask == null) {
            timeoutTask = ctx.executor().schedule(new TimeoutTask(ctx), timeoutMs,
                    TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof CommandMessage) {
            CommandMessage message = (CommandMessage) msg;
            ByteBuf data = message.getData();
            try {
                entryQ.addLast(new Entry(message.getCommand(), message.getPromise(), System.currentTimeMillis()));
                ByteBuf sendPacket = ctx.alloc().buffer();
                sendPacket.writeMediumLE(1 + data.readableBytes());
                sendPacket.writeByte(message.getSequenceNo());
                sendPacket.writeByte(message.getCommand());
                sendPacket.writeBytes(data);
                int command = message.getCommand();
                if (command == Command.STMT_PREPARE.code()) {
                    MysqlDecoder decoder = (MysqlDecoder) ctx.pipeline().get("decoder");
                    decoder.startPrepared();
                }
                ctx.write(sendPacket);
            } finally {
                data.release();
            }
        } else if (msg instanceof HandshakeResponse) {
            HandshakeResponse handshakeResponse = (HandshakeResponse) msg;
            ByteBuf sendPacket = ctx.alloc().buffer();
            sendPacket.writeMediumLE(handshakeResponse.getBody().readableBytes());
            sendPacket.writeByte(handshakeResponse.getSequenceNo());
            sendPacket.writeBytes(handshakeResponse.getBody());
            ctx.write(sendPacket);
        } else {
            throw new RuntimeException("unknown request media type!" + msg);
        }
        scheduleTimeoutTask(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Entry entry = entryQ.pollFirst();

        if (msg instanceof OKMessage) {
            if (!handShaked) {
                this.handShaked = true;
                this.connectionManager.onHandShakeSuccess(new Connection(ctx.channel()));
            } else {
                if (entry != null) {
                    OKMessage ok=(OKMessage)msg;
                    entry.promise.trySuccess(ok.getAffectedRows());
                }
            }
        } else if (msg instanceof ErrorMessage) {
            ErrorMessage errorMessage = (ErrorMessage) msg;
            if (!handShaked) {
                Throwable cause = new ConnectionException(String.format("ErrorCode:%d:%s", errorMessage.getErrCode(), errorMessage.getErrMsg()));
                connectionManager.onHandShakeFail(cause);
            } else if (entry != null) {
                Throwable cause = new MysqlResponseException(String.format("ErrorCode:%d:%s", errorMessage.getErrCode(), errorMessage.getErrMsg()));
                entry.promise.tryFailure(cause);
            }

        } else if (msg instanceof EOFMessage) {
            if(entry!=null){
                entry.promise.trySuccess(true);
            }
        } else if (msg instanceof HandShakeMessage) {
            HandShakeMessage packet = (HandShakeMessage) msg;
            HandshakeResponse response = connectionManager.doHandShake(packet, ctx.alloc(), ctx.executor());
            ctx.channel().writeAndFlush(response);
        } else if (msg instanceof ResultSetMessage) {
            ResultSetMessage rs = (ResultSetMessage) msg;
            entry.promise.trySuccess(rs.records());
        }
        else if (msg instanceof BinaryResultSetMessage) {
            BinaryResultSetMessage rs = (BinaryResultSetMessage) msg;
            entry.promise.trySuccess(rs);

        }else if (msg instanceof PreparedStatementMessage) {
            entry.promise.trySuccess(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!entryQ.isEmpty()) {
            failAll(new ClosedChannelException());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Error found in MysqlDuplexHandler!", cause);
        failAll(cause);
        ctx.channel().close();
    }

    private void failAll(Throwable cause) {
        for (Entry entry; (entry = entryQ.pollFirst()) != null; ) {
            entry.promise.tryFailure(cause);
        }
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    private final class TimeoutTask implements Runnable {

        private final ChannelHandlerContext ctx;

        public TimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (entryQ.isEmpty() || timeoutMs <= 0) {
                timeoutTask = null;
                return;
            }

            long nextDelayMs = timeoutMs - (System.currentTimeMillis() - entryQ.peek().milliTime);
            if (nextDelayMs <= 0) {
                exceptionCaught(ctx, ReadTimeoutException.INSTANCE);
            } else {
                timeoutTask = ctx.executor().schedule(this, nextDelayMs, TimeUnit.MILLISECONDS);
            }
        }

    }
}
