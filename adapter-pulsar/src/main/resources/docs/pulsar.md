# Pulsar

- [1. Overview](#1-overview)
    - [1.1. Issues Tracker](#11-issues-tracker)
- [2. NB Pulsar Driver Workload Definition Yaml File - High Level Structure](#2-nb-pulsar-driver-workload-definition-yaml-file---high-level-structure)
- [3. NB Pulsar Workload Configuration Parameters](#3-nb-pulsar-workload-configuration-parameters)
    - [3.1. Global Level Parameters](#31-global-level-parameters)
    - [3.2. Document Level Parameters](#32-document-level-parameters)
    - [3.3. Statement Level Parameters](#33-statement-level-parameters)
- [4. Global Level Pulsar Configuration Parameters](#4-global-level-pulsar-configuration-parameters)
- [5. NB Pulsar Driver Yaml File - Statement Blocks](#5-nb-pulsar-driver-yaml-file---statement-blocks)
    - [5.1. Pulsar Admin API Statement Block - Create/Delete Tenants](#51-pulsar-admin-api-statement-block---createdelete-tenants)
    - [5.2. Pulsar Admin API Command Block - Create/Delete Namespaces](#52-pulsar-admin-api-command-block---createdelete-namespaces)
    - [5.3. Pulsar Admin API Command Block - Create/Delete Topics, Partitioned or Not](#53-pulsar-admin-api-command-block---createdelete-topics-partitioned-or-not)
    - [5.4. Producer Statement Block](#54-producer-statement-block)
    - [5.5. Consumer Statement Block](#55-consumer-statement-block)
    - [5.6. Reader Statement Block](#56-reader-statement-block)
    - [5.7. Multi-topic Consumer Statement Block](#57-multi-topic-consumer-statement-block)
    - [5.8. End-to-end Message Latency Statement Block](#58-end-to-end-message-latency-statement-block)
- [6. Generate Message Content](#6-generate-message-content)
- [7. Message Schema Support](#7-message-schema-support)
- [8. Measure End-to-end Message Processing Latency](#8-measure-end-to-end-message-processing-latency)
- [9. Detect Message Out-of-order, Message Loss, and Message Duplication](#9-detect-message-out-of-order-message-loss-and-message-duplication)
- [10. NB Activity Execution Parameters](#10-nb-activity-execution-parameters)
- [11. NB Pulsar Driver Execution Example](#11-nb-pulsar-driver-execution-example)
- [12. Appendix A. Template Global Setting File (config.properties)](#12-appendix-a-template-global-setting-file-configproperties)

# 1. Overview

This driver allows you to simulate and run different types of workloads (as below) against a Pulsar cluster through NoSQLBench (NB).
* Admin API - create/delete tenants
* Admin API - create/delete namespaces
* Admin API - create/delete topics, partitioned or not
* Producer - publish messages with Avro schema support
* Consumer - consume messages with all subscription types
* Reader
* (Future) WebSocket Producer
* (Future) Managed Ledger

## 1.1. Issues Tracker

If you have issues or new requirements for this driver, please add them at the [pulsar issues tracker](https://github.com/nosqlbench/nosqlbench/issues/new?labels=pulsar).

# 2. NB Pulsar Driver Workload Definition Yaml File - High Level Structure

Just like other NB driver types, the actual Pulsar workload generation is determined by the statement blocks in an NB driver Yaml file. Depending on the Pulsar workload type, the corresponding statement block may have different contents.

At high level, Pulsar driver yaml file has the following structure:

* **description**: (optional) general description of the yaml file
* **bindings**: defines NB bindings
* **params**: document level Pulsar driver parameters that apply to all statement blocks.
* **statement blocks**: includes a series of statement blocks. Each statement block represents one Pulsar workload type such as *producer*, *consumer*, etc. Right now, the following workload types are supported. We'll go through each of them with more details in later sections.
    * (Pulsar Admin API)  **create-tenant-block**: create/delete tenants
    * (Pulsar Admin API)  **create-namespace-block**: create/delete namespaces
    * (Pulsar Admin API)  **create-topic-block**: create/delete topics
    * (Pulsar Client API) **producer-block**: producer
    * (Pulsar Client API) **consumer-block**: consumer (single topic)
    * (Pulsar Client API) **reader-block**: reader
    * (Pulsar Client API) **e2e-msg-proc-block**: keep track of end-to-end
      message latency (histogram)
    * (Pulsar Client API) **multi-topic-consumer-block**: consumer (multi-
      topic)

```yaml
description: |
  ... ...

bindings:
  ... ...

params:
  topic_uri: "<pulsar_topic_name>"
  async_api: "false"
  use_transaction: "false"
  admin_delop: "false"
  seq_transaction: "false"
  msg_dedup_broker: "false"

blocks:
  - name: <statement_block_1>
    tags:
      phase: <statement_block_identifier>
    statements:
      - name: <statement_name_1>
        optype: <statement_identifier>
        ... <statement_specific_parameters> ...
      - name: <statement_name_2>
        ... ...

  - name: <command_block_2>
    tags:
      ...
    statements:
      ...
```

When running a NoSQLBench Pulsar workload, we can select a particular
Pulsar workload type to run by filtering against statement blocks'
identifier as defined in the **phase** tag, as below:
```bash
<nb_cmd> driver=pulsar tags=phase:<command_bock_filtering_identifier> yaml=<pulsar_workload.yaml>...
```

An example of executing Pulsar producer/consumer API using NB is like
this:

```bash
# producer
<nb_cmd> driver=pulsar tags=phase:producer yaml=<pulsar_workload.yaml> ...

# consumer
<nb_cmd> driver=pulsar tags=phase:consumer yaml=<pulsar_workload.yaml> ...
```

# 3. NB Pulsar Workload Configuration Parameters

The NB Pulsar driver configuration parameters can be set at 3 different levels:
* Global level
* Document  level
* Statement level

**NOTE**: If one configuration parameters shows up in multiple levels (e.g. Pulsar topic name), the parameter at lower level will take precedence.

## 3.1. Global Level Parameters
**Global Level** parameters are set in an external property file (e.g. ***config.properties*** file). When running a NB Pulsar workload, we need to specify the path of this file.
```
<nb_cmd> driver=pulsar config=</path/to/config.properties> yaml=<pulsar_workload.yaml>...
```

The global level parameters are most related with fine-tuning the behaviors of a Pulsar client connection and/or an object (producer, consumer, etc.). They will impact all the Pulsar workloads types as supported in the NB Pulsar Driver. We'll cover the details of these parameters in section 4.

## 3.2. Document Level Parameters
**Document Level** parameters are set within NB yaml file and under the ***params*** section. These settings will impact multiple workload types as supported in the NB Pulsar Driver.

Currently, the following configuration parameters are available at this level:
* **topic_url**: Pulsar topic uri ([persistent|non-persistent//<tenant>/<namespace>/<topic>). This can be either statically or dynamically bound by NB data bindings.
* **async_api**: Whether to use asynchronous Pulsar client API. This can only be statically bound.
* **use_transaction**: Whether to simulate Pulsar transaction. This can only be statically bound.
* **admin_delop**: For Admin tasks, whether to execute delete operation instead of the default create operation. This can only be statically bound.
* **seq_tracking**: Whether to do message sequence tracking. This is used for abnormal message processing error detection such as message loss, message duplication, or message out-of-order. This can only be statically bound.
* **e2e_starting_time_source**: Starting timestamp for end-to-end operation. When specified, will update the `e2e_msg_latency` histogram with the calculated end-to-end latency. The latency is calculated by subtracting the starting time from the current time. The starting time is determined from a configured starting time source. The unit of the starting time is milliseconds since epoch.  The possible values for `e2e_starting_time_source`:
    * `message_publish_time` - uses the message publishing timestamp as the starting time
    * `message_event_time` - uses the message event timestamp as the starting time
    * `message_property_e2e_starting_time` - uses a message property `e2e_starting_time` as the starting time.


## 3.3. Statement Level Parameters
**Statement Level** parameters are set within the NB yaml file under different statement blocks. Each workload type/statement block has its own set of statement level configuration parameters. We'll cover these parameters in section 5.

# 4. Global Level Pulsar Configuration Parameters

The NB Pulsar driver relies on Pulsar's [Java Client API](https://pulsar.apache.org/docs/en/client-libraries-java/) to publish messages to and consume messages from a Pulsar cluster. In order to do so, a [PulsarClient](https://pulsar.incubator.apache.org/api/client/2.8.0-SNAPSHOT/org/apache/pulsar/client/api/PulsarClient) object needs to be created first in order to establish the connection to the Pulsar cluster; then a workload-specific object (e.g. [Producer](https://pulsar.incubator.apache.org/api/client/2.8.0-SNAPSHOT/org/apache/pulsar/client/api/Producer) or [Consumer](https://pulsar.incubator.apache.org/api/client/2.8.0-SNAPSHOT/org/apache/pulsar/client/api/Consumer)) is required in order to execute workload related actions (e.g. publishing or consuming messages).

When creating these objects (e.g. PulsarClient, Producer), there are different configuration parameters that can be used. For example, [this document](https://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer) lists all possible configuration parameters for a Pulsar producer object.

The NB pulsar driver supports these configuration parameters via a global properties file (e.g. **config.properties**). An example of this file is as below:

```properties
### Schema related configurations - schema.xxx
schema.type = avro
schema.definition = file:///<path/to/avro/schema/definition/file>

### Pulsar client related configurations - client.xxx
client.connectionTimeoutMs = 5000

### Producer related configurations (global) - producer.xxx
producer.topicName = persistent://public/default/mynbtest
producer.producerName =
producer.sendTimeoutMs =
```

There are multiple sections in this file that correspond to different groups of configuration parameters:
* **Schema related settings**:
    * All settings under this section starts with **schema.** prefix.
    * The NB Pulsar driver supports schema-based message publishing and consuming. This section defines configuration parameters that are schema related.
    * There are 2 valid options under this section.
        * *schema.type*: Pulsar message schema type. When unset or set as an empty string, Pulsar messages will be handled in raw *byte[]* format. The other valid option is **avro** which the Pulsar message will follow Avro schema format.
        * *schema.definition*: This only applies when an Avro schema type is specified. The value of this configuration is the (full) file path that contains the Avro schema definition.
* **Pulsar Client related settings**:
    * All settings under this section starts with **client.** prefix.
    * This section defines all configuration parameters that are related with defining a PulsarClient object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#default-broker-urls-for-standalone-clusters)
* **Pulsar Producer related settings**:
    * All settings under this section starts with **producer** prefix.
    * This section defines all configuration parameters that are related with defining a Pulsar Producer object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer)
* **Pulsar Consumer related settings**:
    * All settings under this section starts with **consumer** prefix.
    * This section defines all configuration parameters that are related with defining a Pulsar Consumer object.
        * See [Pulsar Doc Reference](http://pulsar.apache.org/docs/en/client-libraries-java/#configure-consumer)
* **Pulsar Reader related settings**:
    * All settings under this section starts with **reader** prefix.
    * This section defines all configuration parameters that are related with defining a Pulsar Reader object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#reader)

# 5. NB Pulsar Driver Yaml File - Statement Blocks

## 5.1. Pulsar Admin API Statement Block - Create/Delete Tenants

This workload type is used to create or delete Pulsar tenants. It has the following format.
```yaml
  - name: create-tenant-block
    tags:
        phase: admin-tenant
        admin_task: true
    statements:
        - name: s1
          optype: admin-tenant
          admin_roles:
          allowed_clusters:
          tenant: "{tenant}"
```

In this statement block, there is only one statement (s1):
* Statement **s1** is used for creating a Pulsar tenant
    * (Mandatory) **optype (admin-tenant)** is the statement identifier for this statement
    * (Optional) **allowed_clusters** must be statically bound, and it specifies the cluster list that is allowed for a tenant.
    * (Optional) **admin_roles** must be statically bound, and it specifies the superuser role that is associated with a tenant.
    * (Mandatory) **tenant** is the Pulsar tenant name to be created/deleted. It can either be dynamically or statically bound.

Please note that when document level parameter **admin_delop** is set to be true, then this statement block will delete Pulsar tenants instead. Similarly, this applies to other Admin API blocks for namespace and topic management.

## 5.2. Pulsar Admin API Command Block - Create/Delete Namespaces

This Pulsar Admin API Block is used to create Pulsar namespaces. It has the following format:
```yaml
  - name: create-namespace-block
    tags:
        phase: admin-namespace
        admin_task: true
    statements:
        - name: s1
          optype: admin-namespace
          namespace: "{tenant}/{namespace}"
```

In this statement block, there is only one statement (s1):

* Statement **s1** is used for creating a Pulsar namespace in format "<tenant>/<namespace>"
    * (Mandatory) **optype (admin-namespace)** is the statement identifier
      for this statement
    * (Mandatory) **namespace** is the Pulsar namespace name to be created/deleted under a tenant. It can be either statically or dynamically bound.

## 5.3. Pulsar Admin API Command Block - Create/Delete Topics, Partitioned or Not

This Pulsar Admin API Block is used to create Pulsar topics. It has the following format:
```yaml
  - name: create-topic-block
    tags:
        phase: admin-topic
        admin_task: true
    statements:
        - name: s1
          optype: admin-topic
          enable_partition: "false"
          partition_num: "5"
```

In this statement block, there is only one statement (s1):

* Statement **s1** is used for creating a Pulsar tenant and a namespace
    * (Mandatory) **optype (admin-crt-top)** is the statement identifier for this statement
    * (Mandatory) **enable_partition** specifies whether to create/delete a partitioned topic. It can either be dynamically or statically bound.
    * (Mandatory) **partition_num** specifies the number of partitions if a partitioned topic is to be created. It also can be dynamically or statically bound.

**NOTE**: The topic name is bound by the document level parameter "topic_uri".

## 5.4. Producer Statement Block

This is the regular Pulsar producer statement block that produces one Pulsar message per NB execution cycle. A typical format of this statement block is as below:

```yaml
  - name: producer-block
    tags:
        phase: producer
    statements:
        - name: s1
          optype: msg-send
          # producer_name: {producer_name}
          msg_key: "{mykey}"
          msg_property: |
          {
            "prop1": "{myprop1}",
            "prop2": "{myprop2}"
          }
          msg_value: |
              {
                  "SensorID": "{sensor_id}",
                  "SensorType": "Temperature",
                  "ReadingTime": "{reading_time}",
                  "ReadingValue": {reading_value}
              }
```

This statement block only has one statement (s1):

* Statement **s1** is used to generate one message and publish to the Pulsar cluster.
    * (Mandatory) **optype (msg-send)** is the statement identifier for this statement
    * (Optional)  **producer_name**, when provided, specifies the Pulsar producer name that is associated with the message production.
    * (Optional)  **msg_key**, when provided, specifies the key of the generated message
    * (Optional)  **msg_property**, when provided, specifies the properties of the generated message. It must be a JSON string that contains a series of key-value pairs.
    * (Mandatory) **msg_payload** specifies the payload of the generated message. If the message schema type is specified as Avro schema type, then the message value format must be in proper Avro format.

**NOTE**: the topic that the producer needs to publish messages to is specified by the document level parameter ***topic_uri***.

## 5.5. Consumer Statement Block

This is the regular Pulsar consumer statement block that consumes one message per NB execution cycle. A typical format of this statement block is as below:

```yaml
  - name: consumer-block
    tags:
        phase: consumer
    statements:
        - name: s1
          optype: msg-consume
          subscription_name:
          subscription_type:
          consumer_name:
```

This statement block only has one statement (s1):

* Statement **s1** is used to consume one message from the Pulsar cluster and acknowledge it.
    * (Mandatory) **optype (msg-consume)** is the statement identifier for this statement
    * (Mandatory) **subscription_name** specifies subscription name.
    * (Optional)  **subscription_type**, when provided, specifies subscription type. Default to **Exclusive** subscription type. Other valid types are **Failover**, **Shared**, and **Key_Shared**.
    * (Optional)  **consumer_name**, when provided, specifies the associated consumer name.

**NOTE**: the topic that the consumer receives messages from is specified by the document level parameter ***topic_uri***.

## 5.6. Reader Statement Block

This is the regular Pulsar reader statement block that reads one message per NB cycle execution. It has  a typical format as below:

```yaml
  - name: reader-block
    tags:
        phase: reader
    statements:
        - name: s1
          optype: msg-read
          reader_name:
          start_msg_position: "earliest"
```

This statement block only has one statement (s1):

* Statement **s1** is used to read one message from the Pulsar cluster.
    * (Mandatory) **optype (msg-consume)** is the statement identifier for
      this statement
    * (Optional)  **reader_name**, when provided, specifies the associated
      consumer name.
    * (Optional) **start_msg_position**, the position for the reader to read a message from. Valid values are: "earliest" or "latest"
        * **NOTE**: at the moment, the NB Pulsar driver Reader API only supports reading from the following positions: **MessageId.earliest** and **MessageId.latest** (default)

**NOTE**: the topic that the reader needs to read messages from is specified by the document level parameter ***topic_uri***.

## 5.7. Multi-topic Consumer Statement Block

This is the Pulsar consumer statement block that consumes messages from multiple Pulsar topics per NB execution. It has a typical format as below:

```yaml
  - name: multi-topic-consumer-block
    tags:
      phase: multi-topic-consumer
      admin_task: false
    statements:
      - name: s1
        optype: msg-mt-consume
        topic_names:
        topics_pattern:
        subscription_name: "mysub"
        subscription_type:
        consumer_name:
```

This statement block only has one statement (s1):

* Statement **s1** is used to consume one message from the Pulsar cluster and acknowledge it.
    * (Mandatory) **optype (msg-consume)** is the statement identifier for this statement
    * (Optional)  **topic_names**, when provided, specifies a list of topic names from which to consume messages.
    * (Optional)  **topics_pattern**, when provided, specifies Pulsar topic regex pattern for multi-topic message consumption
    * (Mandatory) **subscription_name** specifies subscription name.
    * (Optional)  **subscription_type**, when provided, specifies subscription type. Default to **Exclusive** subscription type.
    * (Optional)  **consumer_name**, when provided, specifies the associated consumer name.

**NOTE 1**: if neither **topic_names** and **topics_pattern** is provided, consumer topic name is default to the document level parameter **topic_uri**. Otherwise, the document level parameter  **topic_uri** is ignored.

**NOTE 2**: when both **topic_names** and **topics_pattern** are provided, **topic_names** takes precedence over **topics_pattern**.

## 5.8. End-to-end Message Latency Statement Block

End-to-end message latency statement block is used to simplify the task of measuring the end-to-end message processing (from being published to being consumed)latency. It has a typical format as below:

```yaml
  - name: e2e-msg-proc-block
    tags:
      phase: e2e-msg-proc
      admin_task: false
    statements:
      - name: s1
        optype: ec2-msg-proc-send
        msg_key:
        msg_property: |
          {
            "prop1": "{myprop1}"
          }
        msg_value: "{myvalue}"
        ratio: 1
      - name: s2
        optype: ec2-msg-proc-consume
        subscription_name: "mysub"
        subscription_type:
        ratio: 1
```

This statement block has two statements (s1 and s2) with the following ratios: 1, 1.

* Statement **s1** is used to publish a message to a topic
    * (Mandatory) **optype (ec2-msg-proc-send)** is the statement identifier for this statement
    * (Optional)  **msg_key**, when provided, specifies the key of the generated message
    * (Optional)  **msg_property**, when provided, specifies the properties of the generated message. It must be a JSON string that contains a series of key-value pairs.
    * (Mandatory) **msg_payload** specifies the payload of the generated message
    * (Optional)  **ratio**, must be 1 when provided. Otherwise, default to 1.
* Statement **s2** is used to consume the message that just got published
  from the same topic
    * (Mandatory) **optype (ec2-msg-proc-consume)** is the statement identifier for this statement
    * (Mandatory) **subscription_name** specifies subscription name.
    * (Optional)  **subscription_type**, when provided, specifies subscription type. Default to **Exclusive** subscription type.
    * (Optional)  **ratio**, must be 1 when provided. Otherwise, default to 1.

**NOTE**: the topic that the producer/consumer needs to publish messages to/consume messages from is specified by the document level parameter ***topic_uri***.

# 6. Generate Message Content

A Pulsar message has three main components: message key, message properties, and message payload. The former two are optional and the last one is mandatory for each message.

In the producer statement block, the contents of these components can be generated via the following statement level parameters respectively:
* msg_key: defines message key value
* msg_property: defines message property values
* msg_value: defines message payload value

For **msg_key**, when specified, its value is a text string by a NB data binding rule.

For **msg_property**, when specified, its value must be a valid JSON string that contains a list of key-value pairs. The value of each key-value pair is a text string by a NB data binding rule.
```
    statements:
      - name: s1
        msg_property: |
          {
            "prop1": "{myprop1}",
            "prop2": "{myprop2}"
          }
```

NOTE that If the **msg_property** value is not a valid JSON string, NB Pulsar driver will ignore it and treat the message as having no properties.

For **msg_value**, its value could be a plain simple text or a valid JSON string, depending on whether message schema (Avro type) will be used. The message schema is defined as global level configuration parameters (see the next section).

# 7. Message Schema Support

Pulsar has built-in schema support. Other than primitive types, Pulsar also supports complex types like **Avro**, etc. At the moment, the NB Pulsar driver supports two schema types:
* (Default) binary type
* Avro schema type

The schema type and definition are determined by  the following global level configuration parameters:
```properties
  # for Avro schema
  schema.type= avro
  schema.definition= file:///<file/path/to/the/definition/file>

  # for default binary schema
  #schema.type=
  #schema.definition=
```

If the message schema type is specified as Avro, then the schema definition needs to be provided in an external file that has the right Avro schema definition JSON string. An example is as below:
```json
{
  "type": "record",
  "name": "IotSensor",
  "namespace": "TestNS",
  "fields" : [
    {"name": "SensorID", "type": "string"},
    {"name": "SensorType", "type": "string"},
    {"name": "ReadingTime", "type": "string"},
    {"name": "ReadingValue", "type": "float"}
  ]
}
```

# 8. Measure End-to-end Message Processing Latency

The built-in **e2e-msg-proc-block** measures the end-to-end message latency metrics. It contains one message producing statement and one message consuming statement. When the message that is published by the producer is received by the consumer, the consumer calculates the time difference between when the time is received and when the time is published.

The measured end-to-end message processing latency is captured as a histogram metrics  "**e2e_msg_latency**".

This built-in command block uses one single machine to act as both a producer and a consumer. We do so just for convenience purposes. In reality, we can use**producer-block** and **consumer-block** command blocks on separate machines to achieve the same goal, which is probably closer to the actual use case and probably more accurate measurement (to avoid the situation of always reading messages from the managed ledger cache).

One thing to remember though if we're using multiple machines to measure the end-to-end message processing latency, we need to make sure:
1) The time of the two machines are synced up with each other, e.g. through the NTP protocol.
2) If there is some time lag of starting the consumer, we need to count that into consideration when interpreting the end-to-end message processing latency.

# 9. Detect Message Out-of-order, Message Loss, and Message Duplication

In order to detect abnormal message processing errors like message loss, message duplication, or message out-of-order, we need to set the following document level parameter to be true.
```
params:
  # Only applicable to producer and consumer
  # - used for message ordering and message loss check
  seq_tracking: "true"
```

When this parameter is set true, the NB Pulsar consumer workload, when executed, will check the received messages that were published by an NB Pulsar producer workload and count the corresponding errors into the following metrics:
* **msgErrOutOfSeqCounter**
* **msgErrLossCounter**
* **msgErrDuplicateCounter**

# 10. NB Activity Execution Parameters

At the moment, the following Pulsar driver specific** NB activity parameters are supported:
* service_url=<pulsar_driver_url>
* config=<file/path/to/global/configuration/properties/file>

Some other common NB activity parameters are listed as below. Please refer to NB documentation for more information.
* driver=pulsar
* tags=phase:<command_block_identifier>
* threads=<NB_execution_thread_number>
* cycles=<total_NB_cycle_execution_number>
* --report-csv-to <metrics_output_dir_name>

# 11. NB Pulsar Driver Execution Example

1. Run Pulsar producer API to produce 100K messages using 100 NB threads

```bash
<nb_cmd> run driver=pulsar tags=phase:producer threads=100 cycles=100K web_url=http://localhost:8080 service_url=pulsar://localhost:6650 config=<dir>/config.properties yaml=<dir>/pulsar.yaml
```

2. Run Pulsar consumer API to consume (and acknowledge) 100 messages using
   one single NB thread.
```bash
<nb_cmd> run driver=pulsar tags=phase:consumer cycles=100 web_url=http://localhost:8080 service_url=pulsar://localhost:6650 config=<dir>/config.properties yaml=<dir>/pulsar.yaml
```

# 12. Appendix A. Template Global Setting File (config.properties)

```
schema.type =
schema.definition =


### Pulsar client related configurations - client.xxx
client.connectionTimeoutMs =


### Producer related configurations (global) - producer.xxx
producer.producerName =
producer.topicName =
producer.sendTimeoutMs =


### Consumer related configurations (global) - consumer.xxx
consumer.topicNames =
consumer.topicsPattern =
consumer.subscriptionName =
consumer.subscriptionType =
consumer.consumerName =
consumer.receiverQueueSize =


### Reader related configurations (global) - reader.xxx
reader.topicName =
reader.receiverQueueSize =
reader.readerName =
reader.startMessagePos =
```
