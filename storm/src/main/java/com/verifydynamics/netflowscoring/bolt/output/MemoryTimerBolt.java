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
package com.verifydynamics.netflowscoring.bolt.output;

import com.verifydynamics.netflowscoring.utils.RedisContext;
import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.Constants;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.Jedis;

import java.util.Map;

// Instrumentation bolt used for tracking various counters and timings - stores data in memory
public class MemoryTimerBolt extends BaseBasicBolt {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(MemoryTimerBolt.class);

    Jedis connection;
    String key;
    boolean setOnce;
    long tupleCount = 0;

    public MemoryTimerBolt(String key, boolean setOnce) {
        this.key = key;
        this.setOnce = setOnce;
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config conf = new Config();
        int tickFrequencyInSeconds = 10;
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, tickFrequencyInSeconds);
        return conf;
    }

    private static boolean isTickTuple(Tuple tuple) {
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
                && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
    }

    @Override
    public void prepare(Map conf, TopologyContext context) {
        // redis connection
        RedisContext redisContext = RedisContext.getInstance();
        this.connection = redisContext.getConnection();
        this.connection.select(9);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {

        if (isTickTuple(input)) {
            Long now = System.currentTimeMillis();
            Long checkpoint = (now/10000);

            // set primary counter
            String counter = key + "-counter";
            connection.incrBy(counter, tupleCount);

            // set checkpoint counter
            String checkpoint_key = key + "-checkpoint-" + checkpoint.toString() + "-counter";
            connection.incrBy(checkpoint_key, tupleCount);

            tupleCount = 0;
        }
        tupleCount++;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
