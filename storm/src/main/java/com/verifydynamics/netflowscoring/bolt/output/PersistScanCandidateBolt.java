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
import com.verifydynamics.netflowscoring.lookup.ASNLookup;
import com.verifydynamics.netflowscoring.utils.RedisContext;
import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.Map;


public class PersistScanCandidateBolt extends BaseBasicBolt {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PersistScanCandidateBolt.class);

    private int maxPackets;
    ASNLookup asnLookup;
    Jedis connection;

    public PersistScanCandidateBolt(int maxPackets) {
        this.maxPackets = maxPackets;
    }

    @Override
    public void prepare(Map conf, TopologyContext context) {
        // redis connection
        RedisContext redisContext = RedisContext.getInstance();
        this.connection = redisContext.getConnection();

        // ASN lookup
        this.asnLookup = ASNLookup.getInstance();
    }

    void persistScanCandidate (NetFlow netFlowBean) {
        try {

            String scan_src_ip;
            String scan_dst_ip;

            int scan_dst_port;

            String port_scan_key;
            String host_scan_key;

            if ((netFlowBean.getProtocol() == 6) && (netFlowBean.getPackets_sent() <= maxPackets)) {
                // work out which port and ip to use based on AS and service type
                if (asnLookup.exists(netFlowBean.getSrc_as())) {
                    scan_src_ip = netFlowBean.getDst_ip();
                    scan_dst_ip = netFlowBean.getSrc_ip();
                    scan_dst_port = netFlowBean.getSrc_port();
                } else { // ASSUMPTION : we are only looking at flows between our AS and another AS
                    scan_src_ip = netFlowBean.getSrc_ip();
                    scan_dst_ip = netFlowBean.getDst_ip();
                    scan_dst_port = netFlowBean.getDst_port();
                }

                host_scan_key = scan_src_ip + ":" + scan_dst_ip;
                port_scan_key = scan_src_ip + ":" + scan_dst_port;

                // send to redis
                String key;
                String field;

                Transaction transaction = connection.multi();

                String id_field = "flow_id-" + String.valueOf(netFlowBean.getId());

                // vertical scan first
                key = "scan-candidate-vertical-" + scan_src_ip + ":" + scan_dst_ip;

                transaction.hincrBy(key, "packets", netFlowBean.getPackets_sent());
                transaction.hincrBy(key, "bytes", netFlowBean.getBytes_sent());
                transaction.hincrBy(key, "flows", 1);
                field = "port-" + scan_dst_port;
                transaction.hincrBy(key, field, 1);
                transaction.hsetnx(key, "first_seen", String.valueOf(netFlowBean.getStart_timestamp_unix_secs()));
                transaction.hset(key, "last_seen", String.valueOf(netFlowBean.getStart_timestamp_unix_secs()));
                transaction.hset(key, id_field, String.valueOf(netFlowBean.getId()));
                transaction.expire(key, 3600 * 8);

                // horizontal scan next
                key = "scan-candidate-horizontal-" + scan_src_ip + ":" + scan_dst_port;

                transaction.hincrBy(key, "packets", netFlowBean.getPackets_sent());
                transaction.hincrBy(key, "bytes", netFlowBean.getBytes_sent());
                transaction.hincrBy(key, "flows", 1);
                field = "dst_ip-" + scan_dst_ip;
                transaction.hincrBy(key, field, 1);
                transaction.hsetnx(key, "first_seen", String.valueOf(netFlowBean.getStart_timestamp_unix_secs()));
                transaction.hset(key, "last_seen", String.valueOf(netFlowBean.getStart_timestamp_unix_secs()));
                transaction.hset(key, id_field, String.valueOf(netFlowBean.getId()));
                transaction.expire(key, 3600 * 8);

                // AxB scan
                key = "scan-candidate-botnet-" + scan_dst_port;

                field = scan_src_ip + ":" + scan_dst_ip;
                transaction.hincrBy(key, field, 1);
                transaction.hsetnx(key, "first_seen", String.valueOf(netFlowBean.getStart_timestamp_unix_secs()));
                transaction.hset(key, "last_seen", String.valueOf(netFlowBean.getStart_timestamp_unix_secs()));
                transaction.hset(key, id_field, String.valueOf(netFlowBean.getId()));
                transaction.expire(key, 3600 * 8);

                transaction.exec();

            }
        } catch (Exception e) {
            LOG.error("PersistScanCandidateBolt error", e);
        }
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow) input.getValue(1);

        LOG.info("PersistScanCandidateBolt - record id = " + flowId);

        persistScanCandidate(netFlowBean);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
