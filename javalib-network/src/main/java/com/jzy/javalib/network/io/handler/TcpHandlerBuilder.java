package com.jzy.javalib.network.io.handler;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 消息，消息处理器映射
 *
 * @param <Msg>        消息类型
 * @param <MsgHandler> Handler类型
 * @author jzyong
 * @mail 359135103@qq.com
 */
public class TcpHandlerBuilder<Msg extends Message, MsgHandler extends IHandler> implements IHandlerBuilder {
    // 消息类型
    private final Class<? extends Message> messageClass;
    // Handler类型
    private final Class<? extends IHandler> handlerClass;
    // 执行线程
    private final String executeThread;
    //消息解析方法
    private final Method parseFromMethod;

    public TcpHandlerBuilder(Class<? extends Message> messageClass, Class<? extends IHandler> handlerClass, String executeThread) throws NoSuchMethodException {
        this.messageClass = messageClass;
        this.handlerClass = handlerClass;
        this.executeThread = executeThread;
        parseFromMethod = messageClass.getDeclaredMethod("parseFrom", new Class<?>[]{byte[].class});
    }


    /**
     * 构建消息
     *
     * @param bytes
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unchecked")
    public Msg buildMessage(final byte[] bytes) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Msg msg = (Msg) parseFromMethod.invoke(null, bytes.length < 1 ? new byte[0] : bytes);
        return msg;
    }

    /**
     * 构建消息
     *
     * @param bytes
     * @param off
     * @param len
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public Msg buildMessage(final byte[] bytes, int off, int len) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        byte[] ib = Arrays.copyOfRange(bytes, off, len);
        return buildMessage(ib);
    }

    /**
     * 构建消息
     *
     * @param buf
     * @param off
     * @param len
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public Msg buildMessage(final ByteBuf buf, int off, int len) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ByteBuf data = buf.slice(off, len);
        return buildMessage(data.array());
    }

    @Override
    public String getExecuteThread() {
        return this.executeThread;
    }

    /**
     * 获取请求处理对象
     *
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Override
    public IHandler buildHandler() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (handlerClass == null) {
            return null;
        }
        return  handlerClass.getDeclaredConstructor().newInstance();
    }

    public Class<? extends Message> getMessageClass() {
        return messageClass;
    }


}
