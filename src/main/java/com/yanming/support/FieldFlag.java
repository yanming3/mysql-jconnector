package com.yanming.support;

/**
 * Created by allan on 16/10/31.
 */
public enum FieldFlag{
    NOT_NULL(0x0001),
    PRI_KEY(0x0002),
    UNIQUE_KEY(0x0004),
    MULTIPLE_KEY(0x0008),
    UNSIGNED(0x0020),
    ZEROFILL(0x40),
    BINARY(0x0080),
    AUTO_INCREMENT(0x0200);

    private int code;

    private FieldFlag(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }
}
