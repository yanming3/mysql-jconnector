package com.yanming.server.parser.response;

import com.yanming.response.ServerPacket;
import com.yanming.support.FieldType;
import com.yanming.support.MysqlField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allan on 16/10/25.
 */
public class PreparedResponse extends ServerPacket {

    private final long statementId;

    private final int columNum;

    private final int paramNum;

    private final List<MysqlField> columns = new ArrayList<>();

    private final List<MysqlField> parameters = new ArrayList<>();


    public PreparedResponse(int packetLen, int packetNo, long statementId, int columNum,int paramNum) {
        super(packetLen, packetNo);
        this.statementId = statementId;
        this.columNum = columNum;
        this.paramNum=paramNum;
    }

    public long getStatementId() {
        return statementId;
    }

    public int getColumNum() {
        return columNum;
    }

    public int getParamNum() {
        return paramNum;
    }

    public void addColumn(MysqlField field) {
        columns.add(field);
    }

    public void addParameter(MysqlField field) {
        parameters.add(field);
    }

    public List<MysqlField> getColumns() {
        return columns;
    }

    public List<MysqlField> getParameters() {
        return parameters;
    }
}
