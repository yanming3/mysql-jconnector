package com.yanming.resultset;

import com.yanming.support.MysqlField;
import com.yanming.response.ServerPacket;

/**
 * Created by allan on 16/10/31.
 */
public class ResultSetFieldResponse extends ServerPacket implements ResultSetObject {
    private final MysqlField field;

    public ResultSetFieldResponse(int packetLength, int packetNumber, MysqlField field) {
        super(packetLength, packetNumber);
        this.field = field;
    }

    public MysqlField getField() {
        return field;
    }
}
