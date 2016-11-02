package com.yanming.test;

import com.yanming.support.ServerStatus;

import java.lang.reflect.Method;

/**
 * Created by allan on 16/10/27.
 */
public class TestMain {
    public static void main(String[] args) throws Exception{
       Class cls= Class.forName("com.yanming.support.ServerStatus");
      Method m=  cls.getMethod("code");
       Object obj= m.invoke(ServerStatus.AUTO_COMMIT);
        System.out.println(obj);
    }
}
