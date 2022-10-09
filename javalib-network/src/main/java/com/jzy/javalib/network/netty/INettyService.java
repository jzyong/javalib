package com.jzy.javalib.network.netty;

/**网络通信端接口
 * @param <T>
 */
public interface INettyService<T> {

    /**
     * 发送消息
     * @param msg
     * @return
     */
    public boolean sendMsg(Object msg);
    



}
