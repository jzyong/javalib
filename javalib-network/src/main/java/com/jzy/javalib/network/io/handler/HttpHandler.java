package com.jzy.javalib.network.io.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP消息
 * @author jzyong
 * @mail 359135103@qq.com
 */
public abstract class HttpHandler implements IHandler {
    private static final Logger LOGGER= LoggerFactory.getLogger(HttpHandler.class);

    protected DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
    // 消息来源
    protected Channel channel;
    // 请求消息
    protected DefaultFullHttpRequest request;
    //创建时间
    protected long createTime;
    //参数
    protected Map<String, Object> param = new HashMap<>();

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        return super.clone();
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public DefaultFullHttpRequest getMessage() { // HttpRequestImpl
        return this.request;
    }

    public void setMessage(Object message) {
        if (message instanceof DefaultFullHttpRequest) {
            this.request = (DefaultFullHttpRequest) message;
        }
    }

    public DefaultFullHttpResponse getResponse() {
		return response;
	}

	public void setResponse(DefaultFullHttpResponse response) {
		this.response = response;
	}

    public long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /**
     * 返回消息
     */
    public void response() {
    	if(channel!=null && channel.isActive()) {
    		if(response.status() == HttpResponseStatus.OK) {
    			response.headers().set(HttpHeaderNames.CONTENT_LENGTH,
		    	response.content().readableBytes());
		    	response.headers().set(HttpHeaderNames.ACCEPT_CHARSET,"UTF-8");
		    	response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain; charset=utf-8");
    		}
    		channel.writeAndFlush(response);
    	}else {
    		LOGGER.warn("HttpHandler {channel 未激活！} {}",response.toString());
    	}
    }

    public byte[] getMsgBytes() {
        return response.content().array();
    }

    public void setMsgBytes(byte[] msgBytes) {
    	response.content().writeBytes(msgBytes);
    }
    
    public byte[] getRequestContentBytes() {
    	ByteBuf buf = request.content();
    	return buf.array();
    }

	public Map<String, Object> getParam() {
		return param;
	}

	public void setParam(Map<String, Object> param) {
		this.param = param;
	}
}
