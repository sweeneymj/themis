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
package com.verifydynamics.netflowscoring.storm;

import com.verifydynamics.netflowscoring.bolt.enrichment.MatchFlowBolt;
import com.verifydynamics.netflowscoring.bolt.enrichment.MatchFlowRichBolt;
import com.verifydynamics.netflowscoring.bolt.output.*;
import com.verifydynamics.netflowscoring.domain.NetFlow;
import com.verifydynamics.netflowscoring.bolt.scoring.*;
import com.verifydynamics.netflowscoring.bolt.utility.*;
import com.verifydynamics.netflowscoring.enums.ServiceType;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import com.verifydynamics.netflowscoring.domain.FlowScore;
import com.verifydynamics.netflowscoring.bolt.enrichment.GeoLocationBolt;
import com.verifydynamics.netflowscoring.bolt.enrichment.ASNBolt;
import com.verifydynamics.netflowscoring.bolt.scoring.ScoreSSHBruteForceBolt;

public class NetflowScoreTopology {

    private static final Logger LOG = Logger.getLogger(NetflowScoreTopology.class);

    public static void main(String[] args) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {
        int EXECUTORS = 4;
        int TASKS = 4;
        int MAX_PENDING = 3000;
        int MATCH_DELTA = 0;
        String test_name = "";

        if (args.length > 0) {
            try {
                // Parse the string argument into an integer value.
                EXECUTORS = Integer.parseInt(args[0]);
                TASKS = Integer.parseInt(args[1]);
                MAX_PENDING = Integer.parseInt(args[2]);
                MATCH_DELTA = Integer.parseInt(args[3]);
                test_name = args[4];
            }
            catch (NumberFormatException nfe) {
                // The first argument isn't a valid integer.  Print
                // an error message, then exit with an error code.
                System.out.println("The first four arguments must all be an integer.");
                System.exit(1);
            }
        }

        System.out.println("running with : executors = " + EXECUTORS + " / TASKS = " + TASKS + " / MAX_PENDING = " + MAX_PENDING + " / MATCH_DELTA = " + MATCH_DELTA);

        String randomString = RandomStringUtils.random(10, false, true);

        final BrokerHosts zkrHosts = new ZkHosts("127.0.0.1:2181");
        final String kafkaTopic = "netflow-json-timings";
        final String zkRoot = "/brokers";
        final String clientId = "storm-netflow-consumer-" + randomString;
        final SpoutConfig kafkaConf = new SpoutConfig(zkrHosts, kafkaTopic, zkRoot, clientId);
        kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

        // Build topology to consume message from kafka
        final TopologyBuilder topologyBuilder = new TopologyBuilder();
        // Create KafkaSpout instance using Kafka configuration and add it to topology
        topologyBuilder.setSpout("kafka-spout", new KafkaSpout(kafkaConf), 1);

        // BEGIN - TESTING TOPOLOGIES
        // topologyDummy(EXECUTORS, TASKS, MAX_PENDING);
        // topologyStandard(topologyBuilder, EXECUTORS,  TASKS, EXECUTORS + MATCH_DELTA, TASKS + MATCH_DELTA, MAX_PENDING, "test-full");
        // topologyLinear(topologyBuilder, EXECUTORS, TASKS, MAX_PENDING, test_name);
        // topologyBoltTest(topologyBuilder, EXECUTORS, TASKS, true, test_name);
        // END - TESTING TOPOLOGIES

        // this is the final topology we used
        topologyFinal(topologyBuilder, EXECUTORS,  TASKS, EXECUTORS + MATCH_DELTA, TASKS + MATCH_DELTA, MAX_PENDING, test_name);

        Config conf = new Config();
        //conf.setDebug(true);
        conf.setNumWorkers(1);
        conf.setMessageTimeoutSecs(60);
        conf.setNumAckers(2);
        if (MAX_PENDING > 0) {
            conf.setMaxSpoutPending(MAX_PENDING);
        }
        conf.registerSerialization(NetFlow.class);
        conf.registerSerialization(FlowScore.class);


        StormSubmitter.submitTopology("flowscore-topology", conf, topologyBuilder.createTopology());
    }

    static void topologyFinal(TopologyBuilder topologyBuilder, int executors, int tasks, int match_executors, int match_tasks, int max_pending, String testName) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        // first part of the topology consists of:
        // - flow enrichment
        // - discarding unwanted flows

