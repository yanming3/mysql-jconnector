package com.yanming;

import com.yanming.out.CommandMessage;
import com.yanming.support.CommandFlags;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import java.util.List;
import java.util.Map;

/**
 * Created by allan on 16/10/19.
 */
public class Connection {

    private Channel channel;

    private final PromiseConverter<Boolean> booleanConverter;

    private final PromiseConverter<List<Map<String, String>>> listConverter;

    public Connection(Channel channel) {
        this.channel = channel;
        this.booleanConverter = PromiseConverter.toBoolean(channel.eventLoop());
        this.listConverter = PromiseConverter.toList(channel.eventLoop());
    }

    private <T> Future<T> execCommand(PromiseConverter<T> converter, int command, final String sql) {
        Promise<T> promise = converter.newPromise();
        execCommand0(command, sql).addListener(converter.newListener(promise));
        return promise;
    }

    private Future<Object> execCommand0(int command, final String sql) {
        Promise<Object> promise = channel.eventLoop().newPromise();
        CommandMessage request = new CommandMessage(command, promise, sql);
        channel.writeAndFlush(request);
        return promise;
    }

    public Future<Boolean> createTable(final String sql) {
        return execCommand(booleanConverter, CommandFlags.COM_QUERY, sql);
    }

    public Future<List<Map<String, String>>> queryForList(final String sql) {
        return execCommand(listConverter, CommandFlags.COM_QUERY, sql);
    }

    public void close() {
        channel.close();
    }
}
