package com.jzy.javalib.network.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * rpc
 *
 * @author jzyong
 * @mail 359135103@qq.com
 */
public class RpcServerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerService.class);

	protected Server server;

	private List<BindableService> services = new ArrayList<>();

	/**
	 * rpc执行线程
	 */
	protected Executor executor;

	/**
	 * 注册service
	 *
	 * @param service
	 */
	public void registerService(BindableService service) {
		services.add(service);
	}

	public void start(int rpcPort) {
		try {
			ServerBuilder serverBuilder = ServerBuilder.forPort(rpcPort);
			services.forEach(service -> serverBuilder.addService(service));
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
