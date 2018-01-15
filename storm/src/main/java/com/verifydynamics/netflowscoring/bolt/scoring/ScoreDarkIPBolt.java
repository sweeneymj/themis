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

import com.verifydynamics.netflowscoring.domain.NetFlow;
import com.verifydynamics.netflowscoring.lookup.ASNLookup;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

import com.verifydynamics.netflowscoring.domain.FlowScore;
import com.verifydynamics.netflowscoring.lookup.IPLookup;
import com.verifydynamics.netflowscoring.lookup.NumberLookup;

public class ScoreDarkIPBolt extends ScoreBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ScoreDarkIPBolt.class);

	private IPLookup activeIPLookup;
	private ASNLookup asnLookup;

    public ScoreDarkIPBolt(String scoreCategory, String scoreCode, Integer score) {
        super(scoreCategory, scoreCode, score);
    }

    @Override
    public void prepare(Map conf, TopologyContext context) {
		LOG.info("GeoLocationBolt - initialising reader");
		try {
			activeIPLookup = new IPLookup("/opt/flow_score/data/data_sources/active_hosts.dat");
			asnLookup = ASNLookup.getInstance();
		} catch (Exception e) {
			LOG.error("ScoreDarkIPBolt error", e);
			throw new RuntimeException(e);
		}
	}

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        FlowScore flowScore;

        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow)input.getValue(1);

        LOG.info("ScoreDarkIPBolt - record id = " + flowId);

		if (asnLookup.exists(netFlowBean.getSrc_as()) && activeIPLookup.get(netFlowBean.getSrc_ip()) == null) {
            LOG.info("ScoreDarkIPBolt - result found for " + netFlowBean.getSrc_as() + " / " + netFlowBean.getSrc_ip());
			flowScore = new FlowScore();
            flowScore.setScore_category(scoreCategory);
			flowScore.setScore(score);
			flowScore.setScore_code(scoreCode);
			netFlowBean.addBad_score(flowScore);
		} 

		if (asnLookup.exists(netFlowBean.getDst_as()) && activeIPLookup.get(netFlowBean.getDst_ip()) == null) {
            LOG.info("ScoreDarkIPBolt - result found for " + netFlowBean.getDst_as() + " / " + netFlowBean.getDst_ip());
			flowScore = new FlowScore();
            flowScore.setScore_category(scoreCategory);
			flowScore.setScore(score);
			flowScore.setScore_code(scoreCode);
			netFlowBean.addBad_score(flowScore);
		} 

        collector.emit(new Values(flowId, netFlowBean));
    }
}
