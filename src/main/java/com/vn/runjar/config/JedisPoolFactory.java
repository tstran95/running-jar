package com.vn.runjar.config;

import com.vn.runjar.exception.VNPAYException;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Slf4j
public class JedisPoolFactory {

    private static JedisPool instance;

    /**
     * create new instance of AppServiceImpl
     */
    public static JedisPool getInstance() {
        log.info("JedisPoolFactory getInstance START");
        if (instance == null) {
            log.info("JedisPoolFactory getInstance INSTANCE NULL");
            instance = generateJedisPoolFactory();
        }

        log.info("JedisPoolFactory getInstance END");
        return instance;
    }

    public static JedisPool generateJedisPoolFactory() {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(1_00_000);
            poolConfig.setMaxIdle(1_00_000);
            poolConfig.setMinIdle(5);
            // Whether to block when the connection is exhausted,
            // false will report an exception, true will block until the timeout
            poolConfig.setBlockWhenExhausted(Boolean.TRUE);
            return new JedisPool(poolConfig, "localhost", 6379, 100000);
        } catch (Exception e) {
            throw new VNPAYException("");
        }
    }
}
