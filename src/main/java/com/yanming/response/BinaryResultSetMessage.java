package com.yanming.response;

import java.util.List;

/**
 * Created by allan on 16/10/27.
 */
public class BinaryResultSetMessage {
    private List<String> column;

    private List<byte[][]> data;

    public BinaryResultSetMessage(List<String> column, List<byte[][]> data) {
        this.column = column;
        this.data = data;
    }

    public List<String> getColumn() {
        return column;
    }

    public void setColumn(List<String> column) {
        this.column = column;
    }

    public List<byte[][]> getData() {
        return data;
    }

    public void setData(List<byte[][]> data) {
        this.data = data;
    }

}
