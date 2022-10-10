package com.jzy.javalib.network.scene;

import io.netty.util.concurrent.SingleThreadEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 线程池服务
 * <br>
 * 默认注册了io
 *
 * @author JiangZhiYong
 * @mail 359135103@qq.com
 */
public abstract class AbstractExecutorService implements IExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExecutorService.class);

    //执行器 key：名称
    private final Map<String, Executor> executors = new ConcurrentHashMap<>();
    //默认线程组
    private SceneLoopGroup defaultTaskLoopGroup = new SceneTaskLoopGroup();


    public void destroy() {
        defaultTaskLoopGroup.shutdownGracefully();
    }

    @Override
    public void register(String name, Executor executor) {
        executors.put(name, executor);
    }


    @Override
    public Executor getExecutor(String threadName) {
        return executors.get(threadName);
    }

    @Override
    public void execute(String threadName, Runnable runnable) {
        Executor executor = getExecutor(threadName);
        if (executor == null) {
            LOGGER.warn("线程 {} 未注册", threadName);
            return;
        }
        executor.execute(runnable);
    }

    /**
     * 注册 scene 线程
     *
     * @param threadName
     * @param scene
     */
    public void registerScene(String threadName, Scene scene) {
        SceneLoop sceneLoop = defaultTaskLoopGroup.next();
        LOGGER.info("{} {} 注册到{} 线程 ", threadName, scene.getClass().getSimpleName(),
                ((SingleThreadEventExecutor) sceneLoop).threadProperties().name());
        scene.register(sceneLoop);
        executors.put(threadName, scene.eventLoop());
    }

    /**
     * 移除注册
     *
     * @param executorType
     */
    public void removeScene(String executorType) {
        executors.remove(executorType);
    }

    public Map<String, Executor> getExecutors() {
        return executors;
    }

    /**
     * 打印错误信息，定时任务抛异常任务会被取消
     *
     * @param exception
     */
    public void showException(Exception exception, Logger logger) {
        logger.error("{}", getExceptionString(exception));
    }

    /**
     * 获取异常信息
     *
     * @param exception
     * @return
     */
    protected String getExceptionString(Throwable exception) {
        StringBuffer sb = new StringBuffer();
        sb.append("\r\n");
        sb.append(exception.getClass().getSimpleName()).append(":");
        sb.append(exception.getMessage()).append("\n");
        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
            sb.append(stackTraceElement.toString()).append("\n");
        }
        return sb.toString();
    }
}
