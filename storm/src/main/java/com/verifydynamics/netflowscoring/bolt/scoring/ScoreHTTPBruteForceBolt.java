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
import com.verifydynamics.netflowscoring.lookup.ASNLookup;
import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/*
    based on the research from VanDerToorn2015
 */
public class ScoreHTTPBruteForceBolt extends ScoreBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ScoreHTTPBruteForceBolt.class);

    private ASNLookup asnLookup;

    public ScoreHTTPBruteForceBolt(String scoreCategory, String scoreCode, Integer score) {
        super(scoreCategory, scoreCode, score);
    }

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("ScoreHTTPBruteForceBolt - initialising lookup");
        asnLookup = ASNLookup.getInstance();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
		FlowScore flowScore;

        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow)input.getValue(1);

        LOG.info("ScoreHTTPBruteForceBolt - record id = " + flowId);

		if (netFlowBean.getProtocol() == 6) {
            // only interested in incoming traffic for now - HTTP check first
            if ((asnLookup.exists(netFlowBean.getSrc_as()) && netFlowBean.getSrc_port() == 80)
                    || (asnLookup.exists(netFlowBean.getDst_as()) && netFlowBean.getDst_port() == 80)) {
                // check for small flow and small byte count - indicates possible attack - signatures from research combined
                if (((netFlowBean.getPackets_sent() >= 5) && (netFlowBean.getPackets_sent() <= 12))
                        && ((netFlowBean.getBytes_sent() >= 363) && (netFlowBean.getBytes_sent() <= 1130))) {
                    flowScore = new FlowScore();
                    flowScore.setScore_category(scoreCategory);
                    flowScore.setScore(score);
                    flowScore.setScore_code(scoreCode);
                    netFlowBean.addBad_score(flowScore);
                }
            }

            // incoming HTTPS?
            if ((asnLookup.exists(netFlowBean.getSrc_as()) && netFlowBean.getSrc_port() == 443)
                    || (asnLookup.exists(netFlowBean.getDst_as()) && netFlowBean.getDst_port() == 443)) {
                // check for small flow and small byte count - indicates possible attack - signatures from research combined
                if (((netFlowBean.getPackets_sent() >= 7) && (netFlowBean.getPackets_sent() <= 17))
                        && ((netFlowBean.getBytes_sent() >= 789) && (netFlowBean.getBytes_sent() <= 2885))) {
                    flowScore = new FlowScore();
                    flowScore.setScore_category(scoreCategory);
                    flowScore.setScore(score);
                    flowScore.setScore_code(scoreCode);
                    netFlowBean.addBad_score(flowScore);
                }
            }
        }

        collector.emit(new Values(flowId, netFlowBean));
    }

}
