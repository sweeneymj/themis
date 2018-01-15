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
import com.verifydynamics.netflowscoring.enums.ServiceType;
import com.verifydynamics.netflowscoring.factory.ServiceLookupFactory;
import com.verifydynamics.netflowscoring.lookup.ASNLookup;
import com.verifydynamics.netflowscoring.lookup.GenericCountryLookup;
import com.verifydynamics.netflowscoring.lookup.GenericServiceLookup;
import com.verifydynamics.netflowscoring.lookup.NumberLookup;
import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class ScoreCountryBolt extends ScoreBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ScoreCountryBolt.class);

    GenericCountryLookup genericCountryLookup;
    ASNLookup asnLookup;

    public ScoreCountryBolt(String scoreCategory, String scoreCode, Integer score) {
        super(scoreCategory, scoreCode, score);
	}

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("ScoreCountryBolt - initialising lookup");
        genericCountryLookup = GenericCountryLookup.getInstance();
        asnLookup = ASNLookup.getInstance();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow)input.getValue(1);

        LOG.info("ScoreCountryBolt - record id = " + flowId);

        if (genericCountryLookup == null) {
            LOG.error("genericCountryLookup is NULL");
        }

        Double weighting = 0.0;

        // check country code of opposite side
        if (asnLookup.exists(netFlowBean.getSrc_as())) {
            weighting = genericCountryLookup.exists(netFlowBean.getDst_country_code());
        } else if (asnLookup.exists(netFlowBean.getDst_as())) {
            weighting = genericCountryLookup.exists(netFlowBean.getSrc_country_code());
        }

        // if found then score
        if (weighting > 0.0) {
            FlowScore flowScore = new FlowScore();
            flowScore.setScore_category(scoreCategory);
            flowScore.setScore((int)Math.round(score * weighting));
            flowScore.setScore_code(scoreCode);
            netFlowBean.addBad_score(flowScore);
        }

        // send on
        collector.emit(new Values(flowId, netFlowBean));
    }
}
