package com.yanming;

import com.yanming.exception.MysqlResponseException;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;

import java.util.List;
import java.util.Map;

/**
 * Created by allan on 16/10/19.
 */
abstract class PromiseConverter<T> {
    private final EventExecutor executor;

    public PromiseConverter(EventExecutor executor) {
        this.executor = executor;
    }

    public abstract FutureListener<Object> newListener(Promise<T> promise);

    public Promise<T> newPromise() {
        return executor.newPromise();
    }

    public static PromiseConverter<List<Map<String, String>>> toList(EventExecutor executor) {
        return new PromiseConverter<List<Map<String, String>>>(executor) {

            @Override
            public FutureListener<Object> newListener(final Promise<List<Map<String, String>>> promise) {
                return new FutureListener<Object>() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public void operationComplete(Future<Object> future) throws Exception {
                        if (future.isSuccess()) {
                            Object resp = future.getNow();
                            if (resp instanceof MysqlResponseException) {
                                promise.tryFailure((MysqlResponseException) resp);
                            } else {
                                promise.trySuccess((List<Map<String, String>>) resp);
                            }
                        } else {
                            promise.tryFailure(future.cause());
                        }
                    }
                };


            }
        };


    }

    public static PromiseConverter<Boolean> toBoolean(EventExecutor executor) {
        return new PromiseConverter<Boolean>(executor) {

            @Override
            public FutureListener<Object> newListener(final Promise<Boolean> promise) {
                return new FutureListener<Object>() {

                    @Override
                    public void operationComplete(Future<Object> future) throws Exception {
                        if (future.isSuccess()) {
                            Object resp = future.getNow();
                            if (resp instanceof MysqlResponseException) {
                                promise.tryFailure((MysqlResponseException) resp);
                            } else if (resp instanceof String) {
                                promise.trySuccess(true);
                            } else {
                                promise.trySuccess(((Long) resp).intValue() != 0);
                            }
                        } else {
                            promise.tryFailure(future.cause());
                        }
                    }
                };

            }
        };

    }


    public static PromiseConverter<Double> toDouble(EventExecutor executor) {
        return new PromiseConverter<Double>(executor) {

            @Override
            public FutureListener<Object> newListener(final Promise<Double> promise) {
                return new FutureListener<Object>() {

                    @Override
                    public void operationComplete(Future<Object> future) throws Exception {
                        if (future.isSuccess()) {
                            Object resp = future.getNow();
                            if (resp instanceof MysqlResponseException) {
                                promise.tryFailure((MysqlResponseException) resp);
                            } else {
                                promise.trySuccess(Double.valueOf((String) resp));
                            }
                        } else {
                            promise.tryFailure(future.cause());
                        }
                    }
                };
            }
        };
    }

    public static PromiseConverter<Long> toLong(EventExecutor executor) {
        return new PromiseConverter<Long>(executor) {

            @Override
            public FutureListener<Object> newListener(final Promise<Long> promise) {
                return new FutureListener<Object>() {

                    @Override
                    public void operationComplete(Future<Object> future) throws Exception {
                        if (future.isSuccess()) {
                            Object resp = future.getNow();
                            if (resp instanceof MysqlResponseException) {
                                promise.tryFailure((MysqlResponseException) resp);
                            } else {
                                promise.trySuccess((Long) resp);
                            }
                        } else {
                            promise.tryFailure(future.cause());
                        }
                    }
                };
            }
        };
    }


    public static PromiseConverter<String> toString(EventExecutor executor) {
        return new PromiseConverter<String>(executor) {

            @Override
            public FutureListener<Object> newListener(final Promise<String> promise) {
                return new FutureListener<Object>() {

                    @Override
                    public void operationComplete(Future<Object> future) throws Exception {
                        if (future.isSuccess()) {
                            Object resp = future.getNow();
                            if (resp instanceof MysqlResponseException) {
                                promise.tryFailure((MysqlResponseException) resp);
                            } else {
                                promise.trySuccess(resp.toString());
                            }
                        } else {
                            promise.tryFailure(future.cause());
                        }
                    }
                };
            }
        };
    }

}

