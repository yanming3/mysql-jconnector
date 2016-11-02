package com.yanming.test;

import com.yanming.Connection;
import com.yanming.ConnectionManager;
import com.yanming.PreparedStatement;
import io.netty.util.concurrent.Future;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;

/**
 * Created by allan on 16/10/27.
 */
public class PreparedStatementTest {
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
        Future<PreparedStatement> future = conn.preparedStatement("select * from tag_dict where id<?").sync();
        PreparedStatement ps = future.getNow();
        ps.setInt(0, 4);
        Future<List<String[]>> data = ps.executeQuery().sync();

        List<String[]> recordList = data.getNow();
        for (String[] record : recordList) {
            for (String column : record) {
                System.out.println(column);
            }
        }
        assertEquals(data.getNow().size(), 3);
    }

    @Test
    public void testInsert() throws Exception {
        Future<PreparedStatement> future = conn.preparedStatement("insert into  test1(name) values(?)").sync();
        PreparedStatement ps = future.getNow();
        ps.setString(0, "STEST");
        Future<Long> data = ps.executeUpdate().sync();


        assertEquals((long) data.getNow(), 1L);
    }

    @Test
    public void testUpdate() throws Exception {
        Future<PreparedStatement> future = conn.preparedStatement("update   test1 set name=?").sync();
        PreparedStatement ps = future.getNow();
        ps.setString(0, "TEST1");
        Future<Long> data = ps.executeUpdate().sync();

        assert (data.getNow() == 1);
    }

    @AfterClass
    public static void close() {
        if (conn != null) {
        }
        conn.close();
    }
}
