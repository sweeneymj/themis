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

import com.verifydynamics.netflowscoring.domain.NetFlow;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.maxmind.geoip.*;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import java.util.Map;

public class ASNBolt extends BaseBasicBolt {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ASNBolt.class);

	private LookupService asnLookupService;
	
    @Override
    public void prepare(Map conf, TopologyContext context) {
		LOG.info("ASNBolt - initialising reader");
		try {
            // TODO : get path from config
			asnLookupService = new LookupService("/opt/flow_score/data/data_sources/GeoIPASNum.dat");
		} catch (Exception e) {
            LOG.error("ASNBolt error", e);
			throw new RuntimeException(e);
        }
	}

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
		NetFlow netFlowBean;
		String asnDetail;
		Iterable<String> result;
		String theASN;

		try {
			// extract our domain
            long flowId = (long) input.getValue(0);
            netFlowBean = (NetFlow) input.getValue(1);
			LOG.info("ASNBolt - record id = " + flowId);

			// lookup asn's
			try {
				asnDetail = asnLookupService.getOrg(netFlowBean.getSrc_ip());
				result = Splitter.on(' ').split(asnDetail);
				theASN = CharMatcher.DIGIT.retainFrom(result.iterator().next());
				netFlowBean.setSrc_as(Integer.parseInt(theASN));
			} catch (Exception e) {
				netFlowBean.setSrc_as(0);
			}

			try {
				asnDetail = asnLookupService.getOrg(netFlowBean.getDst_ip());
				result = Splitter.on(' ').split(asnDetail);
				theASN = CharMatcher.DIGIT.retainFrom(result.iterator().next());
				netFlowBean.setDst_as(Integer.parseInt(theASN));
			} catch (Exception e) {
				netFlowBean.setDst_as(0);
			}

			// send on
			collector.emit(new Values(netFlowBean.getId(), netFlowBean));
		} catch (Exception e) {
			LOG.error("LoggerBolt error", e);
			collector.reportError(e);
		}  
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("flowId", "netFlowBean"));
    }
}
