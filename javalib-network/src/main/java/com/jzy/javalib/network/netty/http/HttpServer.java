package com.jzy.javalib.network.netty.http;

import com.jzy.javalib.network.netty.config.NettyServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

/**
 * HTTP 服务器
 *
 * @author jzyong
 * @mail 359135103@qq.com
 */
public class HttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);


    private NettyServerConfig nettyServerConfig;
    protected boolean isRunning = false;
    private ChannelFuture channelFuture;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelInitializer channelInitializer;


    public HttpServer() {
    }

    /**
     * 启动
     */
    public void start() {
        synchronized (this) {
            if (!isRunning) {
                isRunning = true;
                new Thread(new BindServer()).start();
            }
        }
    }

    /**
     * 关闭
     */
    public void stop() {
        synchronized (this) {
            try {
                if (!isRunning) {
                    LOGGER.info("HttpServer " + nettyServerConfig.getName() + "is already stopped");
                    return;
                }
                channelFuture.channel().closeFuture();
                isRunning = false;
                LOGGER.info("Server is stopped");
            } catch (Exception ex) {
                LOGGER.error("close http server", ex);
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }
    }

    /**
     * 绑定http 服务
     */
    private class BindServer implements Runnable {

        private final Logger LOG = LoggerFactory.getLogger(BindServer.class);

        @Override
        public void run() {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                        .childHandler(channelInitializer)
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                channelFuture = b.bind(nettyServerConfig.getPort()).sync();
                channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if (future.isSuccess()) {
                            LOG.info("HTTP服务  端口：{} 开启完成", nettyServerConfig.getPort());
                        } else {
                            LOG.info("HTTP服务  端口：{} 开启失败", nettyServerConfig.getPort());
                        }
                    }
                });

                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException ex) {
                LOGGER.error("HTTP 服务绑定",ex);
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }
    }

    public ChannelInitializer getChannelInitializer() {
        return channelInitializer;
    }

    public void setChannelInitializer(ChannelInitializer channelInitializer) {
        this.channelInitializer = channelInitializer;
    }

    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }

    public void setNettyServerConfig(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
    }
}
