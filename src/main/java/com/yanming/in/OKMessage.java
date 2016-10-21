package com.yanming.in;

import java.io.Serializable;

/**
 * Created by allan on 16/10/18.
 */
public class OKMessage implements Serializable {
    private final long affectedRows;
    private final long lastInsertId;

    public OKMessage(long affectedRows, long lastInsertId) {
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
