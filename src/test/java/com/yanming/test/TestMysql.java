package com.yanming.test;

import com.yanming.Connection;
import com.yanming.ConnectionManager;
import com.yanming.PreparedStatement;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by allan on 16/10/18.
 */
public class TestMysql {


    public static void main(final String[] args) throws Exception {
        final ConnectionManager manager = new ConnectionManager("10.36.40.42", 3306, "f_test", "f_test_2015", "market_platform", 1000);
        Future<Connection> f = manager.connect().sync();

        Connection conn = f.getNow();

        /*Future<List<Map<String, String>>> data = conn.queryForList("select id from tag_dict where id<5").sync();
        List<Map<String, String>> result = data.getNow();
        System.out.println(result.size());*/

        Future<PreparedStatement> ps = conn.preparedStatement("insert into test1(name,create_time,total,score) values(?,?,?,?)").sync();
        PreparedStatement preparedStatement = ps.getNow();
        preparedStatement.setString(0, "tess");
        //preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        preparedStatement.setBigDecimal(2, new BigDecimal("222333.3344"));
        preparedStatement.setDouble(3, 45.4d);
        Future<Long> future = preparedStatement.executeUpdate().sync();
        Long data = future.getNow();
        System.out.println(data);
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
