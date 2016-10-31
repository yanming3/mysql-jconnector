package com.yanming.support;

/**
 * Created by allan on 16/10/25.
 */
public enum Command {
    /**
     * Text protocal
     */
    SLEEP(0x00),
    QUIT(0x01),
    INIT_DB(0x02),
    QUERY(0x03),
    FIELD_LIST(0x04),
    CREATE_DB(0x05),
    DROP_DB(0x06),
    REFRESH(0x07),
    SHUTDOWN(0x08),
    STATISTICS(0x09),
    PROCESS_INFO(0x0a),
    CONNECT(0x0b),
    PROCESS_KILL(0x0c),
    DEBUG(0x0d),
    PING(0x0e),
    TIME(0x0f),
    DELAYED_INSERT(0x10),
    CHANGED_USER(0x11),
    RESET_CONNECTION(0x1f),
    DAEMON(0x1d),

    /**
     * Replication Protocol
     */
    BINLOG_DUMP(0x12),
    BINLOG_DUMP_GTID(0x1e),
    TABLE_DUMP(0x13),
    CONNECT_OUT(0x14),
    REGISTER_SLAVE(0x15),


    /**
     * Prepared statement
     */
    STMT_PREPARE(0x16),
    STMT_EXECUTE(0x17),
    STMT_SEND_LONG_DATA(0x18),
    STMT_CLOSE(0x19),
    STMT_RESET(0x1a),

    SET_OPTION(0x1b),
    FETCH(0x1c);

    private int code;

    Command(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
