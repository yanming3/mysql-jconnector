package com.yanming.resultset;

import com.yanming.response.ServerPacket;

/**
 * Created by allan on 16/10/31.
 */
public class ResultSetResponse extends ServerPacket implements ResultSetObject{
    private final int columnCount;

    public ResultSetResponse(int packetLength, int packetNumber, int columnCount) {
        super(packetLength, packetNumber);
        this.columnCount = columnCount;
    }

    public int getColumnCount() {
        return columnCount;
    }
}
