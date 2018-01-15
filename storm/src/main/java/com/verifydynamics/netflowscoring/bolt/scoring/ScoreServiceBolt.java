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
import com.verifydynamics.netflowscoring.lookup.GenericServiceLookup;
import org.apache.log4j.Logger;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/*
  this bolt scores flows to or from a host that match expected traffic.
  'expected' traffic is defined as 'services' either hosted on a host or accessed by the host
  on remote hosts. An example of a hosted service is SMTP on a mail server- any traffic originating
  from port 25 on the host is expected and can be scored positively. a remote services example would
  be HTTP(S) for a proxy - any flows to port 80 or 443 would be expected and  can be scored positively.
  if the doNegativeScoring flag is set then any traffic not configured for a host is scored negatively
*/
public class ScoreServiceBolt extends ScoreBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ScoreServiceBolt.class);

	boolean doNegativeScoring;
    int minPackets;
    int negativeScore;

    ServiceType serviceType;

    GenericServiceLookup serviceLookup;
    private ASNLookup asnLookup;

    public ScoreServiceBolt(String scoreCategory, String scoreCode, Integer score, ServiceType serviceType, boolean doNegativeScoring, int negativeScore, int minPackets) {
        super(scoreCategory, scoreCode, score);
        this.serviceType = serviceType;
        this.minPackets = minPackets;
        this.doNegativeScoring = doNegativeScoring;
        this.negativeScore = negativeScore;
	}

    @Override
    public void prepare(Map conf, TopologyContext context) {
        LOG.info("ScoreServiceBolt - initialising lookup");
        asnLookup = ASNLookup.getInstance();
        serviceLookup = ServiceLookupFactory.getServiceLookup(serviceType);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        long flowId = (long) input.getValue(0);
        NetFlow netFlowBean = (NetFlow)input.getValue(1);

        LOG.info("ScoreServiceBolt - record id = " + flowId);

        if (serviceLookup == null) {
            LOG.error("serviceLookup is NULL");
        }

        // first check - bigger than our threshold?
        if (netFlowBean.getPackets_sent() >= minPackets) {
            // we want the ip from our AS but port depends on hosted vs remote
            String host_address;
            int host_port;

            // work out which port and ip to use based on AS and service type
            if  (asnLookup.exists(netFlowBean.getSrc_as())) {
                host_address = netFlowBean.getSrc_ip();
                if ( serviceType == ServiceType.TCP_HOSTED || serviceType == ServiceType.UDP_HOSTED ) {
                    host_port = netFlowBean.getSrc_port();
                } else { // remote
                    host_port = netFlowBean.getDst_port();
                }
            } else { // ASSUMPTION : we are only looking at flows between our AS and another AS
                host_address = netFlowBean.getDst_ip();
                if ( serviceType == ServiceType.TCP_HOSTED || serviceType == ServiceType.UDP_HOSTED ) {
                    host_port = netFlowBean.getDst_port();
                } else { // remote
                    host_port = netFlowBean.getSrc_port();
                }
            }

            LOG.info("ScoreServiceBolt - record id = " + flowId + " : host_address + " + host_address + " and host port = " + host_port);

            if (serviceLookup.exists(host_address, host_port)) {
                FlowScore flowScore = new FlowScore();
                flowScore.setScore_category(scoreCategory);
                flowScore.setScore(score);
                flowScore.setScore_code(scoreCode);
                netFlowBean.addGood_score(flowScore);
            } else {
                // no ip/port matches - if negative scoring then check if only IP exists
                if (doNegativeScoring && serviceLookup.exists(host_address)) {
                    FlowScore negflowScore = new FlowScore();
                    negflowScore.setScore_category(scoreCategory);
                    negflowScore.setScore(negativeScore);
                    negflowScore.setScore_code(scoreCode);
                    netFlowBean.addBad_score(negflowScore);
                }
            }
        }
        collector.emit(new Values(flowId, netFlowBean));
    }
}
