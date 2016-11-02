package com.yanming.support;

/**
 * Created by allan on 16/10/31.
 */
public enum ServerStatus {
    IN_TRANSACTION(0x0001),
    AUTO_COMMIT(0x0002),
    MORE_RESULTS(0x0008),
    NO_GOOD_INDEX_USED(0x0010),
    NO_INDEX_USED(0x0020),
    CURSOR_EXISTS(0x0040),
    LAST_ROW_SENT(0x0080),
    DB_DROPPED(0x0100),
    NO_BACKSLASH_ESCAPES(0x0200),
    METADATA_CHANGED(0x0400),
    QUERY_WAS_SLOW(0x0800),
    PS_OUT_PARAMS(0x1000),
    IN_TRANS_READONLY(0x2000),
    SESSION_STATE_CHANGED(0x4000);

    private long code;

    private ServerStatus(long code) {
        this.code = code;
    }

    public long code() {
        return this.code;
    }
}
