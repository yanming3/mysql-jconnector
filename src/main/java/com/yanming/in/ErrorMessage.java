package com.yanming.in;

import java.io.Serializable;

/**
 * Created by allan on 16/10/18.
 */
public class ErrorMessage implements Serializable {

    private static final long serialVersionUID = 3695922314376125122L;

    private int errCode;

    private String errMsg;

    public ErrorMessage(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
