package com.jzy.javalib.network.netty.http;

import com.jzy.javalib.base.script.ScriptManager;
import com.jzy.javalib.base.util.TimeUtil;
import com.jzy.javalib.network.io.handler.HttpHandler;
import com.jzy.javalib.network.io.message.MsgUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * http消息处理
 *
 * @author jzyong
 * @mail 359135103@qq.com
 */
public abstract class HttpServerIoHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerIoHandler.class);

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private HttpRequest request;
    private HttpPostRequestDecoder decoder;
    /**
     * HTTP:{path:消息处理器}
     */
    Map<String, Class<? extends HttpHandler>> httpHandlerClasses = new ConcurrentHashMap<>();// 存HTTP消息处理类

    ScriptManager scriptManager;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            HttpMethod method = request.method();
            String uri = request.uri();
            if (uri == null || uri.length() == 0) {
                LOGGER.warn("{} 请求地址为空", MsgUtil.getRemoteIpPort(ctx.channel()));
                return;
            }
            try {
                if (HttpMethod.GET.equals(method)) {
                    if (uri.contains("?")) {
                        String param = uri.substring(uri.indexOf("?"));
                        if (param != null && param.length() > 0) {
                            if (msg instanceof DefaultFullHttpRequest) {
                                ((DefaultFullHttpRequest) msg).content().writeBytes(param.getBytes());
                            }
                        }
                    }
                }


                decoder = new HttpPostRequestDecoder(factory, request);
                HttpHandler handler = getHttpHandler(uri);
                LOGGER.debug("http request uri:{}", uri);
                if (handler == null) {
                    LOGGER.warn("{} 请求地址{}处理器未实现", MsgUtil.getRemoteIpPort(ctx.channel()), uri);
                    return;
                }
                if (HttpMethod.POST.equals(method)) {
                    Map<String, Object> param = getPostParamsFromChannel((FullHttpRequest) msg);
                    handler.setParam(param);
                }

                long time = TimeUtil.currentTimeMillis();
                handler.setMessage(msg);
                handler.setChannel(ctx.channel());
                handler.setCreateTime(TimeUtil.currentTimeMillis());
                handler.run();

                time = TimeUtil.currentTimeMillis() - time;
                if (time > 20) {
                    LOGGER.warn("{}处理时间超过{}", handler.getClass().getSimpleName(), time);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (msg instanceof HttpContent) {
            if (decoder != null) {
                HttpContent chunk = (HttpContent) msg;
                try {
                    decoder.offer(chunk);
                } catch (ErrorDataDecoderException e1) {
                    ctx.channel().close();
                    return;
                }
                if (chunk instanceof LastHttpContent) {
                }
            } else {
                ctx.channel().close();
                return;
            }
        }
    }

    /**
     * 获取 请求post 参数
     *
     * @param fullHttpRequest
     * @return
     */
    private Map<String, Object> getPostParamsFromChannel(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<String, Object>();
        if (fullHttpRequest.method() == HttpMethod.POST) {
            params = getFormParams(fullHttpRequest);
            return params;
        }
        return null;
    }

    private Map<String, Object> getFormParams(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<String, Object>();
        // HttpPostMultipartRequestDecoder
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                params.put(data.getName(), (FileUpload) data);
            }
        }
        return params;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    public Map<String, Class<? extends HttpHandler>> getHttpHandlerClasses() {
        return httpHandlerClasses;
    }

    public void setHttpHandlerClasses(Map<String, Class<? extends HttpHandler>> httpHandlerClasses) {
        this.httpHandlerClasses = httpHandlerClasses;
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

}
