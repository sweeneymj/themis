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

import com.verifydynamics.netflowscoring.domain.FlowScore;
import com.verifydynamics.netflowscoring.domain.NetFlow;
import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.codehaus.jackson.map.ObjectMapper;

import java.sql.*;
import java.util.Map;

public class PersistScoredBolt extends BaseBasicBolt {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PersistScoredBolt.class);

    // TODO : must come from config or parameters
    private final String url = "jdbc:postgresql://127.0.0.1/NetflowScore";
    private final String user = "nfs_user";
    private final String password = "CREATE USER davide WITH PASSWORD 'jw8s0F4';";

    //private String SQL_SCORED_FLOW = "INSERT INTO public.scored_flow( "
    //        + "id, src_ip, dst_ip, protocol, src_port, dst_port, src_asn, dst_asn, processing_time, src_city, src_country, dst_city, dst_country, good_score, bad_score, src_location, dst_location, src_country_code, dst_country_code, start_time) "
    //        + " VALUES (?, ?::inet, ?::inet, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326), ST_SetSRID(ST_MakePoint(?, ?), 4326), ?, ?, to_timestamp(?));";

    private String SQL_SCORED_FLOW = "INSERT INTO public.scored_flow( "
            + "id, src_ip, dst_ip, protocol, src_port, dst_port, src_asn, dst_asn, processing_time, src_city, src_country, dst_city, dst_country, good_score, bad_score, src_country_code, dst_country_code, start_time) "
            + " VALUES (?, ?::inet, ?::inet, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_timestamp(?));";


    private String SQL_SCORED_FLOW_DETAIL = "INSERT INTO public.scored_flow_detail( "
            + "flow_id, score_category, score_name, score, is_bad_score) "
            + "VALUES (?, ?, ?, ?, ?);";

    private Connection db_connection;
    private PreparedStatement pstmt;
    private PreparedStatement pstmt_detail;

    // for debug only
    private ObjectMapper mapper;

    private int connectDB() {
        try {
            db_connection = DriverManager.getConnection(url, user, password);

            // prepared statements
            pstmt = db_connection.prepareStatement(SQL_SCORED_FLOW);
            pstmt_detail = db_connection.prepareStatement(SQL_SCORED_FLOW_DETAIL);

        } catch (SQLException ex) {
            LOG.error(ex.getMessage());
            LOG.error("PersistScoredBolt : " + ex.getMessage());
            return 0;
        }
        return 1;
    }

    private void persistNetFlowBean (NetFlow netFlowBean) {

        try {
			// refresh scores
			netFlowBean.update_scores();
            // TODO: must add the counters
            pstmt.setLong(1, netFlowBean.getId());
            pstmt.setString(2, netFlowBean.getSrc_ip());
            pstmt.setString(3, netFlowBean.getDst_ip());
            pstmt.setInt(4, netFlowBean.getProtocol());
            pstmt.setInt(5, netFlowBean.getSrc_port());
            pstmt.setInt(6, netFlowBean.getDst_port());
            pstmt.setInt(7, netFlowBean.getSrc_as());
            pstmt.setInt(8, netFlowBean.getDst_as());
            pstmt.setLong(9, System.currentTimeMillis() - netFlowBean.getStart_processing_ms());
            pstmt.setString(10, netFlowBean.getSrc_city());
            pstmt.setString(11, netFlowBean.getSrc_country());
            pstmt.setString(12, netFlowBean.getDst_city());
            pstmt.setString(13, netFlowBean.getDst_country());
            pstmt.setInt(14, netFlowBean.getGoodness());
            pstmt.setInt(15, netFlowBean.getBadness());
            pstmt.setString(16, netFlowBean.getSrc_country_code());
            pstmt.setString(17, netFlowBean.getDst_country_code());
            pstmt.setLong(18, netFlowBean.getStart_timestamp_unix_secs());
            //pstmt.setDouble(16, netFlowBean.getSrc_longitude() == null ? 0.0 : netFlowBean.getSrc_longitude());
            //pstmt.setDouble(17, netFlowBean.getSrc_latitude() == null ? 0.0 : netFlowBean.getSrc_latitude());
            //pstmt.setDouble(18, netFlowBean.getDst_longitude() == null ? 0.0 : netFlowBean.getDst_longitude());
            //pstmt.setDouble(19, netFlowBean.getDst_latitude() == null ? 0.0 : netFlowBean.getDst_latitude());

            boolean succeeded = pstmt.execute();

			// good first
            for (Object object : netFlowBean.getGood_scores()) {
                FlowScore flowScore = (FlowScore) object;

				pstmt_detail.setLong(1, netFlowBean.getId());
                pstmt_detail.setString(2, flowScore.getScore_category());
				pstmt_detail.setString(3, flowScore.getScore_code());
				pstmt_detail.setInt(4, flowScore.getScore());
				pstmt_detail.setBoolean(5, false);
		
				succeeded = pstmt_detail.execute();
			}

			// bad first
            for (Object object : netFlowBean.getBad_scores()) {
                FlowScore flowScore = (FlowScore) object;

                pstmt_detail.setLong(1, netFlowBean.getId());
                pstmt_detail.setString(2, flowScore.getScore_category());
                pstmt_detail.setString(3, flowScore.getScore_code());
                pstmt_detail.setInt(4, flowScore.getScore());
				pstmt_detail.setBoolean(5, true);
		
				succeeded = pstmt_detail.execute();
			}
        } catch (SQLException ex) {
            LOG.error("PersistScoredBolt : id = " + netFlowBean.getId() + " - " + ex.getMessage());
        }
    }

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("PersistScoredBolt - initialising db");
        connectDB();

        // for debug only
        // mapper = new ObjectMapper();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        long elapsed;
        try {
            long flowId = (long) input.getValue(0);
            NetFlow netFlowBean = (NetFlow)input.getValue(1);
            LOG.info("PersistScoredBolt - record id = " + netFlowBean.getId() + netFlowBean.getSrc_country() + " / " + netFlowBean.getDst_country() + " / " + netFlowBean.getSrc_as() + " / " + netFlowBean.getDst_as());
            persistNetFlowBean(netFlowBean);
        } catch (Exception e) {
            LOG.error("PersistScoredBolt error", e);
            collector.reportError(e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
