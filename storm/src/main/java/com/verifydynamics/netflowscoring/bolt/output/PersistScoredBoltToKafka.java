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

import com.esotericsoftware.kryo.Kryo;
import com.verifydynamics.netflowscoring.domain.FlowScore;
import com.verifydynamics.netflowscoring.domain.NetFlow;
import com.verifydynamics.netflowscoring.domain.ELKScoredFlowDetail;
import com.verifydynamics.netflowscoring.domain.ELKScoredFlow;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class PersistScoredBoltToKafka extends BaseBasicBolt {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PersistScoredBoltToKafka.class);
    Producer<String, String> producer;
    private ObjectMapper mapper;
    String flowTopicName;
    String flowDetailTopicName;

    DateFormat df;

    public PersistScoredBoltToKafka(String flowTopicName, String flowdetailTopicName) {
        this.flowTopicName = flowTopicName;
        this.flowDetailTopicName = flowdetailTopicName;
    }

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("PersistScoredBoltToKafka - initialising");
        KafkaContext kafkaContext = KafkaContext.getInstance();
        producer = kafkaContext.getConnection();

        // for json
        mapper = new ObjectMapper();

        // date formatting
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private ELKScoredFlow copyTuple(NetFlow scored_flow) {
        ELKScoredFlow elkScoredFlow = new ELKScoredFlow();

        elkScoredFlow.setTimestamp(scored_flow.getStart_timestamp_unix_secs());
        elkScoredFlow.setType("scored_flow");
        //gelf.setTimestamp(System.currentTimeMillis()/1000);

        // timestamp for ELK
        elkScoredFlow.setEvent_time_str(df.format(scored_flow.getStart_timestamp_unix_secs() * 1000));

        elkScoredFlow.setFlow_id(scored_flow.getId());
        elkScoredFlow.setMatched(scored_flow.isMatched());
        elkScoredFlow.setStart_timestamp_unix_secs(scored_flow.getStart_timestamp_unix_secs());
        elkScoredFlow.setEnd_timestamp_unix_secs(scored_flow.getEnd_timestamp_unix_secs());
        elkScoredFlow.setProtocol(scored_flow.getProtocol());
        elkScoredFlow.setSrc_ip(scored_flow.getSrc_ip());
        elkScoredFlow.setDst_ip(scored_flow.getDst_ip());
        elkScoredFlow.setSrc_port(scored_flow.getSrc_port());
        elkScoredFlow.setDst_port(scored_flow.getDst_port());
        elkScoredFlow.setPackets_sent(scored_flow.getPackets_sent());
        elkScoredFlow.setPackets_recv(scored_flow.getPackets_recv());
        elkScoredFlow.setBytes_sent(scored_flow.getBytes_sent());
        elkScoredFlow.setBytes_recv(scored_flow.getBytes_recv());
        elkScoredFlow.setFlags(scored_flow.getFlags());

        elkScoredFlow.setSrc_as(scored_flow.getSrc_as());
        elkScoredFlow.setDst_as(scored_flow.getDst_as());
        elkScoredFlow.setFlow_direction(scored_flow.getFlow_direction());

        elkScoredFlow.setGoodness(scored_flow.getGoodness());
        elkScoredFlow.setBadness(scored_flow.getBadness());

        return elkScoredFlow;
    }

    private Set<ELKScoredFlowDetail> copyTupleDetail(NetFlow scored_flow) {
        Set<ELKScoredFlowDetail> elkScoredFlowDetails = new HashSet<ELKScoredFlowDetail>();

        // fill in a object with all the flow info
        ELKScoredFlowDetail elkScoredFlow_core = new ELKScoredFlowDetail();

        elkScoredFlow_core.setTimestamp(scored_flow.getStart_timestamp_unix_secs());
        elkScoredFlow_core.setType("scored_flow_detail");
        //gelf_core.setTimestamp(System.currentTimeMillis() / 1000);

        // timestamp for ELK
        elkScoredFlow_core.setEvent_time_str(df.format(scored_flow.getStart_timestamp_unix_secs() * 1000));

        elkScoredFlow_core.setFlow_id(scored_flow.getId());
        elkScoredFlow_core.setMatched(scored_flow.isMatched());
        elkScoredFlow_core.setStart_timestamp_unix_secs(scored_flow.getStart_timestamp_unix_secs());
        elkScoredFlow_core.setProtocol(scored_flow.getProtocol());
        elkScoredFlow_core.setSrc_ip(scored_flow.getSrc_ip());
        elkScoredFlow_core.setDst_ip(scored_flow.getDst_ip());
        elkScoredFlow_core.setSrc_port(scored_flow.getSrc_port());
        elkScoredFlow_core.setDst_port(scored_flow.getDst_port());
        elkScoredFlow_core.setPackets_sent(scored_flow.getPackets_sent());
        elkScoredFlow_core.setBytes_sent(scored_flow.getBytes_sent());
        elkScoredFlow_core.setPackets_recv(scored_flow.getPackets_recv());
        elkScoredFlow_core.setBytes_recv(scored_flow.getBytes_recv());
        elkScoredFlow_core.setFlags(scored_flow.getFlags());

        elkScoredFlow_core.setSrc_as(scored_flow.getSrc_as());
        elkScoredFlow_core.setDst_as(scored_flow.getDst_as());
        elkScoredFlow_core.setFlow_direction(scored_flow.getFlow_direction());

        // copier
        Kryo kryo = new Kryo();

        // add bad stuff
        for (Object object : scored_flow.getBad_scores()) {
            FlowScore flowScore = (FlowScore) object;

            ELKScoredFlowDetail elkScoredFlowDetail;
            elkScoredFlowDetail = kryo.copy(elkScoredFlow_core);

            elkScoredFlowDetail.setBad_score(flowScore.getScore());
            elkScoredFlowDetail.setBad_score_tag(flowScore.getScore_category() + '-' + flowScore.getScore_code());

            elkScoredFlowDetails.add(elkScoredFlowDetail);
        }

        // add good stuff
        for (Object object : scored_flow.getGood_scores()) {
            FlowScore flowScore = (FlowScore) object;

            ELKScoredFlowDetail elkScoredFlowDetail;
            elkScoredFlowDetail = kryo.copy(elkScoredFlow_core);

            elkScoredFlowDetail.setGood_score(flowScore.getScore());
            elkScoredFlowDetail.setGood_score_tag(flowScore.getScore_category() + '-' + flowScore.getScore_code());

            elkScoredFlowDetails.add(elkScoredFlowDetail);
        }

        return elkScoredFlowDetails;
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        try {
            long flowId = (long) input.getValue(0);
            NetFlow netFlowBean = (NetFlow)input.getValue(1);

            ELKScoredFlow elkScoredFlow = copyTuple(netFlowBean);
            Set<ELKScoredFlowDetail> elkScoredFlowDetails = copyTupleDetail(netFlowBean);

            // primary record
            String jsonString = mapper.writeValueAsString(elkScoredFlow);
            LOG.info("PersistScoredBoltToKafka - record: " + jsonString);
            producer.send(new ProducerRecord<String, String>(flowTopicName, String.valueOf(flowId), jsonString));

            // details
            for (ELKScoredFlowDetail ELKScoredFlowDetail : elkScoredFlowDetails) {
                jsonString = mapper.writeValueAsString(ELKScoredFlowDetail);
                LOG.info("PersistScoredBoltToKafka - record: detail " + jsonString);
                producer.send(new ProducerRecord<String, String>(flowDetailTopicName, String.valueOf(flowId), jsonString));
            }

        } catch (Exception e) {
            LOG.error("PersistScoredBoltToKafka error", e);
            collector.reportError(e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
