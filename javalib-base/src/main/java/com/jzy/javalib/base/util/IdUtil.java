package com.jzy.javalib.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 唯一id 生成工具
 *
 * @author jzy
 */
public class IdUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(IdUtil.class);

    private static final AtomicLong id = new AtomicLong(0);
    // 当前服务器id
    public static int SERVER_ID = 0;

    public static long getId() {
        return (((long) SERVER_ID) & 0xFFFF) << 48 | (TimeUtil.currentTimeMillis() / 1000L & 0xFFFFFFFF) << 16
                | id.addAndGet(1) & 0xFFFF;
    }

    public static long getId(int key) {
        return (((long) key) & 0xFFFF) << 48 | (TimeUtil.currentTimeMillis() / 1000L & 0xFFFFFFFF) << 16
                | id.addAndGet(1) & 0xFFFF;
    }


    /**
     * 经典的Time33 hash算法
     * <p>
     * 参考：https://blog.csdn.net/weixin_33933118/article/details/93177162
     * </p>
     */
    public static int getHash(String key) {
        if (StringUtil.isEmpty(key))
            return 0;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            key = new String(digest.digest(key.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("getHash", e);
        }
        int hash = 5381;
        for (int i = 0; i < key.length(); i++) {
            int cc = key.charAt(i);
            hash += (hash << 5) + cc;
        }
        return hash < 0 ? -hash : hash;
    }

}
