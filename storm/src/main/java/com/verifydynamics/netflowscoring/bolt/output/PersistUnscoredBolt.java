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

import com.verifydynamics.netflowscoring.domain.NetFlow;
import com.verifydynamics.netflowscoring.utils.KafkaContext;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;

public class PersistUnscoredBolt extends BaseBasicBolt {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PersistUnscoredBolt.class);

    Producer<String, String> producer;
    private ObjectMapper mapper;
    String topicName;

    public PersistUnscoredBolt (String topicName) {
        this.topicName = topicName;
    }

    @Override
    public void prepare(Map conf, TopologyContext context) {
        KafkaContext kafkaContext = KafkaContext.getInstance();
        producer = kafkaContext.getConnection();

        // json mapper
        mapper = new ObjectMapper();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        try {
            long flowId = (long) input.getValue(0);
            NetFlow netFlowBean = (NetFlow)input.getValue(1);

            String jsonString = mapper.writeValueAsString(netFlowBean);

            producer.send(new ProducerRecord<String, String>(topicName, String.valueOf(flowId), jsonString));

            //LOG.info("PersistUnscoredBolt - record id = " + netFlowBean.getId() + netFlowBean.getSrc_country() + " / " + netFlowBean.getDst_country() + " / " + netFlowBean.getSrc_as() + " / " + netFlowBean.getDst_as());
        } catch (Exception e) {
            LOG.error("PersistUnscoredBolt error", e);
            collector.reportError(e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
