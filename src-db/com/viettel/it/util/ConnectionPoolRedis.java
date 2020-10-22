/*
* Copyright (C) 2011 Viettel Telecom. All rights reserved.
* VIETTEL PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
*/
package com.viettel.it.util;

import com.google.common.base.Throwables;
import com.viettel.passprotector.PassProtector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author KhiemVK
 * @version 1.0
 * @since: 4/25/2015 10:45 AM
 */
public class ConnectionPoolRedis {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolRedis.class);
    private static ConnectionPoolRedis instance;
    private static JedisPool redisPool;
    private static final int TIME_OUT = 2 * 60 * 1000;
    private static int numberRetry = 5;
    private final static Object lock = new Object();

    private static synchronized ConnectionPoolRedis getInstance() throws Exception {
        if (instance == null) {
            instance = new ConnectionPoolRedis();
        }
        return instance;
    }

    private static synchronized void initConnectionPool() throws Exception {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(300);
            poolConfig.setMaxIdle(200);
            poolConfig.setMinIdle(10);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setMaxWaitMillis(60000);
            poolConfig.setMinEvictableIdleTimeMillis(60000);
            poolConfig.setSoftMinEvictableIdleTimeMillis(600000);
            poolConfig.setTimeBetweenEvictionRunsMillis(100l);
            poolConfig.setNumTestsPerEvictionRun(15);

            redisPool = new JedisPool(
                    poolConfig,
                    MessageUtil.getResourceBundleConfig("redis_hosts"),
                    Integer.valueOf(MessageUtil.getResourceBundleConfig("redis_port")),
                    3*1000,
                    PassProtector.decrypt(MessageUtil.getResourceBundleConfig("redis_master_pass"), "ipchange"));

        } catch (Exception e) {
            logger.error(Throwables.getStackTraceAsString(e));
            throw new Exception("Create connectionPoolRedis failed");
        }
    }

    private static Jedis createRedis(int idx) throws Exception {
        logger.info("CreateRedis redis master -> i = " + idx);
        try {
            synchronized (lock) {
                if (redisPool == null || redisPool.isClosed()) {
                    initConnectionPool();
                }
            }
            Jedis jedis = redisPool.getResource();
            jedis.getClient().setTimeoutInfinite();
            return jedis;
        } catch (Exception e) {
            logger.error("CreateRedis redis master -> i = " + idx, e);
        }
        return null;

    }

    public static Jedis getRedis() throws Exception {
        long startTime = System.currentTimeMillis();
        logger.info("Get redis master " + Thread.currentThread().getName());
        try {
            int i = 0;
            Jedis jedis = null;
            while (i < numberRetry && (jedis = createRedis(i)) == null) {
                i++;
            }
            long duration = (System.currentTimeMillis() - startTime) / 1000;
            if (jedis == null) {
                logger.error("Get redis master NOK, duration: " + duration + " (s)");
                throw new Exception("Get redis master fail");
            } else {
                logger.info("Get redis master OK, duration: " + duration + " (s) -> NumActive: " + redisPool.getNumActive());
                return jedis;
            }
        } catch (Exception e) {
            throw new Exception("Get redis master fail", e);
        }

    }

    public static void closeJedis(Jedis jedis) {
        try {
            if (jedis != null && redisPool != null) {
//                logger.info("Before return redis master -> NumActive: " + redisPool.getNumActive() + ", NumIdle: " + redisPool.getNumIdle() + ", NumWaiters: " + redisPool.getNumWaiters());
                redisPool.returnResourceObject(jedis);
//                logger.info("After return redis master -> NumActive: " + redisPool.getNumActive() + ", NumIdle: " + redisPool.getNumIdle() + ", NumWaiters: " + redisPool.getNumWaiters());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void destroy() {
        try {
            if (redisPool != null) {
                redisPool.destroy();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static  void main(String args[]) {
        try {
//            ConnectionPoolRedis.getRedis().append("123", "123");
//            System.out.print(ConnectionPoolRedis.getRedis().get("123"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}

