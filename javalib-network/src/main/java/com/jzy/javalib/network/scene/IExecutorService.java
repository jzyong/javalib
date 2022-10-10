package com.jzy.javalib.network.scene;


import java.util.concurrent.Executor;

/**
 * 获取调度线程
 *
 * @author JiangZhiYong
 * @mail 359135103@qq.com
 */
public interface IExecutorService {


    /**
     * 获取执行线程
     *
     * @param threadName
     * @return
     */
    Executor getExecutor(String threadName);

    /**
     * 注册执行线程
     *
     * @param name
     * @param executor
     */
    void register(String name, Executor executor);

    /**
     * 执行任务
     *
     * @param threadName
     * @param runnable
     */
    void execute(String threadName, Runnable runnable);

    /**
     * 注册线程使用场景
     *
     * @param threadName
     * @param scene
     */
    void registerScene(String threadName, Scene scene);
}
