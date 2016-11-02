package com.yanming.handler;

import com.yanming.response.EofResponse;
import com.yanming.resultset.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Created by allan on 16/10/31.
 */
public class ResultSetAggregator extends MessageToMessageDecoder<ResultSetObject> {

    private DefaultResultSet currentMessage;

    @Override
    protected void decode(ChannelHandlerContext ctx, ResultSetObject msg, List<Object> out) throws Exception {
        if (msg instanceof ResultSetResponse) {//header
            currentMessage = new DefaultResultSet(((ResultSetResponse) msg).getColumnCount());
        } else if (msg instanceof ResultSetFieldResponse) {
            ResultSetFieldResponse field = (ResultSetFieldResponse) msg;
            currentMessage.addField(field.getField());
            return;
        } else if (msg instanceof EofResponse) {
            EofResponse eofResponse = (EofResponse) msg;
            switch (eofResponse.getType()) {
                case FIELD:
                    break;
                case ROW:
                    out.add(currentMessage);
                    this.currentMessage = null;
                    break;
                default:
                    out.add(msg);
            }

        } else if (msg instanceof ResultSetRowResponse) {
            ResultSetRowResponse row = (ResultSetRowResponse) msg;
            currentMessage.addResult(row.getRow());
            return;
        }
    }
}
