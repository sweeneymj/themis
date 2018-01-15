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
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseStatefulWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

/**
 * basic scoring bolt
 */
public class ScoreBolt extends BaseBasicBolt {

    String scoreCategory;
    String scoreCode;
    Integer score;

    public ScoreBolt(String scoreCategory, String scoreCode, Integer score) {
        this.scoreCategory = scoreCategory;
        this.scoreCode = scoreCode;
        this.score = score;
    }
    public void execute(Tuple input, BasicOutputCollector collector) {
        // just pass along
        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow)input.getValue(1);
        collector.emit(new Values(flowId, netFlowBean));
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("flowId", "netFlowBean"));
    }
}
