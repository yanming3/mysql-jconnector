package com.yanming.server.parser.response;

import com.yanming.response.ServerPacket;
import com.yanming.support.MysqlField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allan on 16/11/1.
 */
public class ExecutedResult extends ServerPacket {

    private final int columNum;

    private final List<MysqlField> columns = new ArrayList<>();

    private final List<byte[][]> data = new ArrayList<>();


    public ExecutedResult(int packetLen, int packetNo,  int columNum) {
        super(packetLen, packetNo);
        this.columNum = columNum;
    }


    public void addColumn(MysqlField field) {
        columns.add(field);
    }

    public void addRecord(byte[][] record) {
        data.add(record);
    }

    public List<byte[][]> getData() {
        return data;
    }

    public int getColumNum() {
        return columNum;
    }

    public List<MysqlField> getColumns() {
        return columns;
    }
}
