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
package com.verifydynamics.netflowscoring;

import com.verifydynamics.netflowscoring.utils.RedisContext;
import org.apache.log4j.Logger;
import org.junit.Test;
import redis.clients.jedis.Jedis;


public class RedisCaching {

    private static final Logger LOG = Logger.getLogger(RedisCaching.class);

    @Test
    public void testRedisCaching () {
        RedisContext redisContext = RedisContext.getInstance();
        Jedis connection = redisContext.getConnection();

        connection.setex("196.5.160.2", 60, "100");

    }

}
