/*******************************************************************
 Themis - NetFlow scoring and tagging framework.
 Version: 0.5
 Release date: 2017/12/31
 Author: MJ Sweeney
 Rhodes University
 Computer Science Masters Project - 2017
 Supervisor: Barry Irwin
 Copyright (C) 2017, MJ Sweeney
 *******************************************************************/
package com.verifydynamics.netflowscoring.utils;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisContext {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(RedisContext.class);

    // TODO : should come from config or params
    private static String HOST = "127.0.0.1";
    // private static String HOST = "10.8.0.1";
    private static Integer PORT=6379;
    private static Integer REDIS_DB = 15;

    private static RedisContext instance = new RedisContext();

    String host;

    JedisPool pool;

    // singleton
    private RedisContext() {
        connectDB(HOST, PORT, REDIS_DB);
    }

    public static RedisContext getInstance() {
        return instance;
    }

    public int connectDB(String host, Integer port, Integer redis_db) {

        LOG.info("RedisContext - first time - create new connection");

        this.host = host;
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);

        pool = new JedisPool(poolConfig, host);

        return 1;
    }

    public Jedis getConnection() {
        Jedis jedis = pool.getResource();
        jedis.select(REDIS_DB);
        LOG.info("RedisContext - fetch a connection from the pool, active = " + pool.getNumActive() + " and idle = " + pool.getNumIdle() + " and waiting = " + pool.getNumWaiters());
        return jedis;
    }

}
