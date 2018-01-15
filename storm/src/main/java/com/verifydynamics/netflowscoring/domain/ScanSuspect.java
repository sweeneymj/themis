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

import com.verifydynamics.netflowscoring.enums.ScanSuspectType;

/****************************************************************************
 * stores the score and the score code from a test
 ****************************************************************************/

public class ScanSuspect {
    private static final long serialVersionUID = 1L;

    long id;
    String  key;
    ScanSuspectType type;

    // timestamps for elk TODO: update hashcode and equals
    String event_time_str;

    long start_timestamp_unix_secs;

    String src_ip;
    String dst_ip;
    int dst_port;

    long packets_sent;
    long bytes_sent;
    long packets_recv;
    long bytes_recv;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ScanSuspectType getType() {
        return type;
    }

    public void setType(ScanSuspectType type) {
        this.type = type;
    }

    public String getEvent_time_str() {
        return event_time_str;
    }

    public void setEvent_time_str(String event_time_str) {
        this.event_time_str = event_time_str;
    }

    public long getStart_timestamp_unix_secs() {
        return start_timestamp_unix_secs;
    }

    public void setStart_timestamp_unix_secs(long start_timestamp_unix_secs) {
        this.start_timestamp_unix_secs = start_timestamp_unix_secs;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScanSuspect)) return false;

        ScanSuspect that = (ScanSuspect) o;

        if (bytes_recv != that.bytes_recv) return false;
        if (bytes_sent != that.bytes_sent) return false;
        if (dst_port != that.dst_port) return false;
        if (id != that.id) return false;
        if (packets_recv != that.packets_recv) return false;
        if (packets_sent != that.packets_sent) return false;
        if (start_timestamp_unix_secs != that.start_timestamp_unix_secs) return false;
        if (dst_ip != null ? !dst_ip.equals(that.dst_ip) : that.dst_ip != null) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (src_ip != null ? !src_ip.equals(that.src_ip) : that.src_ip != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (start_timestamp_unix_secs ^ (start_timestamp_unix_secs >>> 32));
        result = 31 * result + (src_ip != null ? src_ip.hashCode() : 0);
        result = 31 * result + (dst_ip != null ? dst_ip.hashCode() : 0);
        result = 31 * result + dst_port;
        result = 31 * result + (int) (packets_sent ^ (packets_sent >>> 32));
        result = 31 * result + (int) (bytes_sent ^ (bytes_sent >>> 32));
        result = 31 * result + (int) (packets_recv ^ (packets_recv >>> 32));
        result = 31 * result + (int) (bytes_recv ^ (bytes_recv >>> 32));
        return result;
    }
}
