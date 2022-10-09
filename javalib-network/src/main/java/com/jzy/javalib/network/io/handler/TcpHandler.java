package com.jzy.javalib.network.io.handler;

import com.google.protobuf.Message;
import com.jzy.javalib.network.io.message.MsgUtil;
import io.netty.channel.Channel;

/**
 * @author jzyong
 * @mail 359135103@qq.com
 */
public abstract class TcpHandler implements IHandler {
    // 消息来源
    protected Channel channel;
    // 请求消息
    protected Message message;
    // 创建时间
    protected long createTime;
    // 角色|用户唯一ID
    protected long id;
    // 去除本消息长度后剩下的数据，可能有id等，根据协议确定
    private byte[] msgBytes;
    /**
     * 消息序号
     */
    private int msgSequence;

    /**
     * 发送消息，服务器内部通信
     *
     * @param msg
     */
    public void sendInnerMsg(Message msg) {
        MsgUtil.sendInnerMsg(channel, msg, id, msgSequence);
    }

    /**
     * 发送消息，网关和客户端
     *
     * @param msg
     */
    public void sendClientMsg(Message msg) {
        MsgUtil.sendClientMsg(channel, msg, msgSequence);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Message getMessage() {
        return this.message;
    }

    /**
     * 获取消息
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> T getRequest() {
        return (T) message;
    }

    public void setMessage(Object message) {
        if (message instanceof Message) {
            this.message = (Message) message;
        }
    }

    public long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public byte[] getMsgBytes() {
        return msgBytes;
    }

    public void setMsgBytes(byte[] msgBytes) {
        this.msgBytes = msgBytes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMsgSequence() {
        return msgSequence;
    }

    public void setMsgSequence(int msgSequence) {
        this.msgSequence = msgSequence;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (createTime ^ (createTime >>> 32));
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TcpHandler other = (TcpHandler) obj;
        if (createTime != other.createTime)
            return false;
        if (id != other.id)
            return false;
        return true;
    }

}
