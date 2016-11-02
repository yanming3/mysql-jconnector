package com.yanming.handler;

import com.yanming.Connection;
import com.yanming.ConnectionManager;
import com.yanming.exception.ConnectionException;
import com.yanming.exception.MysqlResponseException;
import com.yanming.request.ClientRequest;
import com.yanming.response.*;
import com.yanming.request.AuthRequest;
import com.yanming.request.CommandRequest;
import com.yanming.resultset.DefaultResultSet;
import com.yanming.server.parser.response.ExecutedResult;
import com.yanming.server.parser.response.PreparedResponse;
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

        public final ClientRequest clientRequest;

        public final long milliTime;

        public Entry(ClientRequest clientRequest, Promise<Object> promise, long milliTime) {
            this.clientRequest = clientRequest;
            this.promise = promise;
            this.milliTime = milliTime;
        }


    }

    private final Deque<Entry> entryQ = new ArrayDeque<>();

    private long timeoutMs;

    private ScheduledFuture<?> timeoutTask;

    private ConnectionManager connectionManager;

    public MysqlDuplexHandler(ConnectionManager connectionManager, long timeoutMs) {
        this.connectionManager = connectionManager;
        this.timeoutMs = timeoutMs;
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof CommandRequest) {
            CommandRequest cr = (CommandRequest) msg;
            entryQ.addLast(new Entry(cr, cr.getPromise(), System.currentTimeMillis()));
        }
        ctx.write(msg);
        scheduleTimeoutTask(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof OkResponse) {
            Entry entry = entryQ.pollFirst();
            if (entry != null) {
                OkResponse ok = (OkResponse) msg;
                entry.promise.trySuccess(ok.getAffectedRows());
            } else {//握手阶段
                this.connectionManager.onHandShakeSuccess(new Connection(ctx.channel()));
            }
        } else if (msg instanceof ErrorResponse) {
            Entry entry = entryQ.pollFirst();
            ErrorResponse errorResponse = (ErrorResponse) msg;
            if (entry != null) {
                Throwable cause = new MysqlResponseException(String.format("ErrorCode:%d:%s", errorResponse.getErrCode(), errorResponse.getErrMsg()));
                entry.promise.tryFailure(cause);
            } else {//握手阶段
                Throwable cause = new ConnectionException(String.format("ErrorCode:%d:%s", errorResponse.getErrCode(), errorResponse.getErrMsg()));
                connectionManager.onHandShakeFail(cause);
            }

        } else if (msg instanceof ServerGreeting) {
            ServerGreeting packet = (ServerGreeting) msg;
            AuthRequest response = connectionManager.doHandShake(packet, ctx.alloc(), ctx.executor());
            ctx.writeAndFlush(response);
        } else if (msg instanceof DefaultResultSet) {
            Entry entry = entryQ.pollFirst();
            DefaultResultSet rs = (DefaultResultSet) msg;
            entry.promise.trySuccess(rs.records());
        } else if (msg instanceof PreparedResponse) {
            Entry entry = entryQ.pollFirst();
            entry.promise.trySuccess(msg);
        } else if (msg instanceof ExecutedResult) {
            Entry entry = entryQ.pollFirst();
            ExecutedResult rs = (ExecutedResult) msg;
            entry.promise.trySuccess(rs);

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
        logger.error("Error found response MysqlDuplexHandler!", cause);
        failAll(cause);
        ctx.channel().close();
    }

    private void failAll(Throwable cause) {
        for (Entry entry; (entry = entryQ.pollFirst()) != null; ) {
            entry.promise.tryFailure(cause);
        }
    }

    private void scheduleTimeoutTask(ChannelHandlerContext ctx) {
        if (timeoutMs > 0 && timeoutTask == null) {
            timeoutTask = ctx.executor().schedule(new TimeoutTask(ctx), timeoutMs,
                    TimeUnit.MILLISECONDS);
        }
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
