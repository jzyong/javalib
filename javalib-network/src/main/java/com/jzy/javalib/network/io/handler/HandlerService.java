package com.jzy.javalib.network.io.handler;

import com.jzy.javalib.base.script.IHandlerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler 管理
 *
 * @author jzy
 * @mail 359135103@qq.com
 */
public class HandlerService implements IHandlerLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerService.class);

    /**
     * TCP:{MID:消息处理Bean}
     */
    @SuppressWarnings("rawtypes")
    Map<Integer, TcpHandlerBuilder> tcpHandlerBuilders = new ConcurrentHashMap<>();// 存TCP消息处理类
    /**
     * HTTP:{path:消息处理Bean}
     */
    Map<String, Class<? extends HttpHandler>> httpHandlerClasses = new ConcurrentHashMap<>();// 存HTTP消息处理类
    /**
     * Rpc:{path:Handler} rpc消息处理类
     */
    Map<String, Class<? extends RpcHandler>> rpcHandlerClasses = new ConcurrentHashMap<>();


    @Override
    public void loadHandler(Class<?> defineClass) {
        if (IHandler.class.isAssignableFrom(defineClass)) {
            try {
                Handler handler = defineClass.getAnnotation(Handler.class);
                if (handler != null) {
                    if (TcpHandler.class.isAssignableFrom(defineClass)) {
                        TcpHandlerBuilder messageBean = new TcpHandlerBuilder(handler.msg(), defineClass,
                                handler.executor());
                        tcpHandlerBuilders.put(handler.mid(), messageBean);
                        LOGGER.trace("tcp handler ：[{}]", defineClass.getName());
                    } else if (HttpHandler.class.isAssignableFrom(defineClass)) {
                        httpHandlerClasses.put(handler.path(), (Class<? extends HttpHandler>) (defineClass));
                        LOGGER.trace("http handler ：[{}]", defineClass.getName());
                    } else if (RpcHandler.class.isAssignableFrom(defineClass)) {
                        rpcHandlerClasses.put(handler.path(), (Class<? extends RpcHandler>) (defineClass));
                        LOGGER.trace("rpc handler ：[{}]", defineClass.getName());
                    } else {
                        LOGGER.warn("handler[{}]未继承Handler", defineClass.getSimpleName());
                    }
                } else {
                    LOGGER.warn("handler[{}]未添加注解", defineClass.getSimpleName());
                }
            } catch (Exception e) {
                LOGGER.error("加载Handler", e);
            }
        }
    }


    public TcpHandlerBuilder getTcpHandlerBuilder(int msgId) {
        return tcpHandlerBuilders.get(msgId);
    }

    /**
     * 获取http 消息处理器
     *
     * @param path
     * @return
     */
    public HttpHandler getHttpHandler(String path) {
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }
        Class<? extends HttpHandler> class1 = httpHandlerClasses.get(path);
        if (class1 == null) {
            return null;
        }
        try {
            return class1.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.error("HTTP消息", e);
        }
        return null;
    }

    /**
     * 获取Rpc处理器
     * @param path
     * @return
     */
    public RpcHandler getRpcHandler(String path) {
        Class<? extends RpcHandler> class1 = rpcHandlerClasses.get(path);
        if (class1 == null) {
            return null;
        }
        try {
            return class1.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.error("RPC消息", e);
        }
        return null;
    }

    /**
     * TCP消息是否注册
     *
     * @param mid
     * @return
     */
    public boolean tcpIsRegister(int mid) {
        return tcpHandlerBuilders.containsKey(mid);
    }

    /**
     * 获取tcp消息id
     *
     * @return
     */
    public Set<Integer> getTcpMessageIds() {
        return tcpHandlerBuilders.keySet();
    }

    /**
     * 注册tcp Handler
     *
     * @param handlerClass
     */
    public void registerTcpHandler(Class<? extends TcpHandler> handlerClass) {
        try {
            Handler handler = handlerClass.getAnnotation(Handler.class);
            if (handler != null) {
                TcpHandlerBuilder tcpHandlerBuilder = new TcpHandlerBuilder(handler.msg(), handlerClass, handler.executor());
                tcpHandlerBuilders.put(handler.mid(), tcpHandlerBuilder);
                LOGGER.info("加载到tcp handler到容器：{}", handlerClass.getSimpleName());
            }
        } catch (Exception e) {
            LOGGER.error("registerTcpHandler", e);
        }
    }


}
