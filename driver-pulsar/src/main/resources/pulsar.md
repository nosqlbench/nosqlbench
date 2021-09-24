- [1. NoSQLBench (NB) Pulsar Driver Overview](#1-nosqlbench-nb-pulsar-driver-overview)
  - [1.1. Issues Tracker](#11-issues-tracker)
  - [1.2. Global Level Pulsar Configuration Settings](#12-global-level-pulsar-configuration-settings)
  - [1.3. NB Pulsar Driver Yaml File - High Level Structure](#13-nb-pulsar-driver-yaml-file---high-level-structure)
    - [1.3.1. Configuration Parameter Levels](#131-configuration-parameter-levels)
  - [1.4. Pulsar Driver Yaml File - Command Blocks](#14-pulsar-driver-yaml-file---command-blocks)
    - [1.4.1. Pulsar Admin API Command Block - Create Tenants](#141-pulsar-admin-api-command-block---create-tenants)
    - [1.4.2. Pulsar Admin API Command Block - Create Namespaces](#142-pulsar-admin-api-command-block---create-namespaces)
    - [1.4.3. Pulsar Admin API Command Block - Create Topics (Partitioned or Regular)](#143-pulsar-admin-api-command-block---create-topics-partitioned-or-regular)
    - [1.4.4. Batch Producer Command Block](#144-batch-producer-command-block)
    - [1.4.5. Producer Command Block](#145-producer-command-block)
    - [1.4.6. (Single-Topic) Consumer Command Block](#146-single-topic-consumer-command-block)
    - [1.4.7. Reader Command Block](#147-reader-command-block)
    - [1.4.8. Multi-topic Consumer Command Block](#148-multi-topic-consumer-command-block)
    - [1.4.9. End-to-end Message Processing Command Block](#149-end-to-end-message-processing-command-block)
  - [1.5. Message Properties](#15-message-properties)
  - [1.6. Schema Support](#16-schema-support)
  - [1.7. Measure End-to-end Message Processing Latency](#17-measure-end-to-end-message-processing-latency)
  - [1.8. Detect Message Out-of-order Error and Message Loss](#18-detect-message-out-of-order-error-and-message-loss)
  - [1.9. NB Activity Execution Parameters](#19-nb-activity-execution-parameters)
  - [1.10. NB Pulsar Driver Execution Example](#110-nb-pulsar-driver-execution-example)
  - [1.11. Appendix A. Template Global Setting File (config.properties)](#111-appendix-a-template-global-setting-file-configproperties)
- [2. TODO : Design Revisit -- Advanced Driver Features](#2-todo--design-revisit----advanced-driver-features)
  - [2.1. Other Activity Parameters](#21-other-activity-parameters)
  - [2.2. API Caching](#22-api-caching)
    - [2.2.1. Instancing Controls](#221-instancing-controls)

# 1. NoSQLBench (NB) Pulsar Driver Overview

This driver allows you to simulate and run different types of workloads (as below) against a Pulsar cluster through NoSQLBench (NB).
* Admin API - create tenants
* Admin API - create namespaces
* Admin API - create topics
* Producer
* Consumer
* Reader
* (Future) WebSocket Producer
* (Future) Managed Ledger

## 1.1. Issues Tracker

If you have issues or new requirements for this driver, please add them at the [pulsar issues tracker](https://github.com/nosqlbench/nosqlbench/issues/new?labels=pulsar).

## 1.2. Global Level Pulsar Configuration Settings

The NB Pulsar driver relies on Pulsar's [Java Client API](https://pulsar.apache.org/docs/en/client-libraries-java/) to publish messages to and consume messages from a Pulsar cluster. In order to do so, a [PulsarClient](https://pulsar.incubator.apache.org/api/client/2.7.0-SNAPSHOT/org/apache/pulsar/client/api/PulsarClient) object needs to be created first in order to establish the connection to the Pulsar cluster; then a workload-specific object (e.g. [Producer](https://pulsar.incubator.apache.org/api/client/2.7.0-SNAPSHOT/org/apache/pulsar/client/api/Producer) or [Consumer](https://pulsar.incubator.apache.org/api/client/2.7.0-SNAPSHOT/org/apache/pulsar/client/api/Consumer)) is required in order to execute workload-specific actions (e.g. publishing or consuming messages).

When creating these objects (e.g. PulsarClient, Producer), there are different configuration options that can be used. For example, [this document](https://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer) lists all possible configuration options when creating a Pulsar Producer object.

The NB pulsar driver supports these options via a global properties file (e.g. **config.properties**). An example of this file is as below:

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

There are multiple sections in this file that correspond to different groups of configuration settings:
* **Schema related settings**:
    * All settings under this section starts with **schema.** prefix.
    * The NB Pulsar driver supports schema-based message publishing and
      consuming. This section defines configuration settings that are
      schema related.
    * There are 2 valid options under this section.
        * *schema.type*: Pulsar message schema type. When unset or set as
          an empty string, Pulsar messages will be handled in raw *byte[]*
          format. The other valid option is **avro** which the Pulsar
          message will follow a specific Avro format.
        * *schema.definition*: This only applies when an Avro schema type
          is specified. The value of this configuration is the (full) file
          path that contains the Avro schema definition.
* **Pulsar Client related settings**:
    * All settings under this section starts with **client.** prefix.
    * This section defines all configuration settings that are related
      with defining a PulsarClient object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#default-broker-urls-for-standalone-clusters)
* **Pulsar Producer related settings**:
    * All settings under this section starts with **producer** prefix.
    * This section defines all configuration settings that are related
      with defining a Pulsar Producer object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer)
* **Pulsar Consumer related settings**:
    * All settings under this section starts with **consumer** prefix.
    * This section defines all configuration settings that are related
      with defining a Pulsar Consumer object.
        * See [Pulsar Doc Reference](http://pulsar.apache.org/docs/en/client-libraries-java/#configure-consumer)
* **Pulsar Reader related settings**:
    * All settings under this section starts with **reader** prefix.
    * This section defines all configuration settings that are related
      with defining a Pulsar Reader object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#reader)

In the future, when the support for other types of Pulsar workloads is
added in NB Pulsar driver, there will be corresponding configuration
sections in this file as well.

## 1.3. NB Pulsar Driver Yaml File - High Level Structure

Just like other NB driver types, the actual Pulsar workload generation is
determined by the statement blocks in an NB driver Yaml file. Depending
on the Pulsar workload type, the corresponding statement block may have
different contents.

At high level, Pulsar driver yaml file has the following structure:

* **description**: (optional) general description of the yaml file
* **bindings**: defines NB bindings
* **params**: document level Pulsar driver parameters that apply to all
  command blocks. Currently, the following parameters are valid at this
  level:
    * **topic_url**: Pulsar topic uri ([persistent|non-persistent]:
      //<tenant>/<namespace>/<topic>). This can be statically assigned or
      dynamically generated via NB bindings.
    * **async_api**: Whether to use asynchronous Pulsar API (**note**:
      more on this later)
    * **use_transaction**: Whether to simulate Pulsar transaction
    * **admin_delop**: For Admin tasks, whether to execute delete operation
      instead of the default create operation.
    * **seq_tracking**: Whether to do message sequence tracking. This is
      used for message out-of-order and message loss detection (more on
      this later).
* **blocks**: includes a series of command blocks. Each command block
  defines one major Pulsar operation such as *producer*, *consumer*, etc.
  Right now, the following command blocks are already supported or will be
  added in the near future. We'll go through each of these command blocks
  with more details in later sections.
    * (Pulsar Admin API)  **create-tenant-block**: create/delete tenants
    * (Pulsar Admin API)  **create-namespace-block**: create/delete namespaces
    * (Pulsar Admin API)  **create-topic-block**: create/delete topics
    * (Pulsar Client API) **batch-producer-block**: batch producer
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
  admin_delop: "false"

blocks:
  - name: <command_block_1>
    tags:
      phase: <command_bock_identifier>
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

Each time when you execute the NB command, you can choose one command
block to execute by applying a filtering condition against **phase** tag,
as below.
```bash
<nb_cmd> driver=pulsar tags=phase:<command_bock_filtering_identifier> ...
```

An example of executing Pulsar producer/consumer API using NB is like
this:

```bash
# producer
<nb_cmd> driver=pulsar tags=phase:producer ...

# consumer
<nb_cmd> driver=pulsar tags=phase:consumer ...
```

Technically speaking, NB is able to execute multiple command blocks. In
the context of Pulsar driver, this means we're able to use NB to test
multiple Pulsar operations in one run! But if we want to focus the testing
on one particular operation, we can use the tag to filter the command
block as listed above!

### 1.3.1. Configuration Parameter Levels

The NB Pulsar driver configuration parameters can be set at 3 different
levels:

* **global level**: parameters that are set in ***config.properties*** file
```
schema.type=
```
* **document level**: parameters that are set within NB yaml file and under
 the ***params*** section
```
params:
  topic_uri: ...
```
* **statement level**: parameters that are set within NB yaml file, but
under different block statements
```
- name: producer-block
  statements:
    - name: s1
      msg_key:
```

**NOTE**: If one parameter is set at multiple levels (e.g. producer name),
the parameter at lower level will take precedence.

## 1.4. Pulsar Driver Yaml File - Command Blocks

### 1.4.1. Pulsar Admin API Command Block - Create Tenants

This Pulsar Admin API Block is used to create or delete Pulsar tenants. It
has the following format.

Please note that when document level parameter **admin_delop** is set to be
true, then this command block will delete Pulsar tenants instead. Similarly
this applies to other Admin API blocks for namespace and topic management.

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

In this command block, there is only 1 statement (s1):

* Statement **s1** is used for creating a Pulsar tenant
    * (Mandatory) **optype (admin-tenant)** is the statement identifier
      for this statement
    * (Optional) **allowed_clusters** must be statically bound, and it
      specifies the cluster list that is allowed for a tenant.
    * (Optional) **admin_roles** must be statically bound, and it specifies
      the superuser role that is associated with a tenant.
    * (Mandatory) **tenant** is the Pulsar tenant name to be created. It
      can either be dynamically or statically bound.

### 1.4.2. Pulsar Admin API Command Block - Create Namespaces

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

In this command block, there is only 1 statement (s1):

* Statement **s1** is used for creating a Pulsar namespace in format "<tenant>/<namespace>"
    * (Mandatory) **optype (admin-namespace)** is the statement identifier
      for this statement
    * (Mandatory) **namespace** is the Pulsar namespace name to be created
      under a tenant. It can be either statically or dynamically bound.

### 1.4.3. Pulsar Admin API Command Block - Create Topics (Partitioned or Regular)

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

In this command block, there is only 1 statement (s1):

* Statement **s1** is used for creating a Pulsar tenant and a namespace
    * (Mandatory) **optype (admin-crt-top)** is the statement identifier
      for this statement
    * (Mandatory) **enable_partition** specifies whether to create a
      partitioned topic. It can either be dynamically or statically bound.
    * (Mandatory) **partition_num** specifies the number of partitions if
      a partitioned topic is to be created. It also can be dynamically or
      statically bound.

**NOTE**: The topic name is bound by the document level parameter "topic_uri".

### 1.4.4. Batch Producer Command Block

Batch producer command block is used to produce a batch of messages all at
once by one NB cycle execution. A typical format of this command block is
as below:

```yaml
  - name: batch-producer-block
    tags:
      phase: batch-producer
    statements:
      - name: s1
        optype: batch-msg-send-start
        # For batch producer, "producer_name" should be associated with batch start
        # batch_producer_name: {batch_producer_name}
        ratio: 1
      - name: s2
        optype: batch-msg-send
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
        ratio: 100
      - name: s3
        optype: batch-msg-send-end
        ratio: 1
```

This command block has 3 statements (s1, s2, and s3) with the following
ratios: 1, <batch_num>, 1.

* Statement **s1** is used to mark the start of a batch of message
  production within one NB cycle
    * (Mandatory) **optype (batch-msg-send-start)** is the statement
      identifier for this statement
    * (Optional)  **batch_producer_name**, when provided, specifies the
      Pulsar producer name that is associated with the batch production of
      the messages.
    * (Optional)  **ratio**, when provided, MUST be 1. If not provided, it
      is default to 1.
* Statement **s2** is the core statement that generates the message key
  and payload to be put in the batch.
    * (Mandatory) **optype (batch-msg-send)** is the statement identifier
      for this statement
    * (Optional)  **msg_key**, when provided, specifies the key of the
      generated message
    * (Optional)  **msg_property**, when provided, specifies the properties
      of the generated message. It must be a JSON string that contains a
      series of key-value pairs.
    * (Mandatory) **msg_payload** specifies the payload of the generated
      message
    * (Optional)  **ratio**, when provided, specifies the batch size (how
      many messages to be put in one batch). If not provided, it is
      default to 1.
* Statement **s3** is used to mark the end of a batch within one NB cycle
    * (Mandatory) **optype (batch-msg-send-end)** is the statement
      identifier for this statement
    * (Optional)  **ratio**, when provided, MUST be 1. If not provided, it
      is default to 1.

**NOTE**: the topic that the producer needs to publish messages to is
specified by the document level parameter ***topic_uri***.

### 1.4.5. Producer Command Block

This is the regular Pulsar producer command block that produces one Pulsar
message per NB cycle execution. A typical format of this command block is
as below:

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

This command block only has 1 statements (s1):

* Statement **s1** is used to generate the key and payload for one message
    * (Mandatory) **optype (msg-send)** is the statement identifier for
      this statement
    * (Optional)  **producer_name**, when provided, specifies the Pulsar
      producer name that is associated with the message production.
    * (Optional)  **msg_key**, when provided, specifies the key of the
      generated message
    * (Optional)  **msg_property**, when provided, specifies the properties
      of the generated message. It must be a JSON string that contains a
      series of key-value pairs.
    * (Mandatory) **msg_payload** specifies the payload of the generated
      message

**NOTE**: the topic that the producer needs to publish messages to is
specified by the document level parameter ***topic_uri***.

### 1.4.6. (Single-Topic) Consumer Command Block

This is the regular Pulsar consumer command block that consumes one Pulsar
message from one single Pulsar topic per NB cycle execution. A typical
format of this command block is as below:

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

This command block only has 1 statements (s1):

* Statement **s1** is used to consume one message from the Pulsar cluster
  and acknowledge it.
    * (Mandatory) **optype (msg-consume)** is the statement identifier for
      this statement
    * (Mandatory) **subscription_name** specifies subscription name.
    * (Optional)  **subscription_type**, when provided, specifies
      subscription type. Default to **exclusive** subscription type.
    * (Optional)  **consumer_name**, when provided, specifies the
      associated consumer name.

**NOTE**: the single topic that the consumer needs to consume messages from
is specified by the document level parameter ***topic_uri***.

### 1.4.7. Reader Command Block

This is the regular Pulsar reader command block that reads one Pulsar
message per NB cycle execution. A typical format of this command block is
as below:

```yaml
  - name: reader-block
    tags:
        phase: reader
    statements:
        - name: s1
          optype: msg-read
          reader_name:
```

This command block only has 1 statements (s1):

* Statement **s1** is used to consume one message from the Pulsar cluster
  and acknowledge it.
    * (Mandatory) **optype (msg-consume)** is the statement identifier for
      this statement
    * (Optional)  **reader_name**, when provided, specifies the associated
      consumer name.

**NOTE**: the single topic that the reader needs to read messages from
is specified by the document level parameter ***topic_uri***.

**TBD**: at the moment, the NB Pulsar driver Reader API only supports
reading from the following positions:
* MessageId.earliest
* MessageId.latest (default)

A customized reader starting position, as below, is NOT supported yet!
```java
byte[] msgIdBytes = // Some message ID byte array
MessageId id = MessageId.fromByteArray(msgIdBytes);
Reader reader = pulsarClient.newReader()
        .topic(topic)
        .startMessageId(id)
        .create();
```

### 1.4.8. Multi-topic Consumer Command Block

This is the regular Pulsar consumer command block that consumes one Pulsar
message from multiple Pulsar topics per NB cycle execution. A typical format
of this command block is as below:

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

This command block only has 1 statements (s1):

* Statement **s1** is used to consume one message from the Pulsar cluster
  and acknowledge it.
    * (Mandatory) **optype (msg-consume)** is the statement identifier for
      this statement
    * (Optional)  **topic_names**, when provided, specifies multiple topic
      names from which to consume messages for multi-topic message consumption.
    * (Optional)  **topics_pattern**, when provided, specifies pulsar
      topic regex pattern for multi-topic message consumption
    * (Mandatory) **subscription_name** specifies subscription name.
    * (Optional)  **subscription_type**, when provided, specifies
      subscription type. Default to **exclusive** subscription type.
    * (Optional)  **consumer_name**, when provided, specifies the
      associated consumer name.

**NOTE 1**: when both **topic_names** and **topics_pattern** are provided,
**topic_names** takes precedence over **topics_pattern**.

**NOTE 2**: if both **topic_names** and **topics_pattern** are not provided,
consumer topic name is default to the document level parameter **topic_uri**.

### 1.4.9. End-to-end Message Processing Command Block

End-to-end message processing command block is used to simplify measuring
the end-to-end message processing (from being published to being consumed)
latency.  A typical format of this command block is as below:

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

This command block has 2 statements (s1 and s2) with the following
ratios: 1, 1.

* Statement **s1** is used to publish a message to a topic
    * (Mandatory) **optype (ec2-msg-proc-send)** is the statement
      identifier for this statement
    * (Optional)  **msg_key**, when provided, specifies the key of the
      generated message
    * (Optional)  **msg_property**, when provided, specifies the properties
      of the generated message. It must be a JSON string that contains a
      series of key-value pairs.
    * (Mandatory) **msg_payload** specifies the payload of the generated
      message
    * (Optional)  **ratio**, must be 1 when provided.
      Otherwise, default to 1.
* Statement **s2** is used to consume the message that just got published
from the same topic
    * (Mandatory) **optype (ec2-msg-proc-consume)** is the statement
      identifier for this statement
    * (Mandatory) **subscription_name** specifies subscription name.
    * (Optional)  **subscription_type**, when provided, specifies
    subscription type. Default to **exclusive** subscription type.
    * (Optional)  **ratio**, must be 1 when provided.
      Otherwise, default to 1.

**NOTE**: the topic that the producer needs to publish messages to is
specified by the document level parameter ***topic_uri***.

## 1.5. Message Properties

In the producer command block, it is optional to specify message properties:
```
    statements:
      - name: s1
        msg_property: |
          {
            "prop1": "{myprop1}",
            "prop2": "{myprop2}"
          }
```

The provided message property string must be a valid JSON string that
contains a list of key value pairs. Otherwise, if it is not a valid
JSON string as expected, the driver will ignore it and treat the
message as having no properties.

## 1.6. Schema Support

Pulsar has built-in schema support. Other than primitive types, Pulsar
also supports complex types like **Avro**, etc. At the moment, the NB
Pulsar driver provides 2 schema support modes, via the global level schema
related settings as below:
* Avro schema:
  ```properties
  schema.type= avro
  schema.definition= file:///<file/path/to/the/definition/file>
  ```
* Default byte[] schema:
  ```properties
  schema.type=
  schema.definition=
  ```

Take the previous Producer command block as an example, the **msg-value**
parameter has the value of a JSON string that follows the following Avro
schema definition:
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

## 1.7. Measure End-to-end Message Processing Latency

**e2e-msg-proc-block** measures the end-to-end message latency metrics. It
contains one message producing statement and one message consuming statement.
When the message that is published by the producer is received by the consumer,
the consumer calculates the time difference between when the time is received
and when the time is published.

The measured end-to-end message processing latency is captured as a histogram
metrics name "e2e_msg_latency".

This command block uses one single machine to act as both a producer and a
consumer. We do so just for convenience purposes. In reality, we can use
**producer-block** and **consumer-block** command blocks on separate machines
to achieve the same goal, which is probably closer to the actual use case and
probably more accurate measurement (to avoid the situation of always reading
messages from the managed ledger cache).

One thing to remember though if we're using multiple machines to measure the
end-to-end message processing latency, we need to make sure:
1) The time of the two machines are synced up with each other, e.g. through
NTP protocol.
2) If there is some time lag of starting the consumer, we need to count that
into consideration when interpreting the end-to-end message processing latency.

## 1.8. Detect Message Out-of-order Error and Message Loss

In order to detect errors like message out-of-order and message loss through
the NB Pulsar driver, we need to set the following document level parameter
to be true.
```
params:
  # Only applicable to producer and consumer
  # - used for message ordering and message loss check
  seq_tracking: "true"
```

The logic of how this works is based on the fact that NB execution cycle number
is monotonically increasing by 1 for every cycle moving forward. When publishing
a series of messages, we use the current NB cycle number as one message property
which is also monotonically increasing by 1.

When receiving the messages, if the message sequence number stored in the message
property is not monotonically increasing or if there is a gap larger than 1, then
it means the messages are either delivered out of the order or there are some message
loss. Either way, the consumer NB execution will throw runtime exceptions, with the
following messages respectively:

```text
   "Detected message ordering is not guaranteed. Older messages are received earlier!"
```

```text
   "Detected message sequence id gap. Some published messages are not received!"
```

## 1.9. NB Activity Execution Parameters

At the moment, the following NB Pulsar driver **specific** activity
parameters are supported:

* service_url=<pulsar_driver_url>
* config=<file/path/to/global/configuration/properties/file>

Some other common NB activity parameters are listed as below. Please
reference to NB documentation for more parameters

* driver=pulsar
* seq=concat (needed for **batch** producer)
* tags=phase:<command_block_identifier>
* threads=<NB_execution_thread_number>
* cycles=<total_NB_cycle_execution_number>
* --report-csv-to <metrics_output_dir_name>

## 1.10. NB Pulsar Driver Execution Example

**NOTE**: in the following examples, the Pulsar service URL is **pulsar:
//localhost:6650**, please change it accordingly for your own Pulsar
environment.

1. Run Pulsar producer API to produce 100K messages using 100 NB threads

```bash
<nb_cmd> run driver=pulsar tags=phase:producer threads=100 cycles=100K web_url=http://localhost:8080 service_url=pulsar://localhost:6650 config=<dir>/config.properties yaml=<dir>/pulsar.yaml
```

2. Run Pulsar producer batch API to produce 1M messages with 2 NB threads.
**NOTE**: *seq=* must have **concat** value in order to make the batch API working properly!
```bash
<nb_cmd> run driver=pulsar seq=concat tags=phase:batch-producer threads=2 cycles=1M web_url=http://localhost:8080 service_url=pulsar://localhost:6650 config=<dir>/config.properties yaml=<dir>/pulsar.yaml --report-csv-to <metrics_folder_path>
```

3. Run Pulsar consumer API to consume (and acknowledge) 100 messages using
   one single NB thread.
```bash
<nb_cmd> run driver=pulsar tags=phase:consumer cycles=100 web_url=http://localhost:8080 service_url=pulsar://localhost:6650 config=<dir>/config.properties yaml=<dir>/pulsar.yaml
```


## 1.11. Appendix A. Template Global Setting File (config.properties)
```properties
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

---

# 2. TODO : Design Revisit -- Advanced Driver Features

**NOTE**: The following text is based on the original multi-layer API
caching design which is not fully implemented at the moment. We need to
revisit the original design at some point in order to achieve maximum
testing flexibility.

To summarize, the original caching design has the following key
requirements:

* **Requirement 1**: Each NB Pulsar activity is able to launch and cache
  multiple **client spaces**
* **Requirement 2**: Each client space can launch and cache multiple
  Pulsar operators of the same type (producer, consumer, etc.)
* **Requirement 3**: The size of each Pulsar operator specific cached
  space can be configurable.

In the current implementation, only requirement 2 is implemented.

* For requirement 1, the current implementation only supports one client
  space per NB Pulsar activity
* For requirement 3, the cache space size is not configurable (no limit at
  the moment)

## 2.1. Other Activity Parameters

- **maxcached** - A default value to be applied to `max_clients`,
  `max_producers`, `max_consumers`.
    - default: `max_cached=100`
- **max_clients** - Clients cache size. This is the number of client
  instances which are allowed to be cached in the NoSQLBench client
  runtime. The clients cache automatically maintains a cache of unique
  client instances internally. default: _maxcached_
- **max_operators** - Producers/Consumers/Readers cache size (per client
  instance). Limits the number of instances which are allowed to be cached
  per client instance. default: _maxcached_

## 2.2. API Caching

This driver is tailored around the multi-tenancy and topic naming scheme
that is part of Apache Pulsar. Specifically, you can create an arbitrary
number of client instances, producers (per client), and consumers (per
client) depending on your testing requirements.

Further, the topic URI is composed from the provided qualifiers of
`persistence`, `tenant`, `namespace`, and `topic`, or you can provide a
fully-composed value in the `persistence://tenant/namespace/topic`
form.

### 2.2.1. Instancing Controls

Normative usage of the Apache Pulsar API follows a strictly enforced
binding of topics to producers and consumers. As well, clients may be
customized with different behavior for advanced testing scenarios. There
is a significant variety of messaging and concurrency schemes seen in
modern architectures. Thus, it is important that testing tools rise to the
occasion by letting users configure their testing runtimes to emulate
applications as they are found in practice. To this end, the NoSQLBench
driver for Apache Pulsar provides a set controls within its op template
format which allow for flexible yet intuitive instancing in the client
runtime. This is enabled directly by using nominative variables for
instance names where needed. When the instance names are not provided for
an operation, defaults are used to emulate a simple configuration.

Since this is a new capability in a NoSQLBench driver, how it works is
explained below:

When a pulsar cycles is executed, the operation is synthesized from the op
template fields as explained below under _Op Fields_. This happens in a
specific order:

1. The client instance name is resolved. If a `client` field is provided,
   this is taken as the client instance name. If not, it is set
   to `default`.
2. The named client instance is fetched from the cache, or created and
   cached if it does not yet exist.
3. The topic_uri is resolved. This is the value to be used with
   `.topic(...)` calls in the API. The op fields below explain how to
   control this value.
4. For _send_ operations, a producer is named and created if needed. By
   default, the producer is named after the topic_uri above. You can
   override this by providing a value for `producer`.
5. For _recv_ operations, a consumer is named and created if needed. By
   default, the consumer is named after the topic_uri above. You can
   override this by providing a value for `consumer`.

The most important detail for understanding the instancing controls is
that clients, producers, and consumers are all named and cached in the
specific order above.
