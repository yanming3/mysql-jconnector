package com.yanming;

import com.yanming.in.PreparedStatementMessage;
import com.yanming.out.CommandMessage;
import com.yanming.packet.PreparedStatementPacket;
import com.yanming.support.BufferUtils;
import com.yanming.support.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.util.List;
import java.util.Map;


/**
 * Created by allan on 16/10/19.
 */
public class Connection {

    private Channel channel;

    protected final PromiseConverter<Boolean> booleanConverter;

    protected final PromiseConverter<List<Map<String, String>>> listConverter;

    protected final PromiseConverter<Long> longConverter;


    public Connection(Channel channel) {
        this.channel = channel;
        this.booleanConverter = PromiseConverter.toBoolean(channel.eventLoop());
        this.listConverter = PromiseConverter.toList(channel.eventLoop());
        this.longConverter = PromiseConverter.toLong(channel.eventLoop());
    }

    protected Promise newPromise() {
        return channel.eventLoop().newPromise();
    }

    protected Future<Object> execCommand0(int command, final ByteBuf data, int sequenceNo, Map<String, Object> extraData) {
        Promise<Object> promise = channel.eventLoop().newPromise();
        CommandMessage request = new CommandMessage(command, promise, data, sequenceNo);
        if (extraData != null) {
            for (Map.Entry<String, Object> entry : extraData.entrySet()) {
                request.addExtraData(entry.getKey(), entry.getValue());
            }
        }
        channel.writeAndFlush(request);
        return promise;
    }

    protected <T> Future<T> execCommand(PromiseConverter<T> converter, int command, final ByteBuf data, Map<String, Object> extraData) {
        Promise<T> promise = converter.newPromise();
        execCommand0(command, data, 0, extraData).addListener(converter.newListener(promise));
        return promise;
    }

    protected <T> Future<T> execCommand(PromiseConverter<T> converter, int command, final ByteBuf data, int sequenceNo) {
        Promise<T> promise = converter.newPromise();
        execCommand0(command, data, sequenceNo, null).addListener(converter.newListener(promise));
        return promise;
    }

    protected <T> Future<T> execCommand(PromiseConverter<T> converter, int command, final ByteBuf data) {
        return execCommand(converter, command, data, 0);
    }


    public Future<Boolean> createTable(final String sql) {
        return execCommand(booleanConverter, Command.QUERY.code(), BufferUtils.wrapString(sql));
    }

    public Future<Long> execute(final String sql) {
        return execCommand(longConverter, Command.QUERY.code(), BufferUtils.wrapString(sql));
    }

    public Future<List<Map<String, String>>> queryForList(final String sql) {
        return execCommand(listConverter, Command.QUERY.code(), BufferUtils.wrapString(sql));
    }

    public Future<PreparedStatement> preparedStatement(String sql) {
        final Promise<PreparedStatement> promise = channel.eventLoop().newPromise();
        execCommand0(Command.STMT_PREPARE.code(), BufferUtils.wrapString(sql), 0, null).addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                if (future.isSuccess()) {
                    PreparedStatementMessage pp = (PreparedStatementMessage) future.getNow();
                    promise.trySuccess(new PreparedStatement(Connection.this, pp.getId(), pp.getNumParams(), pp.getSequenceNo(), pp.getParamTypes(), pp.getColumTypes()));
                } else {
                    promise.tryFailure(future.cause());
                }
            }
        });
        return promise;
    }


    public void close() {
        channel.close();
    }
}
