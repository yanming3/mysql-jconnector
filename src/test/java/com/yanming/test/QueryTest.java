package com.yanming.test;

import com.yanming.Connection;
import com.yanming.ConnectionManager;
import io.netty.util.concurrent.Future;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

/**
 * Created by allan on 16/10/27.
 */
public class QueryTest {

    private static Connection conn;

    @BeforeClass
    public static void init() {
        try {
            final ConnectionManager manager = new ConnectionManager("10.36.40.42", 3306, "f_test", "f_test_2015", "market_platform", 1000);
            Future<Connection> f = manager.connect();
            f.sync();
            conn = f.getNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleQuery() throws Exception {
        Future<List<Map<String, String>>> f = conn.queryForList("select * from tag_dict where id<4");
        f.sync();
        List<Map<String, String>> result = f.getNow();
        assertEquals(result.size(), 3);
    }

    @Test
    public void testCreateTable() throws Exception {
        Future<Boolean> f = conn.createTable("create table IF NOT EXISTS test1(id int auto_increment,name varchar(50),primary key(id))");
        f.sync();
        Boolean result = f.getNow();
        assertEquals(result, true);
    }

    @Test
    public void testInsert() throws Exception {
        Future<Long> f = conn.execute("insert into test1(name) values('test')");
        f.sync();
        Long result = f.getNow();
        assert (result == 1);
    }

    @Test
    public void testUpdate() throws Exception {
        Future<Long> f = conn.execute("update  test1 set name='test2'");
        f.sync();
        Long result = f.getNow();
        assert (result == 1);
    }

    @AfterClass
    public static void close() {
        if (conn != null) {
        }
        conn.close();
    }
}
