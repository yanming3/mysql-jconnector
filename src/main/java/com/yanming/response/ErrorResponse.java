package com.yanming.response;

import java.io.Serializable;

/**
 * Created by allan on 16/10/18.
 */
public class ErrorResponse extends ServerPacket implements Serializable {

    private static final long serialVersionUID = 3695922314376125122L;

    private final int errCode;

    private final String sqlState;

    private final String errMsg;

    public ErrorResponse(int packetLength, int packetNumber, int errCode, String sqlState, String errMsg) {
        super(packetLength, packetNumber);
        this.errCode = errCode;
        this.sqlState = sqlState;
        this.errMsg = errMsg;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getSqlState() {
        return sqlState;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
