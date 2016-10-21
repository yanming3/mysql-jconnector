package com.yanming.support;


/**
 * Created by allan on 16/10/19.
 */
public enum MysqlMessageType {
    HAND_SHAKE('H'),
    OK('O'),
    ERROR('E'),
    EOF('T'),
    FIELD('F'),
    ROW('R'),
    NULL('N');

    private char value;

    private MysqlMessageType(char value) {
        this.value = value;
    }

    public static MysqlMessageType valueOf(short value) {
        switch (value) {
            case 0x00:
                return OK;
            case 0xff:
                return ERROR;
            case 0xfe:
                return EOF;
            default:
                return NULL;
        }
    }
}
