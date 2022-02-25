package com.jzy.javalib.network.scene;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

/**
 * 可写的 SceneFuture
 * @author JiangZhiYong
 * @date 2019年5月15日 下午4:41:16
 * @mail 359135103@qq.com
 */
public interface ScenePromise extends SceneFuture,Promise<Void>{

	 @Override
	 Scene scene();

	    @Override
	    ScenePromise setSuccess(Void result);

	    ScenePromise setSuccess();

	    boolean trySuccess();

	    @Override
	    ScenePromise setFailure(Throwable cause);

	    @Override
	    ScenePromise addListener(GenericFutureListener<? extends Future<? super Void>> listener);

	    @Override
	    ScenePromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

	    @Override
	    ScenePromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

	    @Override
	    ScenePromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

	    @Override
	    ScenePromise sync() throws InterruptedException;

	    @Override
	    ScenePromise syncUninterruptibly();

	    @Override
	    ScenePromise await() throws InterruptedException;

	    @Override
	    ScenePromise awaitUninterruptibly();

	    /**
	     * Returns a new {@link ScenePromise} if {@link #isVoid()} returns {@code true} otherwise itself.
	     */
	    ScenePromise unvoid();
}
