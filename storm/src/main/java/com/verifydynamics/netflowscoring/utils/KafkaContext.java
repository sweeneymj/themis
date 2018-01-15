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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

public class KafkaContext {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(KafkaContext.class);

    Producer<String, String> producer;

    private static KafkaContext instance = new KafkaContext();

    // singleton
    private KafkaContext() {
        connect();
    }

    public static KafkaContext getInstance() {
        return instance;
    }

    public int connect() {

        LOG.info("KafkaContext - first time - create new connection");

        Properties props = new Properties();

        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 131072);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("compression.type", "lz4");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);

        return 1;
    }

    public Producer<String, String> getConnection() {
        return producer;
    }
}
