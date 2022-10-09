package com.jzy.javalib.network.netty;

import com.jzy.javalib.base.script.IInitScript;
import com.jzy.javalib.base.script.IScript;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;


/**
 * netty channel，handler脚本
 *
 * @author JiangZhiYong
 * @date 2018/12/11
 */
public interface IChannelHandlerScript extends IScript, IInitScript {

    /**
     * 连接是否为黑名单
     *
     * @param ctx
     * @return
     */
    default boolean isBlackList(ChannelHandlerContext ctx) {
        return false;
    }

    /**
     * 进入消息检测
     *
     * @param ctx
     * @param byteBuf
     * @return true 消息正常， false消息异常
     */
    default boolean inBoundMessageCheck(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        return true;
    }

    /**
     * 初始化channel
     */
    default void initChannel(SocketChannel ch, Object... objects) {
    }

    /**
     * channel active
     *
     * @param ctx
     */
    default void channelActive(ChannelHandlerContext ctx, Object... objects) {

    }
}
