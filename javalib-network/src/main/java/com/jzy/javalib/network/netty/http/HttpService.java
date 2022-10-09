package com.jzy.javalib.network.netty.http;


import com.jzy.javalib.network.netty.INettyService;

/**
 * http 通信
 *
 * @author jzyong
 * @mail 359135103@qq.com
 */
public abstract class HttpService implements INettyService<String> {

//    private static final Logger LOGGER = LoggerFactory.getLogger(HttpService.class);


    public HttpService() {
    }


    @Override
    public boolean sendMsg(Object msg) {
        throw new UnsupportedOperationException();
    }

}
