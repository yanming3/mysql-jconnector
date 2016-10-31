package com.yanming.in;

import com.yanming.support.FieldType;

import java.util.List;

/**
 * Created by allan on 16/10/25.
 */
public class PreparedStatementMessage {
    private long id;

    private int sequenceNo;

    private int numColumns;

    private int numParams;

    private FieldType[] paramTypes;

    private FieldType[] columTypes;


    public PreparedStatementMessage(long id, int numColumns, int numParams, int sequenceNo) {
        this.id = id;
        this.numColumns = numColumns;
        this.numParams = numParams;
        this.sequenceNo = sequenceNo;
    }

    private List<String> columns;

    private List<String> params;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    public int getNumParams() {
        return numParams;
    }

    public void setNumParams(int numParams) {
        this.numParams = numParams;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(int sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public FieldType[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(FieldType[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public FieldType[] getColumTypes() {
        return columTypes;
    }

    public void setColumTypes(FieldType[] columTypes) {
        this.columTypes = columTypes;
    }
}
