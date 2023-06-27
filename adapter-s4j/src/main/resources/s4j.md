---
weight: 0
title: S4J
---
- [1. Overview](#1-overview)
- [2. Execute NB S4J Workload](#2-execute-nb-s4j-workload)
- [3. NB S4J Driver Configuration Parameter File](#3-nb-s4j-driver-configuration-parameter-file)
- [4. NB S4J Scenario Definition File](#4-nb-s4j-scenario-definition-file)
    - [4.1. Document Level Parameters](#41-document-level-parameters)
    - [4.2. NB S4J Workload Types](#42-nb-s4j-workload-types)
        - [4.2.1. Publish Messages to a JMS Destination, Queue or Topic](#421-publish-messages-to-a-jms-destination-queue-or-topic)
        - [4.2.2. Receiving Messages from a JMS Destination, Queue or Topic](#422-receiving-messages-from-a-jms-destination-queue-or-topic)
    - [4.3. S4J Named Scenario](#43-s4j-named-scenario)

---

# 1. Overview

This driver is similar to [NB Pulsar driver](../../../../adapter-pulsar/src/main/resources/pulsar.md) that allows NB based workload generation and performance testing against a Pulsar cluster. It also follows a similar pattern to configure and connect to the Pulsar cluster for workload execution.

However, the major difference is instead of simulating native Pulsar client workloads, the NB S4J driver allows simulating JMS oriented workloads (that follows JMS spec 2.0 and 1.1) to be executed on the Pulsar cluster. Under the hood, this is achieved through DataStax's [Starlight for JMS API] (https://github.com/datastax/pulsar-jms).

# 2. Execute NB S4J Workload

The following is an example of executing a NB S4J workload (defined as *pulsar_s4j.yaml*)

```shell
$ <nb_cmd> run driver=s4j cycles=10000 threads=4 num_conn=2 num_session=2 session_mode="client_ack" strict_msg_error_handling="false" web_url=http://localhost:8080 service_url=pulsar://localhost:6650 config=/path/to/nb_s4j_config.properties yaml=/path/to/pulsar_s4j.yaml -vv --logs-dir=s4j_log
```

In the above NB CLI command, the S4J driver specific parameters are listed as below:
* num_conn: the number of JMS connections to be created
* num_session: the number of JMS sessions per JMS connection
    * Note that multiple JMS sessions can be created from one JMS connection, and they share the same connection characteristics.
* session_mode: the session mode used when creating a JMS session
* web_url: the URL of the Pulsar web service
* service_url: the URL of the Pulsar native protocol service
* (optional) strict_msg_error_handling: whether to do strict error handling
    * when true, Pulsar client error will not stop NB S4J execution
    * otherwise, any Pulsar client error will stop NB S4J execution
* (optional) max_s4jop_time: maximum time (in seconds) to execute the actual S4J operations (e.g. message sending or receiving). If NB execution time is beyond this limit, each NB cycle is just a no-op. Please NOTE:
    * this is useful when controlled NB execution is needed with NB CLI scripting.
    * if this parameter is not specified or the value is 0, it means no time limitation. Every single NB cycle will trigger an actual S4J operation.
* (optional) track_msg_cnt: When set to true (with default as false), the S4J driver will keep track of the confirmed response count for message sending and receiving.

Other NB engine parameters are straight forward:
* driver: must be **s4j**
* threads: depending on the workload type, the NB thread number determines how many producers or consumers will be created. All producers or consumers will share the available JMS connections and sessions
* yamL: the NB S4J scenario definition yaml file
* config: specify the file that contains the connection parameters used by the S4J API

# 3. NB S4J Driver Configuration Parameter File

The S4J API has a list of configuration options that can be found here: https://docs.datastax.com/en/streaming/starlight-for-jms/latest/reference/pulsar-jms-reference.html.

The NB S4J driver supports these configuration options via a config property file, an example of which is listed below. The configuration parameters in this file are grouped into several groups. The comments below explain how the grouping works.

```
###########
# Overview: Starlight for JMS (S4J) API configuration items are listed at:
#           https://docs.datastax.com/en/fast-pulsar-jms/docs/1.1/pulsar-jms-reference.html#_configuration_options
enableTransaction=true

####
# S4J API specific configurations (non Pulsar specific) - jms.***

jms.enableClientSideEmulation=true
jms.usePulsarAdmin=false
#...

#####
# Pulsar client related configurations - client.***
# - Valid settings: http://pulsar.apache.org/docs/en/client-libraries-java/#client
#
# - These Pulsar client settings (without the "client." prefix) will be
#   directly used as S4J configuration settings, on a 1-to-1 basis.
#--------------------------------------
# only relevant when authentication is enabled
client.authPlugin=org.apache.pulsar.client.impl.auth.AuthenticationToken
client.authParams=file:///path/to/authentication/jwt/file
# only relevant when in-transit encryption is enabled
client.tlsTrustCertsFilePath=/path/to/certificate/file
#...

#####
# Producer related configurations (global) - producer.***
# - Valid settings: http://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer
#
# - These Pulsar producer settings (without "producer." prefix) will be collectively (as a map)
#   mapped to S4J connection setting of "producerConfig"
#--------------------------------------
producer.blockIfQueueFull=true
# disable producer batching
#producer.batchingEnabled=false
#...

#####
# Consumer related configurations (global) - consumer.***
# - Valid settings: http://pulsar.apache.org/docs/en/client-libraries-java/#configure-consumer
#
# - These Pulsar producer settings (without "consumer." portion) will be collectively (as a map)
#   mapped to S4J connection setting of "consumerConfig"
#--------------------------------------
#...
```

# 4. NB S4J Scenario Definition File

Like any NB scenario yaml file, the NB S4J yaml file is composed of 3 major components:
* bindings: define NB bindings
* params: define document level parameters
* blocks: define various statement blocks. Each statement block represents one JMS workload type

```
bindings:
 ... ...
params:
 ... ...
blocks:
 ... ...
```

## 4.1. Document Level Parameters

The parameters defined in this section will be applicable to all statement blocks. An example of some common parameters that can be set at the document level is listed below:
* temporary_dest: whether JMS workload is dealing with a temporary destination
* dest_type: JMS destination type - queue or topic

```
params:
 temporary_dest: "false"
 dest_type: "<jms_destination_type>"
 async_api: "true"
 txn_batch_num: <number_of_message_ops_in_one_transaction>
 blocking_msg_recv: <whehter_to_block_when_receiving_messages>
 shared_topic: <if_shared_topic_or_not>  // only relevant when the destination type is a topic
 durable_topic: <if_durable_topic_or_not>  // only relevant when the destination type is a topic
```

Please **NOTE** that the above parameters won't necessarily be specified at the document level. If they're specified at the statement level, they will only impact the statement within which they're specified.

## 4.2. NB S4J Workload Types

The NB S4J driver supports 2 types of JMS operations:
* One for message producing/sending/publishing
    * this is identified by NB Op identifier ***MessageProduce***
* One for message consuming/receiving/subscribing
    * this is identified by NB Op identifier ***MessageConsume***

### 4.2.1. Publish Messages to a JMS Destination, Queue or Topic

***NOTE**: Please see [pulsar_s4j_producer.yaml](scenarios/pulsar_s4j_producer.yaml) as the complete example.*

The NB S4J statement block for publishing messages to a JMS destination (either a Queue or a topic) has the following format.
* Optionally, you can specify the JMS headers (**msg_header**) and properties (**msg_property**) via valid JSON strings in key: value format.
* The default message type (**msg_type**) is "byte". But optionally, you can specify other message types such as "text", "map", etc.
* The message payload (**msg_body**) is the only mandatory field.

```yaml
blocks:
  msg-produce-block:
    ops:
      op1:
        ## The value represents the destination (queue or topic) name)
        MessageProduce: "mys4jtest_t"

        ## (Optional) JMS headers (in JSON format).
        msg_header: |
          {
            "<header_key>": "<header_value>"
          }

        ## (Optional) JMS properties, predefined or customized (in JSON format).
        msg_property: |
          {
            "<property1_key>": "<property_value1>",
            "<property2_key>": "<property_value2>"
          }

        ## (Optional) JMS message types, default to be BYTES.
        msg_type: "text"

        ## (Mandatory) JMS message body. Value depends on msg_type.
        msg_body: "{mytext_val}"
```

###  4.2.2. Receiving Messages from a JMS Destination, Queue or Topic

***NOTE**: Please see [pulsar_s4j_consumer.yaml](scenarios/pulsar_s4j_consumer.yaml) as the complete example.*

The generic NB S4J statement block for receiving messages to a JMS destination (either a Queue or a topic) has the following format. All the statement specific parameters are listed as below.
* **msg_selector**: Message selector string
* **no_local**: Only applicable to a Topic as the destination. This allows a subscriber to inhibit the delivery of messages published by its own connection.
* **read_timeout**: The timeout value for receiving a message from a destination
    * This setting only works if **no_wait** is false
    * If the **read_timeout** value is 0, it behaves the same as **no_wait** is true
* **no_wait**: Whether to receive the next message immediately if one is available
* **msg_ack_ratio**: the ratio of the received messages being acknowledged
* **slow_ack_in_sec**: whether to simulate a slow consumer (pause before acknowledging after receiving a message)
    * value 0 means no simulation (consumer acknowledges right away)
* negative ack/ack timeout/deadletter topic related settings
    * The settings here (as the scenario specific settings) will be merged with the
    *    global settings in *s4j_config.properties* file

```yaml
blocks:
  msg-produce-block:
    ops:
      op1:
        ## The value represents the destination (queue or topic) name)
        MessageProduce: "mys4jtest_t"

        ## (Optional) client side message selector
        msg_selector: ""

        ## (Optional) No Local
        no_local: "true"

        ## (Optional) Read Timeout
        read_timeout: "10"

        ## (Optional) Receive message without wait
        no_wait: "true"

        ## (Optional) Message acknowledgement ratio
        msg_ack_ratio: "0.5"

        ## (Optional) Simulate slow consumer acknowledgement
        # must be non-negative numbers. negative numbers will be treated as 0
        # 0 - means no simulation
        # positive value - the number of seconds to pause before acknowledgement
        slow_ack_in_sec: "0"

        #####
        ## (Optional) Statement level settings for Consumer
        #
        ## AckTimeout value (at least 1 second)
        consumer.ackTimeoutMillis: 1000

        ## DLQ policy
        consumer.deadLetterPolicy: '{ "maxRedeliverCount": "2" }'

        ## NegativeAck Redelivery policy
        consumer.negativeAckRedeliveryBackoff: |
          {
          }

        ## AckTimeout Redelivery policy
        consumer.ackTimeoutRedeliveryBackoff: |
          {
            "minDelayMs":"10",
            "maxDelayMs":"20",
            "multiplier":"1.2"
          }
```

## 4.3. S4J Named Scenario

For workload execution convenience, NB engine has the concept of **named scenario** ([doc](https://docs.nosqlbench.io/workloads-101/11-named-scenarios/)).

For NB S4R adapter, the following yaml file is used to define the named scenarios: [nbs4j_msg_proc_named.yaml](scenarios/nbs4j_msg_proc_named.yaml)

The CLI command to execute the S4J named scenarios (against an AS streaming tenant) is as simple as below. By default,
the scenarios will be executed against localhost.
```bash
# for message sender workload
$ <nb_cmd> nbs4j_msg_proc_named msg_send service_url=pulsar+ssl://pulsar-gcp-uscentral1.streaming.datastax.com:6651 web_url=https://pulsar-gcp-uscentral1.api.streaming.datastax.com


# for message receiver workload
$ <nb_cmd> nbs4j_msg_proc_named msg_recv service_url=pulsar+ssl://pulsar-gcp-uscentral1.streaming.datastax.com:6651 web_url=https://pulsar-gcp-uscentral1.api.streaming.datastax.com

```
