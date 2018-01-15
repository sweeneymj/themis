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

public class ScorePossibleScanBolt extends ScoreBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ScorePossibleScanBolt.class);


    public ScorePossibleScanBolt(String scoreCategory, String scoreCode, Integer score) {
        super(scoreCategory, scoreCode, score);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
		FlowScore flowScore;

        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow)input.getValue(1);

        LOG.info("ScorePossibleScanBolt - record id = " + flowId);

		if ( (netFlowBean.getFlags().equals("....S.")) // standard scan
                || (netFlowBean.getFlags().equals(".....F")) // FIN probe
                || (netFlowBean.getFlags().equals(".P...F")) // xmas probe
                || (netFlowBean.getFlags().equals("......")) // null probe
                ) {
			flowScore = new FlowScore();
            flowScore.setScore_category(scoreCategory);
			flowScore.setScore(score);
			flowScore.setScore_code(scoreCode);
			netFlowBean.addBad_score(flowScore);
		}

        collector.emit(new Values(flowId, netFlowBean));
    }

}
