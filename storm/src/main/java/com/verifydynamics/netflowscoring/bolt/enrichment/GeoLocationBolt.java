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
package com.verifydynamics.netflowscoring.bolt.enrichment;

import com.maxmind.geoip2.record.Location;
import com.verifydynamics.netflowscoring.domain.NetFlow;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.maxmind.geoip2.model.*;
import com.maxmind.geoip2.*;

import java.io.File;
import java.net.InetAddress;
import java.util.Map;

public class GeoLocationBolt extends BaseBasicBolt{

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(GeoLocationBolt.class);

	private DatabaseReader reader;
	
    @Override
    public void prepare(Map conf, TopologyContext context) {
		LOG.info("GeoLocationBolt - initialising reader");
		try {
            // TODO : get path and file from config or param
			File database = new File("/opt/flow_score/data/data_sources/GeoLite2-City.mmdb");
			reader = new DatabaseReader.Builder(database).build();
		} catch (Exception e) {
            LOG.error("GeoLocationBolt error", e);
			throw new RuntimeException(e);
        }
	}

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
		NetFlow netFlowBean;
		InetAddress ipAddress;
		CityResponse response;

		try {
			// extract our domain

            long flowId = (long) input.getValue(0);
            netFlowBean = (NetFlow) input.getValue(1);

			LOG.info("GeoLocationBolt - record id = " + flowId);

			// lookup locations
			ipAddress = InetAddress.getByName(netFlowBean.getSrc_ip());

			try {
				response = reader.city(ipAddress);
				netFlowBean.setSrc_city(response.getCity().getName() != null? response.getCity().getName() : "unknown");
				netFlowBean.setSrc_country(response.getCountry().getName() != null? response.getCountry().getName() : "unknown");
				netFlowBean.setSrc_country_code(response.getCountry().getIsoCode() != null? response.getCountry().getIsoCode() : "  ");
                Location location = response.getLocation();
                if (location != null) {
                    netFlowBean.setSrc_latitude(location.getLatitude());
                    netFlowBean.setSrc_longitude(location.getLongitude());
                } else {
                    netFlowBean.setSrc_latitude(0.0);
                    netFlowBean.setSrc_longitude(0.0);
				}
			} catch (Exception e) {
				netFlowBean.setSrc_city("unknown");
				netFlowBean.setSrc_country("unknown");
                netFlowBean.setSrc_latitude(0.0);
                netFlowBean.setSrc_longitude(0.0);
			}

			ipAddress = InetAddress.getByName(netFlowBean.getDst_ip());
			
			try {
				response = reader.city(ipAddress);
				netFlowBean.setDst_city(response.getCity().getName() != null? response.getCity().getName() : "unknown");
				netFlowBean.setDst_country(response.getCountry().getName() != null? response.getCountry().getName() : "unknown");
				netFlowBean.setDst_country_code(response.getCountry().getIsoCode() != null? response.getCountry().getIsoCode() : "  ");
                Location location = response.getLocation();
                if (location != null) {
                    netFlowBean.setDst_latitude(location.getLatitude());
                    netFlowBean.setDst_longitude(location.getLongitude());
                } else {
                    netFlowBean.setDst_latitude(0.0);
                    netFlowBean.setDst_longitude(0.0);
                }
			} catch (Exception e) {
				netFlowBean.setDst_city("unknown");
				netFlowBean.setDst_country("unknown");
                netFlowBean.setDst_latitude(0.0);
                netFlowBean.setDst_longitude(0.0);
			}
			
			// send on
            collector.emit(new Values(netFlowBean.getId(), netFlowBean));
		} catch (Exception e) {
			LOG.error("GeoLocationBolt error", e);
			collector.reportError(e);
		}  
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("flowId", "netFlowBean"));
    }
}
