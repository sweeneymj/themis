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
package com.verifydynamics.netflowscoring.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class NetFlow implements Serializable {
    private static final long serialVersionUID = 1L;

	// control attributes
    long id;
    long start_processing_ms;
    int splitter_instances = 0;
    int cache_counter = 0;
    boolean matched = false;

    // NetFlow attributes
    long start_timestamp_unix_secs;
    long end_timestamp_unix_secs;
    long duration_unix_secs;
    short protocol;
    String src_ip;
    String dst_ip;
    long src_ip_int;
    long dst_ip_int;
    int src_port;
    int dst_port;
    long packets_sent;
    long bytes_sent;
    long packets_recv;
    long bytes_recv;
    int flows;
    String flags;
    short tos;
    int bps;
    int pps;

    // geoip data
    String src_city;
    String src_country;
    String src_country_code;
    String dst_city;
    String dst_country;
    String dst_country_code;
    Double src_latitude;
    Double src_longitude;
    Double dst_latitude;
    Double dst_longitude;

    // ISP data
    int src_as;
    int dst_as;
    String flow_direction;

    // score related attributes
    int goodness;
    int badness;
    Set<FlowScore> good_scores;
    Set<FlowScore> bad_scores;

	// control attributes
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStart_processing_ms() {
        return start_processing_ms;
    }

    public void setStart_processing_ms(long start_processing_ms) {
        this.start_processing_ms = start_processing_ms;
    }

    public int getSplitter_instances() {
        return splitter_instances;
    }

    public void setSplitter_instances(int splitter_instances) {
        this.splitter_instances = splitter_instances;
    }

    public int getCache_counter() {
        return cache_counter;
    }

    public void setCache_counter(int cache_counter) {
        this.cache_counter = cache_counter;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    // NetFlow attributes
    public long getStart_timestamp_unix_secs() {
        return start_timestamp_unix_secs;
    }

    public void setStart_timestamp_unix_secs(long start_timestamp_unix_secs) {
        this.start_timestamp_unix_secs = start_timestamp_unix_secs;
    }

    public long getEnd_timestamp_unix_secs() {
        return end_timestamp_unix_secs;
    }

    public void setEnd_timestamp_unix_secs(long end_timestamp_unix_secs) {
        this.end_timestamp_unix_secs = end_timestamp_unix_secs;
    }

    public long getDuration_unix_secs() {
        return duration_unix_secs;
    }

    public void setDuration_unix_secs(long duration_unix_secs) {
        this.duration_unix_secs = duration_unix_secs;
    }

    public short getProtocol() {
        return protocol;
    }

    public void setProtocol(short protocol) {
        this.protocol = protocol;
    }

    public String getSrc_ip() {
        return src_ip;
    }

    public void setSrc_ip(String src_ip) {
        this.src_ip = src_ip;
    }

    public String getDst_ip() {
        return dst_ip;
    }

    public void setDst_ip(String dst_ip) {
        this.dst_ip = dst_ip;
    }

    public long getSrc_ip_int() {
        return src_ip_int;
    }

    public void setSrc_ip_int(long src_ip_int) {
        this.src_ip_int = src_ip_int;
    }

    public long getDst_ip_int() {
        return dst_ip_int;
    }

    public void setDst_ip_int(long dst_ip_int) {
        this.dst_ip_int = dst_ip_int;
    }

    public int getSrc_port() {
        return src_port;
    }

    public void setSrc_port(int src_port) {
        this.src_port = src_port;
    }

    public int getDst_port() {
        return dst_port;
    }

    public void setDst_port(int dst_port) {
        this.dst_port = dst_port;
    }

    public long getPackets_sent() {
        return packets_sent;
    }

    public void setPackets_sent(long packets_sent) {
        this.packets_sent = packets_sent;
    }

    public long getBytes_sent() {
        return bytes_sent;
    }

    public void setBytes_sent(long bytes_sent) {
        this.bytes_sent = bytes_sent;
    }

    public long getPackets_recv() {
        return packets_recv;
    }

    public void setPackets_recv(long packets_recv) {
        this.packets_recv = packets_recv;
    }

    public long getBytes_recv() {
        return bytes_recv;
    }

    public void setBytes_recv(long bytes_recv) {
        this.bytes_recv = bytes_recv;
    }

    public int getFlows() {
        return flows;
    }

    public void setFlows(int flows) {
        this.flows = flows;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public short getTos() {
        return tos;
    }

    public void setTos(short tos) {
        this.tos = tos;
    }

    public int getBps() {
        return bps;
    }

    public void setBps(int bps) {
        this.bps = bps;
    }

    public int getPps() {
        return pps;
    }

    public void setPps(int pps) {
        this.pps = pps;
    }

	// geoip
    public String getSrc_city() {
        return src_city;
    }

    public void setSrc_city(String src_city) {
        this.src_city = src_city;
    }

    public String getSrc_country() {
        return src_country;
    }

    public void setSrc_country(String src_country) {
        this.src_country = src_country;
    }

    public String getSrc_country_code() {
        return src_country_code;
    }

    public void setSrc_country_code(String src_country_code) {
        this.src_country_code = src_country_code;
    }

    public String getDst_city() {
        return dst_city;
    }

    public void setDst_city(String dst_city) {
        this.dst_city = dst_city;
    }

    public String getDst_country() {
        return dst_country;
    }

    public String getDst_country_code() {
        return dst_country_code;
    }

    public void setDst_country_code(String dst_country_code) {
        this.dst_country_code = dst_country_code;
    }

    public void setDst_country(String dst_country) {
        this.dst_country = dst_country;
    }

    public Double getSrc_latitude() {
        return src_latitude;
    }

    public void setSrc_latitude(Double src_latitude) {
        this.src_latitude = src_latitude;
    }

    public Double getSrc_longitude() {
        return src_longitude;
    }

    public void setSrc_longitude(Double src_longitude) {
        this.src_longitude = src_longitude;
    }

    public Double getDst_latitude() {
        return dst_latitude;
    }

    public void setDst_latitude(Double dst_latitude) {
        this.dst_latitude = dst_latitude;
    }

    public Double getDst_longitude() {
        return dst_longitude;
    }

    public void setDst_longitude(Double dst_longitude) {
        this.dst_longitude = dst_longitude;
    }

    // ISP data
    public int getSrc_as() {
        return src_as;
    }

    public void setSrc_as(int src_as) {
        this.src_as = src_as;
    }

    public int getDst_as() {
        return dst_as;
    }

    public void setDst_as(int dst_as) {
        this.dst_as = dst_as;
    }

    public String getFlow_direction() {
        return flow_direction;
    }

    public void setFlow_direction(String flow_direction) {
        this.flow_direction = flow_direction;
    }

    // score related attributes
    public int getGoodness() {
        return goodness;
    }

    public void setGoodness(int goodness) {
        this.goodness = goodness;
    }

    public int getBadness() {
        return badness;
    }

    public void setBadness(int badness) {
        this.badness = badness;
    }

    public Set<FlowScore> getGood_scores() {
        if (good_scores == null) return new HashSet<>();
        return good_scores;
    }

    public void setGood_scores(Set<FlowScore> good_scores) {
        this.good_scores = good_scores;
    }

	public void addGood_score(FlowScore flowScore) {
		if (good_scores == null) {
			good_scores = new HashSet<FlowScore>();
		}
		good_scores.add(flowScore);
		goodness = goodness + flowScore.getScore();
	}

    public Set<FlowScore> getBad_scores() {
        if (bad_scores == null) return new HashSet<FlowScore>();
        return bad_scores;
    }

    public void setBad_scores(Set<FlowScore> bad_scores) {
        this.bad_scores = bad_scores;
    }

	public void addBad_score(FlowScore flowScore) {
		if (bad_scores == null) {
			bad_scores = new HashSet<FlowScore>();
		}
		bad_scores.add(flowScore);
		badness = badness + flowScore.getScore();
	}

	public void update_scores() {
		// the good
		this.goodness = 0;
		if (this.good_scores != null) {
            for (Object object : this.good_scores) {
                FlowScore flowScore = (FlowScore) object;
				this.goodness = this.goodness + flowScore.getScore();
			}
		}

		// the bad
		this.badness = 0;
		if (this.bad_scores != null) {
            for (Object object : this.bad_scores) {
                FlowScore flowScore = (FlowScore) object;
                this.badness = this.badness + flowScore.getScore();
            }
		}
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetFlow netFlow = (NetFlow) o;

        if (id != netFlow.id) return false;
        if (start_processing_ms != netFlow.start_processing_ms) return false;
        if (splitter_instances != netFlow.splitter_instances) return false;
        if (cache_counter != netFlow.cache_counter) return false;
        if (matched != netFlow.matched) return false;
        if (start_timestamp_unix_secs != netFlow.start_timestamp_unix_secs) return false;
        if (end_timestamp_unix_secs != netFlow.end_timestamp_unix_secs) return false;
        if (duration_unix_secs != netFlow.duration_unix_secs) return false;
        if (protocol != netFlow.protocol) return false;
        if (src_ip_int != netFlow.src_ip_int) return false;
        if (dst_ip_int != netFlow.dst_ip_int) return false;
        if (src_port != netFlow.src_port) return false;
        if (dst_port != netFlow.dst_port) return false;
        if (packets_sent != netFlow.packets_sent) return false;
        if (bytes_sent != netFlow.bytes_sent) return false;
        if (packets_recv != netFlow.packets_recv) return false;
        if (bytes_recv != netFlow.bytes_recv) return false;
        if (flows != netFlow.flows) return false;
        if (tos != netFlow.tos) return false;
        if (bps != netFlow.bps) return false;
        if (pps != netFlow.pps) return false;
        if (src_as != netFlow.src_as) return false;
        if (dst_as != netFlow.dst_as) return false;
        if (goodness != netFlow.goodness) return false;
        if (badness != netFlow.badness) return false;
        if (src_ip != null ? !src_ip.equals(netFlow.src_ip) : netFlow.src_ip != null) return false;
        if (dst_ip != null ? !dst_ip.equals(netFlow.dst_ip) : netFlow.dst_ip != null) return false;
        if (flags != null ? !flags.equals(netFlow.flags) : netFlow.flags != null) return false;
        if (src_city != null ? !src_city.equals(netFlow.src_city) : netFlow.src_city != null) return false;
        if (src_country != null ? !src_country.equals(netFlow.src_country) : netFlow.src_country != null) return false;
        if (src_country_code != null ? !src_country_code.equals(netFlow.src_country_code) : netFlow.src_country_code != null)
            return false;
        if (dst_city != null ? !dst_city.equals(netFlow.dst_city) : netFlow.dst_city != null) return false;
        if (dst_country != null ? !dst_country.equals(netFlow.dst_country) : netFlow.dst_country != null) return false;
        if (dst_country_code != null ? !dst_country_code.equals(netFlow.dst_country_code) : netFlow.dst_country_code != null)
            return false;
        if (src_latitude != null ? !src_latitude.equals(netFlow.src_latitude) : netFlow.src_latitude != null)
            return false;
        if (src_longitude != null ? !src_longitude.equals(netFlow.src_longitude) : netFlow.src_longitude != null)
            return false;
        if (dst_latitude != null ? !dst_latitude.equals(netFlow.dst_latitude) : netFlow.dst_latitude != null)
            return false;
        if (dst_longitude != null ? !dst_longitude.equals(netFlow.dst_longitude) : netFlow.dst_longitude != null)
            return false;
        if (flow_direction != null ? !flow_direction.equals(netFlow.flow_direction) : netFlow.flow_direction != null)
            return false;
        if (good_scores != null ? !good_scores.equals(netFlow.good_scores) : netFlow.good_scores != null) return false;
        return bad_scores != null ? bad_scores.equals(netFlow.bad_scores) : netFlow.bad_scores == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (start_processing_ms ^ (start_processing_ms >>> 32));
        result = 31 * result + splitter_instances;
        result = 31 * result + cache_counter;
        result = 31 * result + (matched ? 1 : 0);
        result = 31 * result + (int) (start_timestamp_unix_secs ^ (start_timestamp_unix_secs >>> 32));
        result = 31 * result + (int) (end_timestamp_unix_secs ^ (end_timestamp_unix_secs >>> 32));
        result = 31 * result + (int) (duration_unix_secs ^ (duration_unix_secs >>> 32));
        result = 31 * result + (int) protocol;
        result = 31 * result + (src_ip != null ? src_ip.hashCode() : 0);
        result = 31 * result + (dst_ip != null ? dst_ip.hashCode() : 0);
        result = 31 * result + (int) (src_ip_int ^ (src_ip_int >>> 32));
        result = 31 * result + (int) (dst_ip_int ^ (dst_ip_int >>> 32));
        result = 31 * result + src_port;
        result = 31 * result + dst_port;
        result = 31 * result + (int) (packets_sent ^ (packets_sent >>> 32));
        result = 31 * result + (int) (bytes_sent ^ (bytes_sent >>> 32));
        result = 31 * result + (int) (packets_recv ^ (packets_recv >>> 32));
        result = 31 * result + (int) (bytes_recv ^ (bytes_recv >>> 32));
        result = 31 * result + flows;
        result = 31 * result + (flags != null ? flags.hashCode() : 0);
        result = 31 * result + (int) tos;
        result = 31 * result + bps;
        result = 31 * result + pps;
        result = 31 * result + (src_city != null ? src_city.hashCode() : 0);
        result = 31 * result + (src_country != null ? src_country.hashCode() : 0);
        result = 31 * result + (src_country_code != null ? src_country_code.hashCode() : 0);
        result = 31 * result + (dst_city != null ? dst_city.hashCode() : 0);
        result = 31 * result + (dst_country != null ? dst_country.hashCode() : 0);
        result = 31 * result + (dst_country_code != null ? dst_country_code.hashCode() : 0);
        result = 31 * result + (src_latitude != null ? src_latitude.hashCode() : 0);
        result = 31 * result + (src_longitude != null ? src_longitude.hashCode() : 0);
        result = 31 * result + (dst_latitude != null ? dst_latitude.hashCode() : 0);
        result = 31 * result + (dst_longitude != null ? dst_longitude.hashCode() : 0);
        result = 31 * result + src_as;
        result = 31 * result + dst_as;
        result = 31 * result + (flow_direction != null ? flow_direction.hashCode() : 0);
        result = 31 * result + goodness;
        result = 31 * result + badness;
        result = 31 * result + (good_scores != null ? good_scores.hashCode() : 0);
        result = 31 * result + (bad_scores != null ? bad_scores.hashCode() : 0);
        return result;
    }
}

