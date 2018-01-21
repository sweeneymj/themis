# Themis -  NetFlow scoring and tagging

Source repository for themis, a NetFlow scoring and tagging framework for threat detection.

This project is the POC work done as part of my MSc thesis under Prof Barry Irwin of the Computer Science department at Rhodes University, Grahamstown. This project extends the work presented at http://researchspace.csir.co.za/dspace/handle/10204/9693. The instructions presented are a rough guideline and are not intended as a detailed how-to.

# Source Code

The source for Themis is laid out in this repository as follows:

 * [intelmq](./intelmq)    # custom intelmq plugin
 * [scripts](./scripts) # a number of sample utility scripts
* [sql](./sql) # PostgreSQL setup scripts
 * [storm](./storm) # Apache Storm topology Java source
 
# Dependencies

The framework requires the following applications to be installed and running:

Application |Version | Installation
---------------|------------|--------------
  Apache Zookeeper | 3.4.9 | manually
  Apache Kafka | 0.10.2.0| manually
  Apache Storm | 1.0.3| manually
  IntelMQ | 1.1.0| manually
  Logstash | 5.0.0-beta1 | using apt
  ElasticSearch | 5.5.1| using apt
  Kibana | 5.5.1| using apt
  PostgreSQL | 9.5| using apt
  Python | 2.7| using apt
  Redis | 3.0.6| using apt

All applications installed manually were installed under /opt/flow_score/.

# Database Setup

Themis requires access to a PostgreSQL database for storage of lookup/configuration data and for (optional) persistence of scored flow records. The database can be setup using the scripts in the SQL subdirectory. If different credentials are used for the database user then the Java source must be updated to match. The database setup instructions are in the SQL directory.

# IntelMQ Setup

IntelMQ (https://github.com/certtools/intelmq) collects and processes threat intelligence from multiple security feeds, enriches the data and stores the results in a harmonised data format. A custom IntelMQ output bot was implemented for Themis. This bot stores the aggregated data in a simple format in a Redis database using the IP address of the threat source as a key. This information is then used for scoring in Themis. The source and installation instructions for this are under the IntelMQ directory.

# Kafka queues

The following queues need to be created in the installed Kafka instance:

| Topic  | Purpose |
| -------| --------
netflow-json | flow ingestion topic
ignored-flows | for discarded flows
scored-flows | output queue for scored flow records
scored-flows-detail | output queue for scored flow tag records
scan-candidates | output queue for potential scan flows records

For the project the queue retention parameter was set to 1209600000 (14 days). Change as required for your purposes.

# Configuration

There are a number of different configuration requirements. The first type are stored on the file system in the ```/opt/flow_score/data/data_sources/```  directory:

1. **Internal Hosts**
The list of IP addresses of hosts on the internal (i.e. 'our' network). These must be stored in a file using the MaxMind IP db format (https://www.maxmind.com). The configuration file must be named 'active_hosts.dat'.
2. **Internal Autonomous System**
The list of ASN's for the internal (i.e. 'our' network). These must be stored in a plain text file listing one ASN per line. The file must be named 'asn.txt'.
3. **Geolocation Data**
For geolocation data the free to use MaxMind IP address and ASN databases are used. These must be named 'GeoLite2-City.mmdb' and 'GeoIPASNum.dat' respectively.
4. **Black Lists**
In the POC two blacklists are used: AlienVault and Emerging Threats. These must be converted to MaxMind format and stored as 'alien_vault_reputation.dat' and 'emerging_threats_ips.dat' respectively.
5. **White List**
IP white list data must be stored in a file called 'ndpi_good.dat'. The data was extracted from the list of IP's used by the OpenNDPI project for classifying traffic from sites such as Google, AWS, etc.
6. **Port Data**
A number of configuration files are used for listing known and insecure TCP/UDP ports. The file format consists of a single port per line. The configuration files required are 'tcp_insecure_ports.txt', 'tcp_known_ports.txt', 'udp_insecure_ports.txt' and 'udp_known_ports.txt'.
7. **Protocols**
A single file containing the IP protocols of interest is required. The file, named 'valid_protocols.txt', must contain a list of protocol numbers one per line.

In the /scripts/maxmind/ directory there is a sample Perl script (generate_maxmind_db.pl) that can be used to convert a comma separated file with IP address data into the MaxMind format. 

The second set of configuration options are for listing hosted and remote services. The configuration is stored in the 'list_known_host_service' table in the PostgreSQL database. The table is defined as follows:

```SQL
CREATE TABLE list_known_host_service (
    id bigint DEFAULT nextval('flow_score_detail_id_seq'::regclass) NOT NULL,
    list_known_host_id bigint,
    protocol integer,
    port integer,
    is_hosted boolean,
    description character varying(100)
);
```
This table is populated with the IP address, protocol and port number of hosted (i.e. local) or remote services. This is used to identify expected traffic patterns from services that are local (e.g. known Web servers) or remote (e.g. outgoing SMTP traffic).

# Compiling and Installation

The Themis topology can be compiled and deployed as follows:
- In the ./storm/ directory run the following command to build: `mvn clean package`.
- Deploy using storm: `/opt/flow_score/apache-storm-1.0.3/bin/storm jar target/score-topology-1.0-SNAPSHOT-jar-with-dependencies.jar com.verifydynamics.netflowscoring.storm.NetflowScoreTopology <# of executors> <# of tasks> <max outstanding tuples> <extra match bolts> <instance-name>`. 

The deployment parameters  are explained below:

| Parameter  | Suggested Value | Purpose
| -------| --------|--------
\# of executors | 4 | number of executors per bolt
\# of tasks | 4 | number of tasks per bolt
max outstanding tuples | 20,000 | this value is used for the setMaxSpoutPending value in the Kafka spout
extra match bolts | 0 | how many extra instances of the Match bolt to deploy
instance-name | themis | used for naming stats counters in the timing bolts

# Ingestion

Flow records are ingested by the topology from a Kafka queue (default queue name : netflow-json). The flows must be in JSON format and must match the following schema:

```javascript
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "definitions": {},
    "properties": {
    "bps": {
      "type": "integer"
    },
    "bytes": {
      "type": "integer"
    },
    "dst_ip": {
      "type": "string"
    },
    "dst_port": {
      "type": "integer"
    },
    "duration_unix_secs": {
      "type": "integer"
    },
    "end_timestamp_unix_secs": {
      "type": "integer"
    },
    "flags": {
      "type": "string"
    },
    "id": {
      "type": "integer"
    },
    "packets": {
      "type": "integer"
    },
    "pps": {
      "type": "integer"
    },
    "protocol": {
      "type": "integer"
    },
    "src_ip": {
      "type": "string"
    },
    "src_port": {
      "type": "integer"
    },
    "start_timestamp_unix_secs": {
      "type": "integer"
    },
    "tos": {
      "type": "integer"
    }
  },
  "type": "object"
}
```

In the /scripts directory are two example scripts. The first, 'import_netflow.sh' can be used to import flow data from nfdump nfcapd files into a MySQL database. The second, 'kafka_netflow_producer.py', is an example python script that loads flow records from a MySQL database and submits them to the Kafka queue.

# Output

There are a number of options for scored flow outputs:

- Scored flows can be persisted in a PostgreSQL database. This data can then be further processed or visualised as required. 
- Scored flows can be sent to a Kafka queue (scored-flows and scored-flows-detail by default). The installed ELK stack can then be used to ingest the flows and present live scoring in Kibana dashboards.
