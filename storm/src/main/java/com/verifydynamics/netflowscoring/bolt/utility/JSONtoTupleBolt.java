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

import com.verifydynamics.netflowscoring.domain.NetFlow;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONtoTupleBolt extends BaseBasicBolt{

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(JSONtoTupleBolt.class);

	JSONObject jsonObject;

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
		JSONParser jsonParser = new JSONParser();
		NetFlow netFlowBean = new NetFlow();
        try {
            jsonObject = (JSONObject) jsonParser.parse(input.getString(0));
			netFlowBean.setId((long) jsonObject.get("id"));
			netFlowBean.setStart_processing_ms(System.currentTimeMillis());
			netFlowBean.setStart_timestamp_unix_secs((long) jsonObject.get("start_timestamp_unix_secs"));
			netFlowBean.setEnd_timestamp_unix_secs((long) jsonObject.get("end_timestamp_unix_secs"));
			netFlowBean.setProtocol((short) (long)jsonObject.get("protocol"));
			netFlowBean.setSrc_ip((String) jsonObject.get("src_ip"));
			netFlowBean.setDst_ip((String) jsonObject.get("dst_ip"));
			netFlowBean.setSrc_ip_int((long) jsonObject.get("src_ip_int"));
			netFlowBean.setDst_ip_int((long) jsonObject.get("dst_ip_int"));
			netFlowBean.setSrc_port((int) (long) jsonObject.get("src_port"));
			netFlowBean.setDst_port((int) (long) jsonObject.get("dst_port"));
			netFlowBean.setPackets_sent((long) jsonObject.get("packets"));
			netFlowBean.setBytes_sent((long) jsonObject.get("bytes"));
            netFlowBean.setPackets_recv(0);
            netFlowBean.setBytes_recv(0);
			netFlowBean.setFlags((String) jsonObject.get("flags"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        collector.emit(new Values(netFlowBean.getId(), netFlowBean));
        //LOG.info(input.getString(0));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("flowId", "netFlowBean"));
    }
}
