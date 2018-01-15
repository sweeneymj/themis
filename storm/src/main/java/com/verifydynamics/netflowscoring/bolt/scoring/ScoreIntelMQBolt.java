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
package com.verifydynamics.netflowscoring.bolt.scoring;

import com.verifydynamics.netflowscoring.domain.FlowScore;
import com.verifydynamics.netflowscoring.domain.NetFlow;
import com.verifydynamics.netflowscoring.lookup.ASNLookup;
import com.verifydynamics.netflowscoring.utils.RedisContext;
import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

public class ScoreIntelMQBolt extends ScoreBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ScoreIntelMQBolt.class);

    Jedis connection;

    JSONParser jsonParser;

    ASNLookup asnLookup;

    public ScoreIntelMQBolt(String scoreCategory, String scoreCode, Integer score) {
        super(scoreCategory, scoreCode, score);
	}

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("ScoreIntelMQBolt - initialising lookup");

        RedisContext redisContext = RedisContext.getInstance();
        connection = redisContext.getConnection();
        connection.select(14);

        jsonParser = new JSONParser();

        asnLookup = ASNLookup.getInstance();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow)input.getValue(1);

        LOG.info("ScoreIntelMQBolt - record id = " + flowId);

        // check which side to lookup
        String ip_address;
        if (asnLookup.exists(netFlowBean.getSrc_as())) {
            ip_address = netFlowBean.getDst_ip();
        } else {
            ip_address = netFlowBean.getSrc_ip();
        }

        // get data from the intelMQ redis cache
        String key = "intel-mq-" + ip_address;

        Map<String, String> values = connection.hgetAll(key);

        JSONObject jsonObject;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            try {
                LOG.info("ScoreIntelMQBolt - key = " +  key + " : " + entry.getKey());
                jsonObject = (JSONObject) jsonParser.parse(entry.getValue());

                FlowScore flowScore = new FlowScore();
                flowScore.setScore_category(scoreCategory);
                flowScore.setScore(score);
                flowScore.setScore_code(jsonObject.get("classification_type") + " : " + jsonObject.get("classification_taxonomy"));
                netFlowBean.addBad_score(flowScore);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // send on
        collector.emit(new Values(flowId, netFlowBean));
    }
}
