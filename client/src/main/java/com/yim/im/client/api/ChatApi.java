package com.yim.im.client.api;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.yim.im.client.context.UserContext;
import com.yim.im.client.handler.ClientConnectorHandler;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.proto.generate.Ack;
import com.yrw.im.proto.generate.Chat;
import io.netty.util.CharsetUtil;

/**
 * Date: 2019-05-14
 * Time: 10:29
 *
 * @author yrw
 */
public class ChatApi {

    private UserContext userContext;

    @Inject
    public ChatApi(UserContext userContext) {
        this.userContext = userContext;
    }

    public Long text(String toId, String text) {
        checkLogin();

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
            .setId(IdWorker.genId())
            .setFromId(userContext.getUserId())
            .setDestId(toId)
            .setDestType(Chat.ChatMsg.DestType.SINGLE)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Chat.ChatMsg.MsgType.TEXT)
            .setVersion(1)
            .setMsgBody(ByteString.copyFrom(text, CharsetUtil.UTF_8))
            .build();

        sendToConnector(chat, chat.getId());

        return chat.getId();
    }

    public Long file(String toId, byte[] bytes) {
        checkLogin();
        return null;
    }

    private void checkLogin() {
        if (userContext.getUserId() == null) {
            throw new ImException("client has not login!");
        }
    }

    private void sendToConnector(Message msg, Long id) {
        userContext.getClientConnectorHandler().writeAndFlush(msg, id);
    }

    public void confirmRead(Chat.ChatMsg msg) {
        Ack.AckMsg read = Ack.AckMsg.newBuilder()
            .setId(IdWorker.genId())
            .setVersion(1)
            .setFromId(msg.getDestId())
            .setDestId(msg.getFromId())
            .setCreateTime(System.currentTimeMillis())
            .setDestType(msg.getDestType() == Chat.ChatMsg.DestType.SINGLE ? Ack.AckMsg.DestType.SINGLE : Ack.AckMsg.DestType.GROUP)
            .setMsgType(Ack.AckMsg.MsgType.READ)
            .setAckMsgId(msg.getId())
            .build();

        ClientConnectorHandler.getCtx().writeAndFlush(read);
    }
}
