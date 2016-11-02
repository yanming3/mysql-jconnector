package com.yanming;

import com.yanming.handler.MysqlDecoder;
import com.yanming.handler.PreparedDecoder;
import com.yanming.server.parser.response.PreparedResponse;
import com.yanming.request.CommandRequest;
import com.yanming.support.BufferUtils;
import com.yanming.support.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
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

    protected Future<Object> execCommand0(Command cmd, final ByteBuf data, int sequenceNo, Map<String, Object> extraData) {
        Promise promise = channel.eventLoop().newPromise();
        CommandRequest request = new CommandRequest(cmd, promise, data, sequenceNo);
        if (extraData != null) {
            for (Map.Entry<String, Object> entry : extraData.entrySet()) {
                request.addExtraData(entry.getKey(), entry.getValue());
            }
        }
        channel.writeAndFlush(request);
        return promise;
    }


    protected <T> Future<T> execCommand(PromiseConverter<T> converter, Command cmd, final ByteBuf data, int sequenceNo) {
        Promise<T> promise = converter.newPromise();
        execCommand0(cmd, data, sequenceNo, null).addListener(converter.newListener(promise));
        return promise;
    }

    protected <T> Future<T> execCommand(PromiseConverter<T> converter, Command cmd, final ByteBuf data) {
        return execCommand(converter, cmd, data, 0);
    }


    public Future<Boolean> createTable(final String sql) {
        return execCommand(booleanConverter, Command.QUERY, BufferUtils.wrapString(sql));
    }

    public Future<Long> execute(final String sql) {
        return execCommand(longConverter, Command.QUERY, BufferUtils.wrapString(sql));
    }

    public Future<List<Map<String, String>>> queryForList(final String sql) {
        return execCommand(listConverter, Command.QUERY, BufferUtils.wrapString(sql));
    }

    public Future<PreparedStatement> preparedStatement(String sql) {
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(MysqlDecoder.class) != null) {
            channel.pipeline().remove(MysqlDecoder.class);
        }
        if (pipeline.get(PreparedDecoder.class) == null) {
            channel.pipeline().addAfter("logging", "preparedDecoder", new PreparedDecoder());
        }
        final Promise<PreparedStatement> promise = channel.eventLoop().newPromise();
        execCommand0(Command.STMT_PREPARE, BufferUtils.wrapString(sql), 0, null).addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                if (future.isSuccess()) {
                    PreparedResponse pp = (PreparedResponse) future.getNow();
                    promise.trySuccess(new PreparedStatement(Connection.this, pp.getStatementId(), pp.getParamNum(), pp.getPacketNumber(), pp.getColumns()));
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
