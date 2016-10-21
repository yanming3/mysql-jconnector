# mysql-jconnector

## 简介
基于netty重新实现的mysql客户端类库，采用异步方式访问数据库;

## 例子

```java
final ConnectionManager manager = new ConnectionManager("192.10.40.42", 3306, "f_test", "f_test_2015", "test", 1000);
        Future<Connection> f = manager.connect();

manager.connect().addListener(new GenericFutureListener<Future<Connection>>() {
@Override
public void operationComplete(Future<Connection> f) throws Exception {
    if (f.isSuccess()) {
        final Connection conn = f.getNow();
        conn.queryForList("select * from user where login_name='test'").addListener(new GenericFutureListener<Future<List<Map<String, String>>>>() {
            @Override
            public void operationComplete(Future<List<Map<String, String>>> f2) throws Exception {
                if(f2.isSuccess()) {
                    List<Map<String, String>> list = f2.getNow();
                    System.out.println(list);
                    conn.close();
                    manager.close();
                }
                else{
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
```


