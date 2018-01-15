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
import com.verifydynamics.netflowscoring.domain.ScanSuspect;
import com.verifydynamics.netflowscoring.enums.ScanSuspectType;
import com.verifydynamics.netflowscoring.lookup.ASNLookup;
import com.verifydynamics.netflowscoring.utils.KafkaContext;
import com.verifydynamics.netflowscoring.utils.RedisContext;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;


public class PersistScanCandidateToKafkaBolt extends BaseBasicBolt {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PersistScanCandidateToKafkaBolt.class);

    Producer<String, String> producer;
    private ObjectMapper mapper;
    DateFormat df;

    private int maxPackets;
    ASNLookup asnLookup;
    String topicName;

    public PersistScanCandidateToKafkaBolt(int maxPackets, String topicName) {
        this.maxPackets = maxPackets;
        this.topicName = topicName;
    }

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("PersistScanCandidateToKafkaBolt - initialising");
        KafkaContext kafkaContext = KafkaContext.getInstance();
        producer = kafkaContext.getConnection();

        // ASN lookup
        this.asnLookup = ASNLookup.getInstance();

        // for json
        mapper = new ObjectMapper();

        // date formatting
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    void persistScanCandidate (NetFlow netFlowBean) {
        String scan_src_ip;
        String scan_dst_ip;

        int scan_dst_port;

        long packets_sent;
        long bytes_sent;
        long packets_recv;
        long bytes_recv;

        if ((netFlowBean.getProtocol() == 6) && (netFlowBean.getPackets_sent() <= maxPackets)) {
            // work out which port and ip to use based on AS and service type
            if (asnLookup.exists(netFlowBean.getSrc_as())) {
                scan_src_ip = netFlowBean.getDst_ip();
                scan_dst_ip = netFlowBean.getSrc_ip();
                scan_dst_port = netFlowBean.getSrc_port();
                packets_sent = netFlowBean.getPackets_recv();
                packets_recv = netFlowBean.getPackets_sent();
                bytes_sent = netFlowBean.getBytes_recv();
                bytes_recv = netFlowBean.getBytes_sent();
            } else { // ASSUMPTION : we are only looking at flows between our AS and another AS
                scan_src_ip = netFlowBean.getSrc_ip();
                scan_dst_ip = netFlowBean.getDst_ip();
                scan_dst_port = netFlowBean.getDst_port();
                scan_dst_port = netFlowBean.getSrc_port();
                packets_sent = netFlowBean.getPackets_sent();
                packets_recv = netFlowBean.getPackets_recv();
                bytes_sent = netFlowBean.getBytes_sent();
                bytes_recv = netFlowBean.getBytes_recv();
            }

            String key;

            // vertical first
            ScanSuspect verticalSuspect = new ScanSuspect();
            key = "scan-candidate-vertical-" + scan_src_ip + ":" + scan_dst_ip;
            verticalSuspect.setId(netFlowBean.getId());
            verticalSuspect.setKey(key);
            verticalSuspect.setType(ScanSuspectType.VERTICAL);
            verticalSuspect.setStart_timestamp_unix_secs(netFlowBean.getStart_timestamp_unix_secs());
            verticalSuspect.setEvent_time_str(df.format(netFlowBean.getStart_timestamp_unix_secs() * 1000));
            verticalSuspect.setDst_port(scan_dst_port);
            verticalSuspect.setDst_ip(scan_dst_ip);
            verticalSuspect.setSrc_ip(scan_src_ip);
            verticalSuspect.setBytes_sent(bytes_sent);
            verticalSuspect.setBytes_recv(bytes_recv);
            verticalSuspect.setPackets_sent(packets_sent);
            verticalSuspect.setPackets_recv(packets_recv);

            // horizontal next
            ScanSuspect horizontalSuspect = new ScanSuspect();
            key = "scan-candidate-horizontal-" + scan_src_ip + ":" + scan_dst_port;
            horizontalSuspect.setId(netFlowBean.getId());
            horizontalSuspect.setKey(key);
            horizontalSuspect.setType(ScanSuspectType.HORIZONTAL);
            horizontalSuspect.setStart_timestamp_unix_secs(netFlowBean.getStart_timestamp_unix_secs());
            horizontalSuspect.setEvent_time_str(df.format(netFlowBean.getStart_timestamp_unix_secs() * 1000));
            horizontalSuspect.setDst_port(scan_dst_port);
            horizontalSuspect.setDst_ip(scan_dst_ip);
            horizontalSuspect.setSrc_ip(scan_src_ip);
            horizontalSuspect.setBytes_sent(bytes_sent);
            horizontalSuspect.setBytes_recv(bytes_recv);
            horizontalSuspect.setPackets_sent(packets_sent);
            horizontalSuspect.setPackets_recv(packets_recv);

            // distributed last
            ScanSuspect distributedSuspect = new ScanSuspect();
            key = "scan-candidate-botnet-" + scan_dst_port;
            distributedSuspect.setId(netFlowBean.getId());
            distributedSuspect.setKey(key);
            distributedSuspect.setType(ScanSuspectType.DISTRIBUTED);
            distributedSuspect.setStart_timestamp_unix_secs(netFlowBean.getStart_timestamp_unix_secs());
            distributedSuspect.setEvent_time_str(df.format(netFlowBean.getStart_timestamp_unix_secs() * 1000));
            distributedSuspect.setDst_port(scan_dst_port);
            distributedSuspect.setDst_ip(scan_dst_ip);
            distributedSuspect.setSrc_ip(scan_src_ip);
            distributedSuspect.setBytes_sent(bytes_sent);
            distributedSuspect.setBytes_recv(bytes_recv);
            distributedSuspect.setPackets_sent(packets_sent);
            distributedSuspect.setPackets_recv(packets_recv);

            // send to kafka
            String jsonString;
            try {
                jsonString = mapper.writeValueAsString(verticalSuspect);
                producer.send(new ProducerRecord<String, String>(topicName, String.valueOf(netFlowBean.getId()), jsonString));
                jsonString = mapper.writeValueAsString(horizontalSuspect);
                producer.send(new ProducerRecord<String, String>(topicName, String.valueOf(netFlowBean.getId()), jsonString));
                jsonString = mapper.writeValueAsString(distributedSuspect);
                producer.send(new ProducerRecord<String, String>(topicName, String.valueOf(netFlowBean.getId()), jsonString));
            } catch (Exception e) {
                LOG.error("PersistScanCandidateToKafkaBolt error", e);
            }
        }
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow) input.getValue(1);

        LOG.info("PersistScanCandidateToKafkaBolt - record id = " + flowId);

        persistScanCandidate(netFlowBean);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
