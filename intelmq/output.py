# -*- coding: utf-8 -*-
import io
import redis
import json
import uuid
import time
import hashlib

from intelmq.lib.bot import Bot

class FlowScoreOutputBot(Bot):

	def init(self):
		self.host = self.parameters.redis_server_ip
		self.port = int(self.parameters.redis_server_port)
		self.db = int(self.parameters.redis_db)
		self.password = self.parameters.redis_password
		self.timeout = int(self.parameters.redis_timeout)
		self.logger.exception("Redis connecting to %s:%s!", self.host, self.port)

		self.data_timeout = int(self.parameters.data_timeout)

		self.conn = redis.ConnectionPool(host=self.host, port=self.port, db=self.db)
		self.connect()

	def connect(self):
		try:
			self.output = redis.StrictRedis(connection_pool=self.conn, socket_timeout=self.timeout, password=self.password)
			info = self.output.info()
		except redis.ConnectionError:
			self.logger.exception("Redis connection to %s:%s failed!", self.host, self.port)
			self.stop()
		else:
			self.logger.info("Connected successfully to Redis %s at %s:%s!",
					info['redis_version'], self.host, self.port)
		

	def process(self):
		event = self.receive_message()
		now = int(time.time())

		if (event.get('source.ip') is not None):
			# unique key that is easy to find
			key = 'intel-mq-' + event.get('source.ip')

			# build up data
			interesting_data = {}
			interesting_data['event_description'] = event.get('event_description.text', '')
			interesting_data['feed_provider'] = event.get('feed.provider', '')
			interesting_data['feed_accuracy'] = event.get('feed.accuracy', '')
			interesting_data['classification_type'] = event.get('classification.type', '')
			interesting_data['classification_taxonomy'] = event.get('classification.taxonomy', '')

			data = json.dumps(interesting_data)
			data_hash = hashlib.md5(data.encode('utf-8')).hexdigest()

			# look for existing entry and clear out state ones
			del_keys = [];
			entries = self.output.hgetall(key)
			self.logger.info("key %s has %s entries", key, len(entries))
			for entry_key in entries:
				value_hash = hashlib.md5(entries[entry_key]).hexdigest()
				# if data exists already then delete previous entry and add one with fresher timestamp
				if (data_hash == value_hash):
					del_keys.append(entry_key.decode('utf-8'))
				# if old data then remove
				if (int(entry_key) < now - self.data_timeout):
					del_keys.append(entry_key.decode('utf-8'))

			try:
				# clear out first
				if len(del_keys) > 0:
					self.logger.info("removing stale entries for key %s (%s)", key, del_keys)
					self.output.hdel(key, *del_keys)
				# add new entry and update expiry
				entry = {now: data}
				self.output.hmset(key, entry)
				self.output.expire(key, 84600)
			except Exception:
				self.logger.exception('error setting message - reconnecting.')
				self.connect()

		else:
			self.logger.info("no ip - skipping")

		self.acknowledge_message()


BOT = FlowScoreOutputBot
