package com.jzy.javalib.network.io.handler;


import com.jzy.javalib.network.io.handler.IHandler;

import java.lang.reflect.InvocationTargetException;

/**
 * 消息 handler构造器接口
 *
 * @author jzyong
 * @mail 359135103@qq.com
 */
public interface IHandlerBuilder {

    /**
     * 消息处理线程
     *
     * @return
     */
    String getExecuteThread();

    /**
     * 获取请求处理对象
     *
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    IHandler buildHandler() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException;
}
