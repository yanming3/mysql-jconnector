package com.yanming.resultset;

import com.yanming.support.MysqlField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by allan on 16/10/31.
 */
public class DefaultResultSet {
    private final List<MysqlField> fields = new ArrayList<>();

    private final int colCount;

    private final List<String[]> results = new ArrayList<>();


    public DefaultResultSet(int colCount) {
        this.colCount = colCount;
    }

    public boolean addResult(String[] result) {
        return results.add(result);
    }

    public boolean addField(MysqlField field) {
        return fields.add(field);
    }

    public List<Map<String, String>> records() {
        List<Map<String, String>> result = new ArrayList<>();
        if (results == null || results.isEmpty()) {
            return result;
        }
        for (String[] record : results) {
            Map<String, String> row = new HashMap<>();
            for (int i = 0; i < colCount; i++) {
                row.put(fields.get(i).getColumnLabel(), record[i]);
            }
            result.add(row);
        }
        return result;
    }
}
