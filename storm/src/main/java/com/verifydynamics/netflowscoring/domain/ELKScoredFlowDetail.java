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

public class ELKScoredFlowDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    // GELF attributes
    long timestamp;
    String type;

    // timestamps for elk TODO: update hashcode and equals
    String event_time_str;

	// control attributes
    long flow_id;

    boolean matched = false;

    // NetFlow attributes
    long start_timestamp_unix_secs;
    short protocol;
    String src_ip;
    String dst_ip;
    int src_port;
    int dst_port;
    long packets_sent;
    long bytes_sent;
    long packets_recv;
    long bytes_recv;
    String flags;

        // ISP data
    int src_as;
    int dst_as;
    String flow_direction;

    // score related attributes
    int good_score;
    int bad_score;
    String good_score_tag;
    String bad_score_tag;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEvent_time_str() {
        return event_time_str;
    }

    public void setEvent_time_str(String event_time_str) {
        this.event_time_str = event_time_str;
    }

    public long getFlow_id() {
        return flow_id;
    }

    public void setFlow_id(long flow_id) {
        this.flow_id = flow_id;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public long getStart_timestamp_unix_secs() {
        return start_timestamp_unix_secs;
    }

    public void setStart_timestamp_unix_secs(long start_timestamp_unix_secs) {
        this.start_timestamp_unix_secs = start_timestamp_unix_secs;
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

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

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

    public int getGood_score() {
        return good_score;
    }

    public void setGood_score(int good_score) {
        this.good_score = good_score;
    }

    public int getBad_score() {
        return bad_score;
    }

    public void setBad_score(int bad_score) {
        this.bad_score = bad_score;
    }

    public String getGood_score_tag() {
        return good_score_tag;
    }

    public void setGood_score_tag(String good_score_tag) {
        this.good_score_tag = good_score_tag;
    }

    public String getBad_score_tag() {
        return bad_score_tag;
    }

    public void setBad_score_tag(String bad_score_tag) {
        this.bad_score_tag = bad_score_tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ELKScoredFlowDetail that = (ELKScoredFlowDetail) o;

        if (timestamp != that.timestamp) return false;
        if (flow_id != that.flow_id) return false;
        if (start_timestamp_unix_secs != that.start_timestamp_unix_secs) return false;
        if (protocol != that.protocol) return false;
        if (src_port != that.src_port) return false;
        if (dst_port != that.dst_port) return false;
        if (packets_sent != that.packets_sent) return false;
        if (bytes_sent != that.bytes_sent) return false;
        if (packets_recv != that.packets_recv) return false;
        if (bytes_recv != that.bytes_recv) return false;
        if (src_as != that.src_as) return false;
        if (dst_as != that.dst_as) return false;
        if (good_score != that.good_score) return false;
        if (bad_score != that.bad_score) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (src_ip != null ? !src_ip.equals(that.src_ip) : that.src_ip != null) return false;
        if (dst_ip != null ? !dst_ip.equals(that.dst_ip) : that.dst_ip != null) return false;
        if (flags != null ? !flags.equals(that.flags) : that.flags != null) return false;
        if (flow_direction != null ? !flow_direction.equals(that.flow_direction) : that.flow_direction != null)
            return false;
        if (good_score_tag != null ? !good_score_tag.equals(that.good_score_tag) : that.good_score_tag != null)
            return false;
        return bad_score_tag != null ? bad_score_tag.equals(that.bad_score_tag) : that.bad_score_tag == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (flow_id ^ (flow_id >>> 32));
        result = 31 * result + (int) (start_timestamp_unix_secs ^ (start_timestamp_unix_secs >>> 32));
        result = 31 * result + (int) protocol;
        result = 31 * result + (src_ip != null ? src_ip.hashCode() : 0);
        result = 31 * result + (dst_ip != null ? dst_ip.hashCode() : 0);
        result = 31 * result + src_port;
        result = 31 * result + dst_port;
        result = 31 * result + (int) (packets_sent ^ (packets_sent >>> 32));
        result = 31 * result + (int) (bytes_sent ^ (bytes_sent >>> 32));
        result = 31 * result + (int) (packets_recv ^ (packets_recv >>> 32));
        result = 31 * result + (int) (bytes_recv ^ (bytes_recv >>> 32));
        result = 31 * result + (flags != null ? flags.hashCode() : 0);
        result = 31 * result + src_as;
        result = 31 * result + dst_as;
        result = 31 * result + (flow_direction != null ? flow_direction.hashCode() : 0);
        result = 31 * result + good_score;
        result = 31 * result + bad_score;
        result = 31 * result + (good_score_tag != null ? good_score_tag.hashCode() : 0);
        result = 31 * result + (bad_score_tag != null ? bad_score_tag.hashCode() : 0);
        return result;
    }
}

