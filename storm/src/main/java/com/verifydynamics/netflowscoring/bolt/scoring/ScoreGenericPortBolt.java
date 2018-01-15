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

import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;

import java.util.Map;

import com.verifydynamics.netflowscoring.lookup.NumberLookup;

public class ScoreGenericPortBolt extends ScoreBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ScoreGenericPortBolt.class);

	NumberLookup portListLookup;
	boolean isGoodScore;
	String portFileName;
	int protocol;
    int minPackets;

    public ScoreGenericPortBolt( String scoreCategory, String scoreCode, Integer score, boolean isGoodScore, String portFileName, int protocol, int minPackets) {
        super(scoreCategory, scoreCode, score);
		this.isGoodScore = isGoodScore;
		this.portFileName = portFileName;
		this.protocol = protocol;
        this.minPackets = minPackets;
	}

    @Override
    public void prepare(Map conf, TopologyContext context) {
		LOG.info("ScoreGenericPortBolt - initialising reader for " + scoreCode);
		try {
			portListLookup = new NumberLookup(portFileName);
		} catch (Exception e) {
			LOG.error("ScoreGenericPortBolt error", e);
			throw new RuntimeException(e);
		}
	}

}
