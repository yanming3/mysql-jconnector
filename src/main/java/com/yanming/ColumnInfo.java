package com.yanming;

import com.yanming.support.FieldType;

/**
 * Created by allan on 16/10/27.
 */
public class ColumnInfo {
    private String name;

    private FieldType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }
}
