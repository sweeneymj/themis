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
package com.verifydynamics.netflowscoring.bolt.utility;

import com.verifydynamics.netflowscoring.domain.FlowScore;
import com.verifydynamics.netflowscoring.domain.NetFlow;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.task.TopologyContext;

import org.codehaus.jackson.map.ObjectMapper;

import org.apache.storm.tuple.Values;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/*
  join the various streams of tuples together and send onwards only a single aggregated tuple.
*/
public class JoinBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(JoinBolt.class);

    private ExpiringMap<Long, NetFlow> tupleCache;
    private int inputs;
    private ObjectMapper mapper;

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("JoinBolt - initialising cache");

        // initialise map
        tupleCache = ExpiringMap.builder()
                .maxSize(50000) // 200000
                .expiration(240, TimeUnit.SECONDS)
				.expirationListener((key, netFlowBean) -> LOG.error("JoinBolt - record evicted id = " + key) )
                .build();

        // check how many sources
        inputs = context.getThisSources().size();
        LOG.info("JoinBolt - joining " + inputs + " streams");

        // json mapper
        mapper = new ObjectMapper();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        NetFlow cachedNetFlowBean;

        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow) input.getValue(1);

        LOG.info("JoinBolt - record id = " + flowId + " : " + input.getMessageId() + " / " + input.getSourceComponent());

        // DEBUGGUNG ONLY
        /*try {
            LOG.info("JoinBolt - record id = " + flowId + " :  input tuple - " + mapper.writeValueAsString(netFlowBean));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // first check and see if we are 'joining' one stream or if we have a tuple that crossed a single stream
        if ( (inputs == 1) || (netFlowBean.getSplitter_instances() == 1) ) {
			LOG.info("JoinBolt - record id = " + flowId + " : new tuple - single copy");
            collector.emit(new Values(flowId, netFlowBean));
            return;
        }

        // check the cache
        cachedNetFlowBean = tupleCache.get(netFlowBean.getId());

        // not in cache so cache and move on
        if (cachedNetFlowBean == null) {
			LOG.info("JoinBolt - record id = " + flowId + " : new tuple - caching");
			netFlowBean.setCache_counter(1);
            tupleCache.put(netFlowBean.getId(), netFlowBean);
            return;
        }

        // update the cached object with new scores
		LOG.info("JoinBolt - record id = " + flowId + " : cached tuple - updating scores");

        Set<FlowScore> bad_scores = cachedNetFlowBean.getBad_scores();
        Set<FlowScore> good_scores = cachedNetFlowBean.getGood_scores();

        bad_scores.addAll(netFlowBean.getBad_scores());
        good_scores.addAll(netFlowBean.getGood_scores());

        cachedNetFlowBean.setBad_scores(bad_scores);
        cachedNetFlowBean.setGood_scores(good_scores);

        // increase the counter
        cachedNetFlowBean.setCache_counter(cachedNetFlowBean.getCache_counter() + 1);
		LOG.info("JoinBolt - record id = " + flowId + " : cached tuple count = " + cachedNetFlowBean.getCache_counter());

        // check to see if we must put back or send on
        if (cachedNetFlowBean.getCache_counter() >= cachedNetFlowBean.getSplitter_instances()) {
			LOG.info("JoinBolt - record id = " + flowId + " : handled all inputs - move on");
            cachedNetFlowBean.update_scores();
            collector.emit(new Values(flowId, cachedNetFlowBean));
            tupleCache.remove(cachedNetFlowBean.getId());
        } else {
			LOG.info("JoinBolt - record id = " + flowId + " : outstanding tuples - wait");
            tupleCache.put(cachedNetFlowBean.getId(), cachedNetFlowBean);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("flowId", "netFlowBean"));
    }
}
