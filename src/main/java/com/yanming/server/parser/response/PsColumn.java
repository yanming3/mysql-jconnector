package com.yanming.server.parser.response;

import com.yanming.support.FieldType;

/**
 * Created by allan on 16/11/1.
 */
public class PsColumn {
    private final FieldType type;

    private final String name;

    public PsColumn(String name,FieldType type) {
        this.type = type;
        this.name = name;
    }

    public FieldType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
