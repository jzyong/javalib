package com.jzy.javalib.network.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.util.MutableHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * rpc 服务器
 *
 * @author jzyong
 * @mail 359135103@qq.com
 */
public class RpcServerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerManager.class);

    private static RpcServerManager rpcServerManager;

    protected Server server;

    /**
     * 固定服务
     */
    private List<BindableService> services = new ArrayList<>();

    /**
     * 可动态添加，修改，删除 service
     */
    private MutableHandlerRegistry mutableHandlerRegistry = new MutableHandlerRegistry();

    /**
     * rpc执行线程
     */
    protected Executor executor;


    public static RpcServerManager getInstance() {
        if (rpcServerManager == null) {
            synchronized (RpcServerManager.class) {
                if (rpcServerManager == null) {
                    rpcServerManager = new RpcServerManager();
                }
            }
        }
        return rpcServerManager;
    }

    /**
     * 注册service
     *
     * @param service
     * @note 在服务器启动之前注册，逻辑不能修改替换
     */
    public void registerService(BindableService service) {
        services.add(service);
    }

    /**
     * 注册service
     * <br>
     * 可动态添加，修改，删除 service
     *
     * @param service
     */
    public void registerMutableService(BindableService service) {
        this.mutableHandlerRegistry.addService(service);
    }

    /**
     * 启动
     *
     * @param rpcPort
     */
    public void start(int rpcPort) {
        try {
            ServerBuilder serverBuilder = ServerBuilder.forPort(rpcPort);
            services.forEach(service -> serverBuilder.addService(service));
            serverBuilder.fallbackHandlerRegistry(mutableHandlerRegistry);
            server = serverBuilder.build().start();
            LOGGER.info("rpc started,listening on {}", rpcPort);
        } catch (Exception e) {
            LOGGER.error("rpc star error", e);
        }
    }

    public void stop() {
        server.shutdownNow();
        LOGGER.info("关服：关闭rpc服务");
    }

}
