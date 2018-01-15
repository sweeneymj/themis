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
package com.verifydynamics.netflowscoring.bolt.enrichment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.verifydynamics.netflowscoring.domain.NetFlow;
import com.verifydynamics.netflowscoring.lookup.ASNLookup;
import com.verifydynamics.netflowscoring.utils.RedisContext;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.Constants;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.*;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.ConcurrentHashMap;

public class MatchFlowBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(MatchFlowBolt.class);

    Jedis connection;
    private Gson gson;
    private ExpiringMap<String, NetFlow> tupleCache;
    private ConcurrentHashMap<String, NetFlow> expiredFlows;
    private ASNLookup asnLookup;

    private void catchExpiry (String key, NetFlow expiredBean) {
        LOG.info("MatchFlowBolt - record evicted id = " + key + " / " + expiredBean.getId());
        expiredFlows.put(key, expiredBean);
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config conf = new Config();
        int tickFrequencyInSeconds = 10;
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, tickFrequencyInSeconds);
        return conf;
    }

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("MatchFlowBolt - initialising cache");

        // initialise map
        tupleCache = ExpiringMap.builder()
                .maxSize(50000)  // 20000
                .expiration(120, TimeUnit.SECONDS) // 120
                .expirationListener((key, netFlowBean) -> catchExpiry((String) key, (NetFlow) netFlowBean) )
                .build();

        // expired map
        expiredFlows = new ConcurrentHashMap<String, NetFlow>();

        // redis connection
        RedisContext redisContext = RedisContext.getInstance();
        this.connection = redisContext.getConnection();
        this.connection.select(10);

        // for serialsation/deserialisation
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();

        // ASN lookup
        asnLookup = ASNLookup.getInstance();
    }

    private void storeTuple (String key, NetFlow tuple) {
        Transaction transaction = connection.multi();
        String tupleJson = gson.toJson(tuple);
        transaction.set(key, tupleJson);
        transaction.expire(key, 300);
        transaction.exec();
    }

    private NetFlow fetchTuple (String key) {
        String jstonTuple = connection.get(key);
        if (jstonTuple == null) {
            return null;
        }
        return gson.fromJson(jstonTuple, NetFlow.class);
    }

    private void removeTuple (String key) {
        connection.del(key);
    }

    private boolean keyExists (String key) {
        return connection.exists(key);
    }

    private static boolean isTickTuple(Tuple tuple) {
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
                && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {

        if (isTickTuple(input)) {
            LOG.info("MatchFlowBolt - tick-tock - check for expired flows");
            Iterator it = expiredFlows.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                // if key is still in the shared memory then we assume no match was found so we have to send on
                LOG.info("MatchFlowBolt - checking for key: " + pair.getKey());
                if (keyExists((String)pair.getKey())) {
                    NetFlow expiredFlow = (NetFlow)pair.getValue();
                    LOG.info("MatchFlowBolt - removing expired flow from cache and forwarding : " + expiredFlow.getId());
                    collector.emit(new Values(expiredFlow.getId(), expiredFlow));
                }
            }
            expiredFlows.clear();
            return;
        }

        NetFlow cachedNetFlowBean;
        String flowKey;

        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow) input.getValue(1);

        LOG.info("MatchFlowBolt - record id = " + flowId);

        // we only cache TCP and UDP for matching
        if ((netFlowBean.getProtocol() != 6) && (netFlowBean.getProtocol() != 17)) {
            collector.emit(new Values(flowId, netFlowBean));
            return;
        }

        // build a key for the cache
        if (netFlowBean.getSrc_ip_int() < netFlowBean.getDst_ip_int()) {
            flowKey = netFlowBean.getSrc_ip() + '-' + netFlowBean.getSrc_port() + '-' + netFlowBean.getDst_ip() + '-' + netFlowBean.getDst_port();
        } else {
            flowKey = netFlowBean.getDst_ip() + '-' + netFlowBean.getDst_port() + '-' + netFlowBean.getSrc_ip() + '-' + netFlowBean.getSrc_port();
        }

        LOG.info("MatchFlowBolt - record id = " + flowId + " : key = " + flowKey);

        // check the cache
        cachedNetFlowBean = fetchTuple(flowKey);

        // not in cache so cache and move on
        if (cachedNetFlowBean == null) {
			LOG.info("MatchFlowBolt - record id = " + flowId + " : new tuple - caching");
			// store in shared memory
            storeTuple(flowKey, netFlowBean);
            // record in our cache
            tupleCache.put(flowKey, netFlowBean);
            return;
        }

        // we found the match - lets update the counters and pass both on
		LOG.info("MatchFlowBolt - record id = " + flowId + " : cached opposite tuple found = " + cachedNetFlowBean.getId());

        cachedNetFlowBean.setBytes_recv(netFlowBean.getBytes_sent());
        cachedNetFlowBean.setPackets_recv(netFlowBean.getPackets_sent());

        netFlowBean.setBytes_recv(cachedNetFlowBean.getBytes_sent());
        netFlowBean.setPackets_recv(cachedNetFlowBean.getPackets_sent());

        netFlowBean.setMatched(true);

        // only send out one copy with our IP on the left
        if (asnLookup.exists(netFlowBean.getSrc_as())) {
            collector.emit(new Values(netFlowBean.getId(), netFlowBean));
        } else {
            collector.emit(new Values(cachedNetFlowBean.getId(), cachedNetFlowBean));
        }

        // remove from the cache
        removeTuple(flowKey);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("flowId", "netFlowBean"));
    }
}
