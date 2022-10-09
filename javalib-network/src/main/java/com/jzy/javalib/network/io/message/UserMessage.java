package com.jzy.javalib.network.io.message;

/**
 * 用户消息，用于网关缓存用户消息，重连进行再次发送
 *
 * @author jzy
 * @mail 359135103@qq.com
 */
public class UserMessage {
    /**
     * 消息唯一编号
     */
    private int msgId;

    /**
     * 消息序号
     */
    private int msgSequence;

    /**
     * 消息内容
     */
    private byte[] msgBytes;

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

    public byte[] getMsgBytes() {
        return msgBytes;
    }

    public void setMsgBytes(byte[] msgBytes) {
        this.msgBytes = msgBytes;
    }
}
