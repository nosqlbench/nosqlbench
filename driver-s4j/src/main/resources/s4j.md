- [1. Overview](#1-overview)
- [2. Execute NB S4J Workload](#2-execute-nb-s4j-workload)
- [3. NB S4J Driver Configuration Parameter File](#3-nb-s4j-driver-configuration-parameter-file)
- [4. NB S4J Scenario Definition File](#4-nb-s4j-scenario-definition-file)
    - [4.1. Document Level Parameters](#41-document-level-parameters)
    - [4.2. NB S4J Workload Types](#42-nb-s4j-workload-types)
        - [4.2.1. Publish Messages to a JMS Destination, Queue or Topic](#421-publish-messages-to-a-jms-destination-queue-or-topic)
    - [4.3. Receiving Messages from a JMS Destination, Queue or Topic](#43-receiving-messages-from-a-jms-destination-queue-or-topic)
    - [4.4. Receiving Messages from a JMS Topic, with a Durable, Shared, or Durable + Shared Subscription](#44-receiving-messages-from-a-jms-topic-with-a-durable-shared-or-durable--shared-subscription)


# 1. Overview

This driver is similar to [NB Pulsar driver](../../../../driver-pulsar/src/main/resources/pulsar.md) that allows NB based workload generation and performance testing against a Pulsar cluster. It also follows a similar pattern to configure and connect to the Pulsar cluster for workload execution.

However, the major difference is instead of simulating native Pulsar client workloads, the NB S4J driver allows simulating JMS oriented workloads (that follows JMS spec 2.0 and 1.1) to be executed on the Pulsar cluster. Under the hood, this is achieved through DataStax's [Starlight for JMS API] (https://github.com/datastax/pulsar-jms).

# 2. Execute NB S4J Workload

The following is an example of executing a NB S4J workload (defined as *pulsar_s4j.yaml*)

```
$ <nb_cmd> run driver=s4j tags=phase:ms cycles=10000 threads=4 num_conn=2 num_session=2 session_mode="client_ack" web_url=http://localhost:8080 service_url=pulsar://localhost:6650 config=/path/to/nb_s4j_config.properties yaml=/path/to/pulsar_s4j.yaml -vv --logs-dir=s4j_log
```

In the above NB CLI command, the S4J driver specific parameters are listed as below:
* num_conn: the number of JMS connections to be created
* num_session: the number of JMS sessions per JMS connection
* Note that multiple JMS sessions can be created from one JMS connection, and they share the same connection characteristics.
* session_mode: the session mode used when creating a JMS session
* web_url: the URL of the Pulsar web service
* service_url: the URL of the Pulsar native protocol service
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

The S4J API has a list of configuration options that can be found here: https://docs.datastax.com/en/fast-pulsar-jms/docs/1.1/pulsar-jms-reference.html#_configuration_options.

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
* dest_name: JMS destination name

```
params:
 temporary_dest: "false"
 dest_type: "<jms_destination_type>"
 dest_name: "<jms_destination_name>"
 async_api: "true"
```

Please **NOTE** that the above parameters won't necessarily be specified at the document level. If they're specified at the statement level, they will only impact the statement within which they're specified.

## 4.2. NB S4J Workload Types

The NB S4J driver supports different types of JMS operations. They're identified by **optype** parameter within a statement block. The following **optype**s are supported:

* **msg_send**: publish messages to a JMS destination, queue or topic
* **msg_read**: receive messages to a JMS destination, queue or topic
* **msg_read_durable**: receive messages to a JMS queue, with a durable subscription
* **msg_read_shared**: receive messages to a JMS queue, with a shared subscription
* **msg_read_shared_durable**: receive messages to a JMS queue, with a durable and shared subscription
* **msg_browse**: browse messages from a JMS queue

### 4.2.1. Publish Messages to a JMS Destination, Queue or Topic

The NB S4J statement block for publishing messages to a JMS destination (either a Queue or a topic) has the following format.
* Optionally, you can specify the JMS headers (**msg_header**) and properties (**msg_property**) via valid JSON strings in key: value format.
* The default message type (**msg_type**) is "byte". But optionally, you can specify other message types such as "text", "map", etc.
* The message payload (**msg_body**) is the only mandatory field.

```
 - name: "msg-send-block"
   tags:
     phase: "ms"
   statements:
     - name: "s1"
       optype: "msg_send"

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

##  4.3. Receiving Messages from a JMS Destination, Queue or Topic

The generic NB S4J statement block for receiving messages to a JMS destination (either a Queue or a topic) has the following format. All the statement specific parameters are listed as below.
* **msg_selector**: Message selector string
* **no_local**: Only applicable to a Topic as the destination. This allows a subscriber to inhibit the delivery of messages published by its own connection.
* **read_timeout**: The timeout value for receiving a message from a destination
* This setting only works if **no_wait** is false
* If the **read_timeout** value is 0, it behaves the same as **no_wait** is true
* **no_wait**: Whether to receive the next message immediately if one is available

```
 - name: "msg-read-block"
   tags:
     phase: "mr"
   statements:
     - name: "s1"
       optype: "msg_read"

       ## (Optional) Message selector
       msg_selector: ""

       ## (Optional) No Local
       no_local: "false"

       ## (Optional) Read Timeout
       read_timeout: ""

       ## (Optional) Receive message without wait
       no_wait: "true"
```

##  4.4. Receiving Messages from a JMS Topic, with a Durable, Shared, or Durable + Shared Subscription

The NB S4J statement block of subscribing from a JMS topic with a ***durable*** subscription has the following format.
* **subscription_name**: This is mandatory
* **no_wait**: Same as the generic message receiving workload

```
 - name: "msg-read-durable-block"
   tags:
     phase: "mrd"
   statements:
     - name: "s1"
       optype: "msg_read_durable"

       ## Subscription name
       subscription_name: "sub-mrd"

       ## (Optional) Receive message without wait
       no_wait: "true"
```

When the subscription type is ***shared***, or ***durable + shared***, the statement block is pretty much the same except that the **optype** is different:
* For ***shared*** subscription type, the **optype** is "msg_read_shared".
* For ***durable + shared*** subscription type, the **optype** is "msg_read_shared_durable".
