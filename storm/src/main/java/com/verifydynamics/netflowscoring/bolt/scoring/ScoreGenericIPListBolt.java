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
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

import com.verifydynamics.netflowscoring.domain.FlowScore;
import com.verifydynamics.netflowscoring.lookup.IPLookup;

public class ScoreGenericIPListBolt extends ScoreBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ScoreGenericIPListBolt.class);

	private IPLookup ipListLookup;
	private boolean isGoodScore;
	private String listFileName;

    public ScoreGenericIPListBolt(boolean isGoodScore, String listFileName, String scoreCategory, String scoreCode, Integer score) {
        super(scoreCategory, scoreCode, score);
		this.isGoodScore = isGoodScore;
		this.listFileName = listFileName;
	}

    @Override
    public void prepare(Map conf, TopologyContext context) {
		LOG.info("ScoreGenericIPListBolt - initialising reader for " + scoreCode);
		try {
			ipListLookup = new IPLookup(listFileName);
		} catch (Exception e) {
			LOG.error("ScoreGenericIPListBolt error", e);
			throw new RuntimeException(e);
		}
	}

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
		FlowScore flowScore;

        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow)input.getValue(1);

        LOG.info("ScoreGenericIPListBolt (" + scoreCode + ") - record id = " + flowId);

		if ((ipListLookup.get(netFlowBean.getSrc_ip()) != null) || (ipListLookup.get(netFlowBean.getDst_ip()) != null)) {
			flowScore = new FlowScore();
            flowScore.setScore_category(scoreCategory);
			flowScore.setScore(score);
			flowScore.setScore_code(scoreCode);
			if (isGoodScore) {
				netFlowBean.addGood_score(flowScore);
			} else {
				netFlowBean.addBad_score(flowScore);
			}
		} 

        collector.emit(new Values(flowId, netFlowBean));
    }

}
