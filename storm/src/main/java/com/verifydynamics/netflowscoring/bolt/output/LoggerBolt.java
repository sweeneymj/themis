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
package com.verifydynamics.netflowscoring.bolt.output;

import com.verifydynamics.netflowscoring.domain.NetFlow;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class LoggerBolt extends BaseBasicBolt{

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LoggerBolt.class);
	private int taskId;

    private ObjectMapper mapper;

	@Override
    public void prepare(Map conf, TopologyContext context) {
		this.taskId = context.getThisTaskId();
        mapper = new ObjectMapper();
	}

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
		try {
            NetFlow netFlowBean = (NetFlow)input.getValue(1);

			long elapsed = System.currentTimeMillis() - netFlowBean.getStart_processing_ms();

			String jsonString = mapper.writeValueAsString(netFlowBean);

			LOG.info("LoggerBolt task id " + taskId + " / " + input.getSourceStreamId() + " - record id = " + netFlowBean.getId() + " [" + elapsed + "] " + input.getMessageId() + " / " + jsonString);
		} catch (Exception e) {
			LOG.error("LoggerBolt error", e);
			collector.reportError(e);
		}   
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("message"));
    }
}
