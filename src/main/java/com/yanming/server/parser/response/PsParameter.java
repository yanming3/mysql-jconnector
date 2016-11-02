package com.yanming.server.parser.response;

import com.yanming.support.FieldType;

/**
 * Created by allan on 16/11/1.
 */
public class PsParameter {
    private final FieldType type;

    private final String name;

    public PsParameter(String name,FieldType type) {
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
