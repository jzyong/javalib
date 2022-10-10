package com.jzy.javalib.base.script;

/**
 * 加载handler 接口
 *
 * @author jzy
 * @mail 359135103@qq.com
 */
public interface IHandlerLoader {

    /**
     * 加载handler
     *
     * @param handlerClass
     */
    void loadHandler(Class<?> handlerClass);

}
