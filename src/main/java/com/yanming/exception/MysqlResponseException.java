package com.yanming.exception;

/**
 * Created by allan on 16/10/19.
 */
public class MysqlResponseException extends RuntimeException {
    public MysqlResponseException(String msg) {
        super(msg);
    }
}
