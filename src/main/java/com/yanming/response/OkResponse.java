package com.yanming.response;

import java.io.Serializable;

/**
 * Created by allan on 16/10/18.
 */
public class OkResponse implements Serializable {
    private final long affectedRows;
    private final long lastInsertId;

    public OkResponse(long affectedRows, long lastInsertId) {
        this.affectedRows = affectedRows;
        this.lastInsertId = lastInsertId;
    }

    public long getAffectedRows() {
        return affectedRows;
    }


    public long getLastInsertId() {
        return lastInsertId;
    }
}
