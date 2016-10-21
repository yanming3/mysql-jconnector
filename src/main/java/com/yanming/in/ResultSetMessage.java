package com.yanming.in;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by allan on 16/10/19.
 */
public class ResultSetMessage {
    private List<String> column;

    private List<String[]> data;

    public ResultSetMessage(List<String> column, List<String[]> data) {
        this.column = column;
        this.data = data;
    }

    public List<String> getColumn() {
        return column;
    }

    public void setColumn(List<String> column) {
        this.column = column;
    }

    public List<String[]> getData() {
        return data;
    }

    public void setData(List<String[]> data) {
        this.data = data;
    }

    public List<Map<String, String>> records() {
        List<Map<String, String>> result = new ArrayList<>();
        for (String[] row : data) {
            Map<String, String> record = new HashMap<>();
            for (int i = 0, len = row.length; i < len; i++) {
                record.put(column.get(i), row[i]);
            }
            result.add(record);
        }
        return result;
    }
}
