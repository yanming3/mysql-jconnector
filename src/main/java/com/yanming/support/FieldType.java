package com.yanming.support;

/**
 * Created by allan on 16/10/26.
 */
public enum FieldType {
    BIT(0x10),

    BLOB(0xfc),

    DATE(0x0a),

    DATETIME(0x0c),

    // Data FieldType
    DECIMAL(0x00),

    DOUBLE(0x05),

    ENUM(0xf7),

    FLOAT(0x04),

    GEOMETRY(0xff),

    INT24(0x09),

    LONG(0x03),

    LONG_BLOB(0xfb),

    LONGLONG(0x08),

    MEDIUM_BLOB(0xfa),

    NEW_DECIMAL(0xf6),

    NEWDATE(0x0e),

    NULL(0x06),

    SET(0xf8),

    SHORT(0x02),

    STRING(0xfe),

    TIME(0x0b),

    TIMESTAMP(0x07),

    TINY(0x01),

    // Older data types
    TINY_BLOB(0xf9),

    VAR_STRING(0xfd),

    VARCHAR(0x0f),

    // Newer data types
    YEAR(0x0d);


    private int code;

    private FieldType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static  FieldType valueOf(int code) {
        for (FieldType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        throw new RuntimeException("Unsupported fieldType:"+code);
    }
}