        // first we convert the incoming JSON to our internal tuple POJO
        topologyBuilder.setBolt("json-to-tuple", new JSONtoTupleBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("kafka-spout");

        // lookup ASN - we need this early on so that we can:
        // - discard unwanted flows early
        // - assist in the matching and sorting
        topologyBuilder.setBolt("resolve-asn", new ASNBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("json-to-tuple");

        // discard unwanted flows as soon as possible
        topologyBuilder.setBolt("discard-flows", new FlowDiscardBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("resolve-asn");

        // ####################################################################################
        // we have two output streams from the discard bolt:

        // stream 1 - match early and froward on
        topologyBuilder.setBolt("match-flows", new MatchFlowBolt(), match_executors)
                .setNumTasks(match_tasks)
                .shuffleGrouping("discard-flows", "INTERESTING STREAM");

        // stream 2 - NO OP - absorb and count the discarded flows
        topologyBuilder.setBolt("discard-timer", new MemoryTimerBolt("discard-tuple-" + testName, false), 1)
                .setNumTasks(1)
                .shuffleGrouping("discard-flows", "DISCARD STREAM");

        //
        // ####################################################################################

        // geolocation enrichment
        topologyBuilder.setBolt("resolve-location", new GeoLocationBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("match-flows");

        // We now split the stream into 3 for efficiency
        topologyBuilder.setBolt("flow-splitter", new FlowSplitterBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("resolve-location");

        // ####################################################################################
        // we have four streams from the spltter
        //
        // "BATCH PROCESSING STREAM"
        //

        // push to redis for batch processing if we interested
        // topologyBuilder.setBolt("scan-candidate-persist", new PersistScanCandidateBolt(2), executors)
        //        .setNumTasks(tasks)
        //        .shuffleGrouping("flow-splitter", "BATCH PROCESSING STREAM");

        // push to kafka for now
        topologyBuilder.setBolt("scan-candidate-persist", new PersistScanCandidateToKafkaBolt(2, "scan-candidates"), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("flow-splitter", "BATCH PROCESSING STREAM");

        //
        //  "INTERESTING STREAM - ALL" Bolts
        //

        // ScoreDarkIPBolt
        topologyBuilder.setBolt("score-dark-ip", new ScoreDarkIPBolt("SUSPICIOUS_TRAFFIC", "DARK_IP", 40), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("flow-splitter", "INTERESTING STREAM - ALL");

        // List based scoring - emerging threats
        topologyBuilder.setBolt("score-emerging-threats-list", new ScoreGenericIPListBolt(false, "/opt/flow_score/data/data_sources/emerging_threats_ips.dat", "IP_LIST", "EMERGING_THREATS", 50), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-dark-ip");

        // List based scoring - alien vault
        topologyBuilder.setBolt("score-alienvault-list", new ScoreGenericIPListBolt(false, "/opt/flow_score/data/data_sources/alien_vault_reputation.dat", "IP_LIST", "ALIENVAULT", 50), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-emerging-threats-list");

        // List based scoring - bad countries
        topologyBuilder.setBolt("score-suspect-country", new ScoreCountryBolt("COUNTRY", "SUSPECT", 80), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-alienvault-list");

        // intelmq
        topologyBuilder.setBolt("score-intelmq", new ScoreIntelMQBolt("INTELMQ", "INTELMQ", 70), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-suspect-country");

        // List based scoring - NDPI known traffic
        topologyBuilder.setBolt("score-ndpi-known-list", new ScoreGenericIPListBolt(true, "/opt/flow_score/data/data_sources/ndpi_good.dat", "IP_LIST", "NDPI_GOOD", 100), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-intelmq");

        //
        //  "INTERESTING STREAM - TCP" Bolts
        //

        // Check for SYN only traffic
        topologyBuilder.setBolt("score-syn-only", new ScorePossibleScanBolt("POSSIBLE_SCAN", "SYN_ONLY", 60), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("flow-splitter", "INTERESTING STREAM - TCP ONLY");

        // Check for SSH brute force
        topologyBuilder.setBolt("score-ssh-brute", new ScoreSSHBruteForceBolt("BRUTE_FORCE", "SSH", 90), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-syn-only");

        // Check for HTTP/HTTPS brute force
        topologyBuilder.setBolt("score-http-brute", new ScoreHTTPBruteForceBolt("BRUTE_FORCE", "HTTP", 90), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-ssh-brute");

        // check for services we know of
        topologyBuilder.setBolt("score-tcp-hosted-service", new ScoreServiceBolt("HOSTED_SERVICES", "TCP", 100, ServiceType.TCP_HOSTED, true, 50, 30), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-http-brute");

        // check for services used
        topologyBuilder.setBolt("score-tcp-remote-service", new ScoreServiceBolt("REMOTE_SERVICES", "TCP", 100, ServiceType.TCP_REMOTE, false, 0, 5), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-tcp-hosted-service");

        // Check for suspicious TCP Ports
        topologyBuilder.setBolt("score-bad-tcp-ports", new ScoreInsecurePortConversationBolt("PORT_LIST", "INSECURE_TCP_TRAFFIC", 50, false, "/opt/flow_score/data/data_sources/tcp_insecure_ports.txt", 6, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-tcp-remote-service");

        // Check for suspicious TCP conversations
        topologyBuilder.setBolt("score-bad-tcp-traffic", new ScoreUnknownPortConversationBolt("PORT_LIST", "UNKNOWN_TCP_TRAFFIC", 70, false, "/opt/flow_score/data/data_sources/tcp_known_ports.txt", 6, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-bad-tcp-ports");

        //
        //  "INTERESTING STREAM - UDP" port specific Bolts
        //

        // check for services we know of - note : low threshold to cater for DNS
        topologyBuilder.setBolt("score-udp-hosted-service", new ScoreServiceBolt("HOSTED_SERVICES", "UDP", 100, ServiceType.UDP_HOSTED, true, 50, 1), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("flow-splitter", "INTERESTING STREAM - UDP ONLY");

        // check for services used - note : low threshold to cater for DNS
        topologyBuilder.setBolt("score-udp-remote-service", new ScoreServiceBolt("REMOTE_SERVICES", "UDP", 100, ServiceType.UDP_REMOTE, false, 0, 1), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-udp-hosted-service");

        // Check for suspicious UDP Ports
        topologyBuilder.setBolt("score-bad-udp-ports", new ScoreInsecurePortConversationBolt("PORT_LIST", "INSECURE_UDP_TRAFFIC", 50, false, "/opt/flow_score/data/data_sources/udp_insecure_ports.txt", 17, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-udp-remote-service");

        // Check for suspicious UDP conversations
        topologyBuilder.setBolt("score-bad-udp-traffic", new ScoreUnknownPortConversationBolt("PORT_LIST", "UNKNOWN_UDP_TRAFFIC", 70, false, "/opt/flow_score/data/data_sources/udp_known_ports.txt", 17, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-bad-udp-ports");

        // ####################################################################################
        // merge and log using flowId as our join key
        topologyBuilder.setBolt("join-streams", new JoinBolt(), executors)
                .setNumTasks(tasks)
                .fieldsGrouping("score-ndpi-known-list", new Fields("flowId"))
                .fieldsGrouping("score-bad-tcp-traffic", new Fields("flowId"))
                .fieldsGrouping("score-bad-udp-traffic", new Fields("flowId"));


        topologyBuilder.setBolt("persist-tuple-kafka", new PersistScoredBoltToKafka("scored-flows-timings", "scored-flows-detail-timings"), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("join-streams");


        //topologyBuilder.setBolt("persist-tuple-db", new PersistScoredBolt(), EXECUTORS).shuffleGrouping("join-streams");


        //topologyBuilder.setBolt("print-messages-tcp", new LoggerBolt(), 8)
        //	.shuffleGrouping("join-streams");

        //topologyBuilder.setBolt("persist-unscored", new PersistUnscoredBolt("ignored-flows"), EXECUTORS)
        //        .setNumTasks(TASKS)
        //        .shuffleGrouping("flow-splitter", "UNINTERESTING STREAM");

        // ##############################################################################################
        //
        // instrumentation
        //

        // time when we see a tuple from the spout
        topologyBuilder.setBolt("input-timer", new MemoryTimerBolt("first-tuple-" + testName, true), 1)
                .setNumTasks(1)
                .shuffleGrouping("kafka-spout");

        // time when we see a tuple reach the end
        topologyBuilder.setBolt("output-timer", new MemoryTimerBolt("last-tuple-" + testName, false), 1)
                .setNumTasks(1)
                .shuffleGrouping("join-streams");
    }

    // All topologies defined below this point are for testing purposes

    static void topologyDummy(int executors, int tasks, int max_pending) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        final BrokerHosts zkrHosts = new ZkHosts("127.0.0.1:2181");
        final String kafkaTopic = "netflow-json";
        final String zkRoot = "/brokers";
        final String clientId = "storm-netflow-consumer";
        final SpoutConfig kafkaConf = new SpoutConfig(zkrHosts, kafkaTopic, zkRoot, clientId);
        kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

        // Build topology to consume message from kafka
        final TopologyBuilder topologyBuilder = new TopologyBuilder();
        // Create KafkaSpout instance using Kafka configuration and add it to topology
        topologyBuilder.setSpout("kafka-spout", new KafkaSpout(kafkaConf), 1);

        // time when we see a tuple from the spout
        topologyBuilder.setBolt("input-timer", new PersistTimerBolt("first-tuple", true), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("kafka-spout");

        // do nothing
        topologyBuilder.setBolt("no-op-bolt", new NoOpBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("kafka-spout");

        // time when we see a tuple reach the end
        topologyBuilder.setBolt("output-timer", new PersistTimerBolt("last-tuple", false), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("no-op-bolt");

        Config conf = new Config();
        //conf.setDebug(true);
        conf.setNumWorkers(1);
        conf.setMessageTimeoutSecs(60);
        conf.setNumAckers(2);
        conf.setMaxSpoutPending(max_pending);
        conf.registerSerialization(NetFlow.class);
        conf.registerSerialization(FlowScore.class);


        StormSubmitter.submitTopology("flowscore-topology", conf, topologyBuilder.createTopology());
    }

    static void topologyStandard(TopologyBuilder topologyBuilder, int executors, int tasks, int match_executors, int match_tasks, int max_pending, String testName) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        // Route the output of Kafka Spout to JSONtoTupleBolt bolt to convert input to a Tuple
        topologyBuilder.setBolt("json-to-tuple", new JSONtoTupleBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("kafka-spout");

        // Route the output of JSONtoTupleBolt bolt to geolookup bolt
        topologyBuilder.setBolt("resolve-location", new GeoLocationBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("json-to-tuple");

        // Route the output of geolookup  bolt to asn bolt
        topologyBuilder.setBolt("resolve-asn", new ASNBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("resolve-location");

        // Try and match flows and update the sent and received counters
        topologyBuilder.setBolt("match-flows", new MatchFlowBolt(), match_executors)
                .setNumTasks(match_tasks)
                .shuffleGrouping("resolve-asn");

        // We now split the stream into 4/5
        topologyBuilder.setBolt("flow-splitter", new FlowSplitterBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("match-flows");

        //
        // "BATCH PROCESSING STREAM"
        //

        // push to redis if we are interested
        topologyBuilder.setBolt("scan-candidate-persist", new PersistScanCandidateBolt(2), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("flow-splitter", "BATCH PROCESSING STREAM");

		//
		//  "INTERESTING STREAM - ALL" Bolts
		//

        // ScoreDarkIPBolt
        topologyBuilder.setBolt("score-dark-ip", new ScoreDarkIPBolt("SUSPICIOUS_TRAFFIC", "DARK_IP", 10), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("flow-splitter", "INTERESTING STREAM - ALL");

        // List based scoring - emerging threats
        topologyBuilder.setBolt("score-emerging-threats-list", new ScoreGenericIPListBolt(false, "/opt/flow_score/data/data_sources/emerging_threats_ips.dat", "IP_LIST", "EMERGING_THREATS", 20), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-dark-ip");

        // List based scoring - alien vault
        topologyBuilder.setBolt("score-alienvault-list", new ScoreGenericIPListBolt(false, "/opt/flow_score/data/data_sources/alien_vault_reputation.dat", "IP_LIST", "ALIENVAULT", 20), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-emerging-threats-list");

        // List based scoring - bad countries
        topologyBuilder.setBolt("score-suspect-country", new ScoreCountryBolt("COUNTRY", "SUSPECT", 30), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-alienvault-list");

        // intelmq
        topologyBuilder.setBolt("score-intelmq", new ScoreIntelMQBolt("INTELMQ", "INTELMQ", 50), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-suspect-country");

        // List based scoring - NDPI known traffic
        topologyBuilder.setBolt("score-ndpi-known-list", new ScoreGenericIPListBolt(true, "/opt/flow_score/data/data_sources/ndpi_good.dat", "IP_LIST", "NDPI_GOOD", 30), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-intelmq");
		//
		//  "INTERESTING STREAM - TCP" Bolts
		//

        // Check for SYN only traffic
        topologyBuilder.setBolt("score-syn-only", new ScorePossibleScanBolt("POSSIBLE_SCAN", "SYN_ONLY", 10), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("flow-splitter", "INTERESTING STREAM - TCP");

        // Check for SSH brute force
        topologyBuilder.setBolt("score-ssh-brute", new ScoreSSHBruteForceBolt("BRUTE_FORCE", "SSH", 50), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-syn-only");

        // Check for HTTP/HTTPS brute force
        topologyBuilder.setBolt("score-http-brute", new ScoreHTTPBruteForceBolt("BRUTE_FORCE", "HTTP", 50), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-ssh-brute");

        // check for services we know of
        topologyBuilder.setBolt("score-tcp-hosted-service", new ScoreServiceBolt("HOSTED_SERVICES", "TCP", 20, ServiceType.TCP_HOSTED, true, 10, 5), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-http-brute");

        // check for services used
        topologyBuilder.setBolt("score-tcp-remote-service", new ScoreServiceBolt("REMOTE_SERVICES", "TCP", 20, ServiceType.TCP_REMOTE, false, 0, 5), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-tcp-hosted-service");


        //
		//  "INTERESTING STREAM - PORTS" port specific Bolts
		//

        // Check for suspicious UDP Ports
        topologyBuilder.setBolt("score-bad-udp-ports", new ScoreInsecurePortConversationBolt("PORT_LIST", "INSECURE_UDP_TRAFFIC", 40, false, "/opt/flow_score/data/data_sources/udp_insecure_ports.txt", 17, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("flow-splitter", "INTERESTING STREAM - PORTS");

        // Check for suspicious TCP Ports
        topologyBuilder.setBolt("score-bad-tcp-ports", new ScoreInsecurePortConversationBolt("PORT_LIST", "INSECURE_TCP_TRAFFIC", 40, false, "/opt/flow_score/data/data_sources/tcp_insecure_ports.txt", 6, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-bad-udp-ports");

        // Check for suspicious UDP conversations
        topologyBuilder.setBolt("score-bad-udp-traffic", new ScoreUnknownPortConversationBolt("PORT_LIST", "UNKNOWN_UDP_TRAFFIC", 20, false, "/opt/flow_score/data/data_sources/udp_known_ports.txt", 17, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-bad-tcp-ports");

        // Check for suspicious TCP conversations
        topologyBuilder.setBolt("score-bad-tcp-traffic", new ScoreUnknownPortConversationBolt("PORT_LIST", "UNKNOWN_TCP_TRAFFIC", 20, false, "/opt/flow_score/data/data_sources/tcp_known_ports.txt", 6, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-bad-udp-traffic");


        // merge and log using flowId as our join key
        topologyBuilder.setBolt("join-streams", new JoinBolt(), executors)
                .setNumTasks(tasks)
			    .fieldsGrouping("score-ndpi-known-list", new Fields("flowId"))
			    .fieldsGrouping("score-tcp-remote-service", new Fields("flowId"))
			    .fieldsGrouping("score-bad-tcp-traffic", new Fields("flowId"));


        // for throughput testing - end in NullBolts
        topologyBuilder.setBolt("persist-tuple-kafka-null", new NullBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("join-streams");

        // time when we see a tuple from the spout
        topologyBuilder.setBolt("input-timer", new MemoryTimerBolt("first-tuple-" + testName, true), 1)
                .setNumTasks(1)
                .shuffleGrouping("kafka-spout");

        // time when we see a tuple reach the end
        topologyBuilder.setBolt("output-timer", new MemoryTimerBolt("last-tuple-" + testName, false), 1)
                .setNumTasks(1)
                .shuffleGrouping(testName);
    }

    static void topologyLinear(TopologyBuilder topologyBuilder, int executors, int tasks, int max_pending, String testName) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        // Route the output of Kafka Spout to JSONtoTupleBolt bolt to convert input to a Tuple
        topologyBuilder.setBolt("json-to-tuple", new JSONtoTupleBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("kafka-spout");

        // Route the output of JSONtoTupleBolt bolt to geolookup bolt
        topologyBuilder.setBolt("resolve-location", new GeoLocationBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("json-to-tuple");

        // Route the output of geolookup  bolt to asn bolt
        topologyBuilder.setBolt("resolve-asn", new ASNBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("resolve-location");

        // Try and match flows and update the sent and received counters
        topologyBuilder.setBolt("match-flows", new MatchFlowBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("resolve-asn");

        // We now discard and re-route
        //topologyBuilder.setBolt("flow-splitter", new FlowDiscardBolt(), executors)
        topologyBuilder.setBolt(testName, new FlowDiscardBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("match-flows");

        /*
        //
        // "BATCH PROCESSING STREAM"
        //

        // push to redis if we are interested

        topologyBuilder.setBolt("scan-candidate-persist", new PersistScanCandidateToKafkaBolt(2, "scan-candidates"), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("flow-splitter", "DISCARD STREAM");

        //
        //  "INTERESTING STREAM" Bolts
        //

        // ScoreDarkIPBolt
        topologyBuilder.setBolt("score-dark-ip", new ScoreDarkIPBolt("SUSPICIOUS_TRAFFIC", "DARK_IP", 10), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("flow-splitter", "INTERESTING STREAM");

        // List based scoring - emerging threats
        topologyBuilder.setBolt("score-emerging-threats-list", new ScoreGenericIPListBolt(false, "/opt/flow_score/data/data_sources/emerging_threats_ips.dat", "IP_LIST", "EMERGING_THREATS", 20), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-dark-ip");

        // List based scoring - alien vault
        topologyBuilder.setBolt("score-alienvault-list", new ScoreGenericIPListBolt(false, "/opt/flow_score/data/data_sources/alien_vault_reputation.dat", "IP_LIST", "ALIENVAULT", 20), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-emerging-threats-list");

        // List based scoring - bad countries
        topologyBuilder.setBolt("score-suspect-country", new ScoreCountryBolt("COUNTRY", "SUSPECT", 30), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-alienvault-list");

        // intelmq
        topologyBuilder.setBolt("score-intelmq", new ScoreIntelMQBolt("INTELMQ", "INTELMQ", 50), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-suspect-country");

        // List based scoring - NDPI known traffic
        topologyBuilder.setBolt("score-ndpi-known-list", new ScoreGenericIPListBolt(true, "/opt/flow_score/data/data_sources/ndpi_good.dat", "IP_LIST", "NDPI_GOOD", 30), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-intelmq");

        // Check for SYN only traffic
        topologyBuilder.setBolt("score-syn-only", new ScorePossibleScanBolt("POSSIBLE_SCAN", "SYN_ONLY", 10), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-ndpi-known-list");

        // Check for SSH brute force
        topologyBuilder.setBolt("score-ssh-brute", new ScoreSSHBruteForceBolt("BRUTE_FORCE", "SSH", 50), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-syn-only");

        // Check for HTTP/HTTPS brute force
        topologyBuilder.setBolt("score-http-brute", new ScoreHTTPBruteForceBolt("BRUTE_FORCE", "HTTP", 50), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-ssh-brute");

        // check for services we know of
        topologyBuilder.setBolt("score-tcp-hosted-service", new ScoreServiceBolt("HOSTED_SERVICES", "TCP", 20, ServiceType.TCP_HOSTED, true, 10, 5), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-http-brute");

        // check for services used
        topologyBuilder.setBolt("score-tcp-remote-service", new ScoreServiceBolt("REMOTE_SERVICES", "TCP", 20, ServiceType.TCP_REMOTE, false, 0, 5), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-tcp-hosted-service");

        // Check for suspicious UDP Ports
        topologyBuilder.setBolt("score-bad-udp-ports", new ScoreInsecurePortConversationBolt("PORT_LIST", "INSECURE_UDP_TRAFFIC", 40, false, "/opt/flow_score/data/data_sources/udp_insecure_ports.txt", 17, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-tcp-remote-service");

        // Check for suspicious TCP Ports
        topologyBuilder.setBolt("score-bad-tcp-ports", new ScoreInsecurePortConversationBolt("PORT_LIST", "INSECURE_TCP_TRAFFIC", 40, false, "/opt/flow_score/data/data_sources/tcp_insecure_ports.txt", 6, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-bad-udp-ports");

        // Check for suspicious UDP conversations
        topologyBuilder.setBolt("score-bad-udp-traffic", new ScoreUnknownPortConversationBolt("PORT_LIST", "UNKNOWN_UDP_TRAFFIC", 20, false, "/opt/flow_score/data/data_sources/udp_known_ports.txt", 17, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-bad-tcp-ports");

        // Check for suspicious TCP conversations
        topologyBuilder.setBolt("score-bad-tcp-traffic", new ScoreUnknownPortConversationBolt("PORT_LIST", "UNKNOWN_TCP_TRAFFIC", 20, false, "/opt/flow_score/data/data_sources/tcp_known_ports.txt", 6, 3), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("score-bad-udp-traffic");


        // for throughput testing - end in NullBolts
        //topologyBuilder.setBolt("persist-tuple-kafka-null", new NullBolt(), executors)
        //        .setNumTasks(tasks)
        //        .shuffleGrouping("score-bad-tcp-traffic");

        //topologyBuilder.setBolt("persist-unscored-null", new NullBolt(), executors)
        //        .setNumTasks(tasks)
        //        .shuffleGrouping("flow-splitter", "UNINTERESTING STREAM");

        //topologyBuilder.setBolt("persist-tuple-kafka", new PersistScoredBoltToKafka("scored-flows", "scored-flows-detail"), executors)
        //        .setNumTasks(tasks)
        //        .shuffleGrouping("score-bad-tcp-traffic");

        //topologyBuilder.setBolt("persist-tuple-db", new PersistScoredBolt(), EXECUTORS).shuffleGrouping("join-streams");


        //topologyBuilder.setBolt("print-messages-tcp", new LoggerBolt(), 8)
        //	.shuffleGrouping("join-streams");

        //topologyBuilder.setBolt("persist-unscored", new PersistUnscoredBolt("ignored-flows"), EXECUTORS)
        //        .setNumTasks(TASKS)
        //        .s
        // huffleGrouping("flow-splitter", "UNINTERESTING STREAM");

        // ##############################################################################################
        //
        // instrumentation
        //
        */

        // time when we see a tuple from the spout
        topologyBuilder.setBolt("input-timer", new MemoryTimerBolt("first-tuple-" + testName, true), 1)
                .setNumTasks(1)
                .shuffleGrouping("kafka-spout");

        // time when we see a tuple reach the end
        topologyBuilder.setBolt("output-timer", new MemoryTimerBolt("last-tuple-" + testName, false), 1)
                .setNumTasks(1)
                .shuffleGrouping(testName, "INTERESTING STREAM");


    }

    // for testing purposes - we want to test different bolts in a single step topology

    static void topologyBoltTest(TopologyBuilder topologyBuilder, int executors, int tasks, boolean doNothing, String testName) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {


        // The first bolt - convert our json to internal class
        topologyBuilder.setBolt("json-to-tuple", new JSONtoTupleBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("kafka-spout");

        // Try and match flows and update the sent and received counters
        topologyBuilder.setBolt(testName, new MatchFlowBolt(), executors)
                .setNumTasks(tasks)
                .shuffleGrouping("json-to-tuple");


        //topologyBuilder.setBolt(testName, new MatchFlowRichBolt(), executors)
        //        .setNumTasks(tasks)
        //        .shuffleGrouping("json-to-tuple");

        // ##############################################################################################
        //
        // instrumentation
        //

        // time when we see a tuple from the spout
        topologyBuilder.setBolt("input-timer", new MemoryTimerBolt("first-tuple-" + testName, true), 1)
                .setNumTasks(1)
                .shuffleGrouping("kafka-spout");


        // time when we see a tuple reach the end
        topologyBuilder.setBolt("output-timer", new MemoryTimerBolt("last-tuple-" + testName, false), 1)
                .setNumTasks(1)
                .shuffleGrouping(testName);

    }


}
