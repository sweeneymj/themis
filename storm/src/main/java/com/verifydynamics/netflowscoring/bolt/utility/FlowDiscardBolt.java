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
import com.verifydynamics.netflowscoring.lookup.HoneyPotPortIgnoreLookup;
import com.verifydynamics.netflowscoring.lookup.ValidProtocolLookup;
import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/*
 The purpose of this bolt is to discard all traffic we are NOT interested in. This includes:
 - non IP traffic (IP protocol not in (1, 6, 17)
 - internal or unknown traffic (our ASN of interest must be on one side or the other)
 - port 23/2323 - this is the honeypot traffic
*/
public class FlowDiscardBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(FlowDiscardBolt.class);

    private ASNLookup asnLookup;
    private HoneyPotPortIgnoreLookup honeyPotPortIgnoreLookup;
    private ValidProtocolLookup validProtocolLookup;
    private Kryo kryo;

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("FlowDiscardBolt - initialising lookup");
        asnLookup = ASNLookup.getInstance();
        honeyPotPortIgnoreLookup = HoneyPotPortIgnoreLookup.getInstance();
        validProtocolLookup = ValidProtocolLookup.getInstance();
        kryo = new Kryo();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        NetFlow netFlowBean;
        NetFlow outboundNetFlowBean;
        boolean discard = false;
        int streamCount = 0;

        netFlowBean = (NetFlow) input.getValue(1);

        LOG.info("FlowDiscardBolt - record id = " + netFlowBean.getId() + " " + input.getMessageId());

        // check protocols first
        if (!validProtocolLookup.exists((int) netFlowBean.getProtocol())) {
            discard = true;
        }

        // we are only interested in inbound traffic but we classify everything
        if (discard == false) {
            if (asnLookup.exists(netFlowBean.getSrc_as())) {
                if (asnLookup.exists(netFlowBean.getDst_as())) {
                    netFlowBean.setFlow_direction("INTERNAL");
                    discard = true;
                } else {
                    netFlowBean.setFlow_direction("OUTBOUND");
                    discard = false;
                }
            } else {
                if (asnLookup.exists(netFlowBean.getDst_as())) {
                    netFlowBean.setFlow_direction("INBOUND");
                    discard = false;
                } else {
                    netFlowBean.setFlow_direction("UNKNOWN");
                    discard = true;
                }
            }
        }

        // finally - discard 23/2323
        if (discard == false) {
            if ( ( honeyPotPortIgnoreLookup.exists(netFlowBean.getSrc_port()) ) || ( honeyPotPortIgnoreLookup.exists(netFlowBean.getDst_port()) ) ) {
                discard = true;
            }
        }

		// we have two outputs: uninteresing data, interesting data and batch processing
        if (discard) {
            outboundNetFlowBean = kryo.copy(netFlowBean);
            collector.emit("DISCARD STREAM", new Values(outboundNetFlowBean.getId(), outboundNetFlowBean));
        } else {
            outboundNetFlowBean = kryo.copy(netFlowBean);
            collector.emit("INTERESTING STREAM", new Values(outboundNetFlowBean.getId(), outboundNetFlowBean));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("INTERESTING STREAM", new Fields("flowId", "netFlowBean"));
        declarer.declareStream("DISCARD STREAM", new Fields("flowId", "netFlowBean"));
    }
}
