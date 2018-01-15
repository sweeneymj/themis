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
import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

// dummy no-op bolt for testing purposes
public class NoOpBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(NoOpBolt.class);


    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("NoOpBolt - initialising");
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {

        LOG.info("NoOpBolt");

        collector.emit(input.getValues());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("netFlowBean"));
    }
}
