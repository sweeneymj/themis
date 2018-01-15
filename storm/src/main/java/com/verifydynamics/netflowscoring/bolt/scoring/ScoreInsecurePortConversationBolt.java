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

import com.verifydynamics.netflowscoring.domain.FlowScore;
import com.verifydynamics.netflowscoring.domain.NetFlow;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class ScoreInsecurePortConversationBolt extends ScoreGenericPortBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ScoreInsecurePortConversationBolt.class);

    public ScoreInsecurePortConversationBolt(String scoreCategory, String scoreCode, Integer score, boolean isGoodScore, String portFileName, int protocol, int minPackets) {
        super(scoreCategory, scoreCode, score, isGoodScore, portFileName, protocol, minPackets);
	}

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
		FlowScore flowScore;

        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow)input.getValue(1);

        LOG.info("ScoreInsecurePortConversationBolt (" + scoreCode + ") - record id = " + flowId);

		if (netFlowBean.getProtocol() == this.protocol) {
            // traffic conversation to a insucure port
			if ((portListLookup.exists(netFlowBean.getSrc_port())) || (portListLookup.exists(netFlowBean.getDst_port()))) {
                if (netFlowBean.getPackets_sent() >= minPackets) {
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
            }
		}

        collector.emit(new Values(flowId, netFlowBean));
    }

}
