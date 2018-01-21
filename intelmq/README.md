# IntelMQ Themis Bot

The 'output.py' file contains the source for a custom IntelMQ bot that will persist any threat intelligence routed to it in a IntelMQ setup to a Redis instance. This is then used for scoring. To install this please follow the instructions here: 'https://github.com/certtools/intelmq/blob/master/docs/Developers-Guide.md#bot-developer-guide'

To configure add the following to the IntelMQ runtime configuration:

```JSON
"flowscore-output": {
        "parameters": {
            "redis_db": 14,
            "redis_password": "",
            "redis_server_ip": "127.0.0.1",
            "redis_server_port": 6379,
            "redis_timeout": 50000,
            "data_timeout": 84600
        },
        "name": "FlowScore",
        "group": "Output",
        "module": "intelmq.bots.outputs.flowscore.output",
        "description": "Custom output bot for flow_score."
    }
 ```
