package com.jzy.javalib.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Http 工具
 */
public class HttpUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    private static final String REQUEST_METHOD_POST = "POST";
    private static final String REQUEST_METHOD_GET = "GET";

    /**
     * 连接超时
     */
    private static int CONNECT_TIME_OUT = 2000;

    /**
     * 读取数据超时
     */
    private static int READ_TIME_OUT = 2000;

    /**
     * 请求编码
     */
    private static String REQUEST_ENCODING = "UTF-8";

    /**
     * <pre>
     * 发送带参数的GET的HTTP请求
     * </pre>
     *
     * @param reqUrl HTTP请求URL
     * @return HTTP响应的字符串
     */
    public static String doGet(String reqUrl, Map<String, String> paramMap, String recvEncoding) {
        return doRequest(reqUrl, paramMap, REQUEST_METHOD_GET, recvEncoding);
    }

    /**
     * <pre>
     * 发送不带参数的GET的HTTP请求
     * </pre>
     *
     * @param reqUrl HTTP请求URL
     * @return HTTP响应的字符串
     */
    public static String doGet(String reqUrl, String recvEncoding) {
        Map<String, String> paramMap = null;
        String queryUrl = reqUrl;
        int paramIndex = reqUrl.indexOf("?");
        if (paramIndex > 0) {
            paramMap = new HashMap<String, String>();
            queryUrl = reqUrl.substring(0, paramIndex);
            String parameters = reqUrl.substring(paramIndex + 1, reqUrl.length());
            String[] paramArray = parameters.split("&");
            for (int i = 0; i < paramArray.length; i++) {
                String string = paramArray[i];
                int index = string.indexOf("=");
                if (index > 0) {
                    String parameter = string.substring(0, index);
                    String value = string.substring(index + 1, string.length());
                    paramMap.put(parameter, value);
                }
            }
        } else {
            return null;
        }

        return doRequest(queryUrl, paramMap, REQUEST_METHOD_GET, recvEncoding);
    }

    /**
     * <pre>
     * 发送带参数的POST的HTTP请求
     * </pre>
     *
     * @param reqUrl HTTP请求URL
     * @return HTTP响应的字符串
     */
    public static String doPost(String reqUrl, Map<String, String> paramMap, String recvEncoding) {
        return doRequest(reqUrl, paramMap, REQUEST_METHOD_POST, recvEncoding);
    }

    private static String doRequest(String reqUrl, Map<String, String> paramMap, String reqMethod, String recvEncoding) {
        HttpURLConnection urlCon = null;
        String responseContent = null;
        try {
            StringBuilder params = new StringBuilder();
            if (paramMap != null) {
                for (Entry<String, String> element : paramMap.entrySet()) {
                    params.append(element.getKey());
                    params.append("=");
                    params.append(URLEncoder.encode(element.getValue(), REQUEST_ENCODING));
                    params.append("&");
                }

                if (params.length() > 0) {
                    params = params.deleteCharAt(params.length() - 1);
                }
            }
            URL url = new URL(reqUrl);
            urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setRequestMethod(reqMethod);
            urlCon.setConnectTimeout(5 * 1000);
            urlCon.setReadTimeout(5 * 1000);
            urlCon.setDoOutput(true);
            byte[] b = params.toString().getBytes();
            urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlCon.setRequestProperty("Content-Length", String.valueOf(b.length));
            OutputStream out = urlCon.getOutputStream();
            out.write(b, 0, b.length);
            out.flush();
            out.close();

            InputStream in = urlCon.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, recvEncoding));
            String tempLine = rd.readLine();
            StringBuffer tempStr = new StringBuffer();
            while (tempLine != null) {
                tempStr.append(tempLine);
                tempLine = rd.readLine();
            }
            responseContent = tempStr.toString();
            rd.close();
            in.close();

            urlCon.getResponseMessage();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("urlconnection error , url " + reqUrl, e);
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
        return responseContent;
    }

    public static void log(StringBuilder sb, String prefix, long end, long start) {
        long t = end - start;
        if (t > 5) {
            sb.append("time cost ====").append(prefix).append(" : ").append((end - start)).append("\n");
        }
    }

    public static String executeGet(String reqUrl, Map<String, String> paramMap, String recvEncoding) {
        return doExecute(reqUrl, paramMap, REQUEST_METHOD_GET, recvEncoding);
    }

    private static String doExecute(String reqUrl, Map<String, String> paramMap, String reqMethod, String recvEncoding) {
        HttpURLConnection urlCon = null;
        String responseContent = null;
        try {
            StringBuilder params = new StringBuilder();
            if (paramMap != null) {
                for (Entry<String, String> element : paramMap.entrySet()) {
                    params.append(element.getKey());
                    params.append("=");
                    params.append(URLEncoder.encode(element.getValue(), REQUEST_ENCODING));
                    params.append("&");
                }

                if (params.length() > 0) {
                    params = params.deleteCharAt(params.length() - 1);
                }
            }
            URL url = new URL(reqUrl);
            urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setRequestMethod(reqMethod);
            urlCon.setConnectTimeout(CONNECT_TIME_OUT);// （单位：毫秒）jdk1.5换成这个,连接超时
            urlCon.setReadTimeout(READ_TIME_OUT);// （单位：毫秒）jdk 1.5换成这个,读操作超时
            urlCon.setDoOutput(true);
            byte[] b = params.toString().getBytes();
            urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;UTF-8");
            urlCon.setRequestProperty("Content-Length", String.valueOf(b.length));
            urlCon.getOutputStream().write(b, 0, b.length);
            urlCon.getOutputStream().flush();
            urlCon.getOutputStream().close();

            InputStream in = urlCon.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, recvEncoding));
            String tempLine = rd.readLine();
            StringBuffer tempStr = new StringBuffer();
            while (tempLine != null) {
                tempStr.append(tempLine);
                tempLine = rd.readLine();
            }
            responseContent = tempStr.toString();
            rd.close();
            in.close();

            urlCon.getResponseMessage();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
        return responseContent;
    }
}
