package com.yanming.test;

import com.yanming.Connection;
import com.yanming.ConnectionManager;
import com.yanming.PreparedStatement;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.List;
import java.util.Map;

/**
 * Created by allan on 16/10/18.
 */
public class TestMysql {


    public static void main(final String[] args) throws Exception {
        final ConnectionManager manager = new ConnectionManager("localhost", 3306, "f_test", "f_test_2015", "test", 1000);
        Future<Connection> f = manager.connect();

        manager.connect().addListener(new GenericFutureListener<Future<Connection>>() {
            public void operationComplete(Future<Connection> f) throws Exception {
                if (f.isSuccess()) {
                    Connection conn = f.getNow();
                    conn.preparedStatement("select id from tag_dict where id<?").addListener(new GenericFutureListener<Future<PreparedStatement>>() {
                        public void operationComplete(Future<PreparedStatement> pf) throws Exception {
                            if (pf.isSuccess()) {
                                PreparedStatement ps = pf.getNow();
                                ps.setInt(0, 4);
                                ps.executeQuery().addListener(new GenericFutureListener<Future<List<String[]>>>() {
                                    public void operationComplete(Future<List<String[]>> future) throws Exception {
                                        if (future.isSuccess()) {
                                            System.out.println(future.getNow());
                                        } else {
                                            Throwable t = future.cause();
                                            t.printStackTrace();
                                            manager.close();
                                        }
                                    }
                                });
                            } else {
                                Throwable t = pf.cause();
                                t.printStackTrace();
                                manager.close();
                            }
                        }
                    });
                } else {
                    Throwable t = f.cause();
                    t.printStackTrace();
                    manager.close();
                }
            }
        });
    }


    private static void test1() {
        final ConnectionManager manager = new ConnectionManager("192.10.40.42", 3306, "f_test", "f_test_2015", "test", 1000);
        Future<Connection> f = manager.connect();

        manager.connect().addListener(new GenericFutureListener<Future<Connection>>() {
            public void operationComplete(Future<Connection> f) throws Exception {
                if (f.isSuccess()) {
                    final Connection conn = f.getNow();
                    conn.queryForList("select * from user where login_name='test'").addListener(new GenericFutureListener<Future<List<Map<String, String>>>>() {
                        public void operationComplete(Future<List<Map<String, String>>> f2) throws Exception {
                            if (f2.isSuccess()) {
                                List<Map<String, String>> list = f2.getNow();
                                System.out.println(list);
                                conn.close();
                                manager.close();
                            } else {
                                f2.cause().printStackTrace();
                                conn.close();
                                manager.close();
                            }
                        }
                    });
                } else {
                    Throwable t = f.cause();
                    t.printStackTrace();
                    manager.close();
                }
            }
        });
    }

}
