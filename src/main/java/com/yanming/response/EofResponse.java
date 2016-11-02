package com.yanming.response;

import com.yanming.resultset.ResultSetObject;
import com.yanming.support.ServerStatus;

import java.util.Set;

/**
 * Created by allan on 16/10/19.
 */
public class EofResponse extends ServerPacket implements ResultSetObject {

    public enum Type {
        FIELD,
        ROW,
        PS_PARAMETER,
        PS_COLUMN
    }

    private final int warnings;
    private final Set<ServerStatus> serverStatus;
    private final Type type;

    public EofResponse(int packetLength, int packetNumber, int warnings, Set<ServerStatus> serverStatus, Type type) {
        super(packetLength, packetNumber);
        this.warnings = warnings;
        this.serverStatus = serverStatus;
        this.type = type;
    }

    public int getWarnings() {
        return warnings;
    }

    public Set<ServerStatus> getServerStatus() {
        return serverStatus;
    }

    public Type getType() {
        return type;
    }
}
