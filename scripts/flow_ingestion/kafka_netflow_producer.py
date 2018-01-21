#!/usr/bin/python

import confluent_kafka
import datetime
import MySQLdb
import time
import json
import socket, struct

bootstrap_servers = 'localhost:9092'
topic = 'netflow-json'

fields = ['id', 'start_timestamp_unix_secs', 'end_timestamp_unix_secs', 'duration_unix_secs', 'protocol', 'src_ip', 'dst_ip', 'src_port', 'dst_port', 'packets', 'bytes', 'flows', 'flags', 'tos', 'bbs', 'pps']

def db_to_json(fields):
	json_data = {}
	json_data['id'] = fields[0]
	json_data['start_timestamp_unix_secs'] = int(time.mktime(fields[1].timetuple()))
	json_data['end_timestamp_unix_secs'] = int(time.mktime(fields[2].timetuple()))
	json_data['duration_unix_secs'] = fields[3]
	json_data['protocol'] = fields[4]
	json_data['src_ip'] = fields[5]
	json_data['dst_ip'] = fields[6]
	json_data['src_ip_int'] = struct.unpack("!I", socket.inet_aton(fields[5]))[0] 
	json_data['dst_ip_int'] = struct.unpack("!I", socket.inet_aton(fields[6]))[0]
	json_data['src_port'] = fields[7]
	json_data['dst_port'] = fields[8]
	json_data['packets'] = fields[9]
	json_data['bytes'] = fields[10]
	json_data['flows'] = fields[11]
	json_data['flags'] = fields[12]
	json_data['tos'] = fields[13]
	json_data['bps'] = fields[14]
	json_data['pps'] = fields[15]

	json_str = json.dumps(json_data, default=str)

	return json_str

def confluent_kafka_netflow_producer(producer, db_cursor):
	messages_to_retry = 0
	max_id = 0

	producer_start = time.time()
	data = cursor.fetchone()
	while data is not None:
		max_id = data[0]
		json_str = db_to_json(data)
		#print (json_str)
		try:
			producer.produce(topic, value=json_str.encode())      
			#time.sleep(1)
		except BufferError as e:
			messages_to_retry += 1
		data = cursor.fetchone()

	producer.flush()
            
	return max_id, time.time() - producer_start

# Open database connection
db = MySQLdb.connect("127.0.0.1","root","root","flow_score_source" )

# kafka
conf = {'bootstrap.servers': bootstrap_servers}
producer = confluent_kafka.Producer(**conf)

# prepare a cursor object using cursor() method
cursor = db.cursor()

# execute SQL query using execute() method.

step_size = 10000
max_id = 0
loop = 5000

while loop > 0:
	print("loop : " + str(loop) + " / max id = " + str(max_id))
	loop = loop - 1
	
	cursor.execute("select id, timestamp as start_timestamp, timestamp as end_timestamp, 0 as duration, protocol, src_ip, dst_ip, src_port, dst_port, packets, bytes, 0 as flows, flags, 0 as tos, 0 as bbs, 0 as pps from flow_data where id > " + str(max_id) + " limit " + str(step_size))

	max_id, elapsed = confluent_kafka_netflow_producer(producer, cursor)
	print(elapsed)
	print(max_id)
	#time.sleep(1)

# disconnect from server
db.close()


