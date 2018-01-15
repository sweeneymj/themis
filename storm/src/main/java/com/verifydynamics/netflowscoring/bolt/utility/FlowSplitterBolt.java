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

import com.esotericsoftware.kryo.Kryo;
import com.verifydynamics.netflowscoring.domain.NetFlow;
import com.verifydynamics.netflowscoring.lookup.ASNLookup;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/*
  split the flow of tuples into multiple streams for parallel processing, number and
  name of streams is hardcoded so this can be done much better
*/
public class FlowSplitterBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(FlowSplitterBolt.class);

    private ASNLookup asnLookup;
    private Kryo kryo;

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("FlowSplitterBolt - initialising lookup");
        asnLookup = ASNLookup.getInstance();
        kryo = new Kryo();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        NetFlow netFlowBean;
        NetFlow outboundNetFlowBean;
        int outboundStream = 0;
        int streamCount = 0;

        netFlowBean = (NetFlow) input.getValue(1);

        LOG.info("FlowSplitterBolt - record id = " + netFlowBean.getId() + " " + input.getMessageId());

        // how many copies? dont count batch processing stream
        // - firstly everyone goes down the "ALL" lane
        streamCount = 1;

        // TCP flows go down two more but one is terminal (batch processing)
        if (netFlowBean.getProtocol() == 6) {
            streamCount += 1;
        }

        // UDP flows fow down one more
        if (netFlowBean.getProtocol() == 17) {
            streamCount += 1;
        }

        netFlowBean.setSplitter_instances(streamCount);

		// we have four outputs, uninteresing data, TCP interesting data, batch, ip scoring data and port scoring
        // for each tupe create a copy of netFlowBean and emit down the "ALL"
        outboundNetFlowBean = kryo.copy(netFlowBean);
        collector.emit("INTERESTING STREAM - ALL", new Values(outboundNetFlowBean.getId(), outboundNetFlowBean));

        // only send TCP down the batch and TCP streams
        if (netFlowBean.getProtocol() == 6) {
            outboundNetFlowBean = kryo.copy(netFlowBean);
            collector.emit("BATCH PROCESSING STREAM", new Values(outboundNetFlowBean.getId(), outboundNetFlowBean));
            outboundNetFlowBean = kryo.copy(netFlowBean);
            collector.emit("INTERESTING STREAM - TCP ONLY", new Values(outboundNetFlowBean.getId(), outboundNetFlowBean));
        }

        // only send UDP down the UDP only stream
        if (netFlowBean.getProtocol() == 17) {
            outboundNetFlowBean = kryo.copy(netFlowBean);
            collector.emit("INTERESTING STREAM - UDP ONLY", new Values(outboundNetFlowBean.getId(),outboundNetFlowBean));
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("INTERESTING STREAM - ALL", new Fields("flowId", "netFlowBean"));
        declarer.declareStream("INTERESTING STREAM - TCP ONLY", new Fields("flowId", "netFlowBean"));
        declarer.declareStream("INTERESTING STREAM - UDP ONLY", new Fields("flowId", "netFlowBean"));
        declarer.declareStream("BATCH PROCESSING STREAM", new Fields("flowId", "netFlowBean"));
    }
}
