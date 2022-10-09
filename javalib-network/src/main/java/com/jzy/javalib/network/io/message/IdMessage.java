package com.jzy.javalib.network.io.message;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * 内部消息，网关到游戏逻辑服，为每个消息添加一个ID标识
 *
 * @author jzyong
 * @mail 359135103@qq.com
 */
public final class IdMessage  {

    /**
     * 纯消息内容
     */
    private Object msg;
    /**
     * 唯一编号
     */
    private long id;

    private Channel channel;
    /**
     * 消息唯一编号
     */
    private int msgId;

    /**
     * 消息序号
     */
    private int msgSequence;




    private IdMessage(Object msg) {
        this.msg = msg;
    }

    /**
     * @param channel
     * @param msg     byte[]
     * @param id
     */
    private IdMessage(Channel channel, Object msg, long id, int msgId,int msgSequence) {
        if (msg instanceof Message || msg instanceof ByteBuf || msg instanceof byte[]) {
            this.msg = msg;
            this.id = id;
            this.channel = channel;
            this.msgId = msgId;
            this.msgSequence=msgSequence;
        } else {
            throw new RuntimeException("数据类型错误：" + msg.getClass().getName());
        }
    }

    public long getId() {
        return id;
    }

    public Channel getChannel() {
        return channel;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }


    public int getMsgSequence() {
        return msgSequence;
    }

    public void setMsgSequence(int msgSequence) {
        this.msgSequence = msgSequence;
    }

    public Object getMsg() {
        return msg;
    }

    public static IdMessage newIDMessage(Object msg) {
        return new IdMessage(msg);
    }


    public static IdMessage newIDMessage(Channel channel, Object msg, long id, int msgId) {
        return new IdMessage(channel, msg, id, msgId,0);
    }

    public static IdMessage newIDMessage(Channel channel, Object msg, long pid, int msgId,int msgSequence) {
        return new IdMessage(channel, msg, pid, msgId,msgSequence);
    }
}
