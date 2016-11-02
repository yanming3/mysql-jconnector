package com.yanming.resultset;

import com.yanming.response.ServerPacket;

/**
 * Created by allan on 16/10/31.
 */
public class ResultSetRowResponse extends ServerPacket implements ResultSetObject {

    private final String[] row;

    public ResultSetRowResponse(int packetLength, int packetNumber, String[] row) {
        super(packetLength, packetNumber);
        this.row = row;
    }

    public String[] getRow() {
        return row;
    }
}
