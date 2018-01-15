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
import com.verifydynamics.netflowscoring.domain.FlowScore;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

// based on the research by Hofstede and Sperotto
public class ScoreSSHBruteForceBolt extends ScoreBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ScoreSSHBruteForceBolt.class);

    FlowScore flowScore;
    NetFlow netFlowBean;
    long flowId;

    public ScoreSSHBruteForceBolt(String scoreCategory, String scoreCode, Integer score) {
        super(scoreCategory, scoreCode, score);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {

        // Long start_ts = System.currentTimeMillis();

        flowId = (long) input.getValue(0);
        netFlowBean = (NetFlow)input.getValue(1);

        LOG.info("ScoreSSHBruteForce - record id = " + flowId);

		// check first - is it SSH?
		if ( (netFlowBean.getProtocol() == 6) &&
			( (netFlowBean.getSrc_port() == 22) || (netFlowBean.getDst_port() == 22) ) ) {
			// check for small flow - indicates possible brute forcing
			if ( (netFlowBean.getPackets_sent() >= 11) || (netFlowBean.getPackets_sent() <= 51) ) {
				flowScore = new FlowScore();
                flowScore.setScore_category(scoreCategory);
				flowScore.setScore(score);
				flowScore.setScore_code(scoreCode);
				netFlowBean.addBad_score(flowScore);
			}
		}

        // Long pre_emit_ts = System.currentTimeMillis();
        collector.emit(new Values(flowId, netFlowBean));
        // Long done_ts = System.currentTimeMillis();
        // LOG.info("ScoreSSHBruteForce - record id = " + flowId + " : total time = " + (done_ts - start_ts) + ", emit time = " + (done_ts - pre_emit_ts));
    }

}
