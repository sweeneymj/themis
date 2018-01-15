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
package com.verifydynamics.netflowscoring;

import com.verifydynamics.netflowscoring.domain.FlowScore;
import com.verifydynamics.netflowscoring.domain.NetFlow;
import com.verifydynamics.netflowscoring.enums.ServiceType;
import com.verifydynamics.netflowscoring.factory.ServiceLookupFactory;
import com.verifydynamics.netflowscoring.lookup.GenericServiceLookup;
import com.verifydynamics.netflowscoring.lookup.TCPHostedServiceLookup;
import com.verifydynamics.netflowscoring.lookup.UDPHostedServiceLookup;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.log4j.Logger;
import org.junit.Test;
import sun.nio.ch.Net;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TupleCaching {

    private static final Logger LOG = Logger.getLogger(TupleCaching.class);

    @Test
    public void testCachingATuple () {

        ExpiringMap<Long, NetFlow> tupleCache;
        NetFlow tupleA;
        NetFlow tupleB;


        FlowScore scoreA;
        FlowScore scoreB;

        // initialise map
        tupleCache = ExpiringMap.builder()
                .maxSize(5000)
                .expiration(30, TimeUnit.SECONDS)
                .expirationListener((key, netFlowBean) -> LOG.info("JoinBolt - record evicted id = " + key) )
                .build();

        // A
        tupleA = new NetFlow();
        tupleA.setId(100);

        scoreA = new FlowScore();
        scoreA.setScore_category("TEST A");

        tupleA.addBad_score(scoreA);

        // B
        tupleB = new NetFlow();
        tupleB.setId(100);

        scoreB = new FlowScore();
        scoreB.setScore_category("TEST B");

        tupleB.addBad_score(scoreB);

        tupleCache.put(tupleA.getId(), tupleA);

        NetFlow cachedTuple = tupleCache.get(100L);
        Set<FlowScore> bad_scores = cachedTuple.getBad_scores();

        bad_scores.addAll(tupleB.getBad_scores());

        cachedTuple.setBad_scores(bad_scores);

        // put it back
        tupleCache.put(cachedTuple.getId(), cachedTuple);

        NetFlow lastTuple = tupleCache.get(100L);

    }

}
