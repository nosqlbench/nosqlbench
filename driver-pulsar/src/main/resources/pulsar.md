- [1. NoSQLBench (NB) Pulsar Driver Overview](#1-nosqlbench-nb-pulsar-driver-overview)
    - [1.1. Issues Tracker](#11-issues-tracker)
    - [1.2. Global Level Pulsar Configuration Settings](#12-global-level-pulsar-configuration-settings)
    - [1.3. NB Pulsar Driver Yaml File - High Level Structure](#13-nb-pulsar-driver-yaml-file---high-level-structure)
        - [1.3.1. NB Cycle Level Parameters vs. Global Level Parameters](#131-nb-cycle-level-parameters-vs-global-level-parameters)
    - [1.4. Pulsar Driver Yaml File - Command Block Details](#14-pulsar-driver-yaml-file---command-block-details)
        - [1.4.1. Pulsar Admin API Command Block](#141-pulsar-admin-api-command-block)
        - [1.4.2. Batch Producer Command Block](#142-batch-producer-command-block)
        - [1.4.3. Producer Command Block](#143-producer-command-block)
        - [1.4.4. Consumer Command Block](#144-consumer-command-block)
        - [1.4.5. Reader Command Block](#145-reader-command-block)
    - [1.5. Schema Support](#15-schema-support)
    - [1.6. NB Activity Execution Parameters](#16-nb-activity-execution-parameters)
    - [1.7. NB Pulsar Driver Execution Example](#17-nb-pulsar-driver-execution-example)
    - [1.8. Appendix A. Template Global Setting File (config.properties)](#18-appendix-a-template-global-setting-file-configproperties)
- [2. TODO : Design Revisit -- Advanced Driver Features](#2-todo--design-revisit----advanced-driver-features)
    - [2.1. Other Activity Parameters](#21-other-activity-parameters)
    - [2.2. API Caching](#22-api-caching)
        - [2.2.1. Instancing Controls](#221-instancing-controls)

# 1. NoSQLBench (NB) Pulsar Driver Overview

This driver allows you to simulate and run different types of workloads (as below) against a Pulsar cluster through NoSQLBench (NB).
* Producer
* Consumer
* Reader
* (Future) WebSocket Producer
* (Future) Managed Ledger

## 1.1. Issues Tracker

If you have issues or new requirements for this driver, please add them at the [pulsar issues tracker](https://github.com/nosqlbench/nosqlbench/issues/new?labels=pulsar).

## 1.2. Global Level Pulsar Configuration Settings

The NB Pulsar driver relies on Pulsar's [Java Client API](https://pulsar.apache.org/docs/en/client-libraries-java/) to publish and consume messages from the Pulsar cluster. In order to do so, a [PulsarClient](https://pulsar.incubator.apache.org/api/client/2.7.0-SNAPSHOT/org/apache/pulsar/client/api/PulsarClient) object needs to be created first in order to establish the connection to the Pulsar cluster; then a workload-specific object (e.g. [Producer](https://pulsar.incubator.apache.org/api/client/2.7.0-SNAPSHOT/org/apache/pulsar/client/api/Producer) or [Consumer](https://pulsar.incubator.apache.org/api/client/2.7.0-SNAPSHOT/org/apache/pulsar/client/api/Consumer)) is required in order to execute workload-specific actions (e.g. publishing or consuming messages).

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
    * The NB Pulsar driver supports schema-based message publishing and consuming. This section defines configuration settings that are schema related.
    * There are 2 valid options under this section.
        * *shcema.type*: Pulsar message schema type. When unset or set as an empty string, Pulsar messages will be handled in raw *byte[]* format. The other valid option is **avro** which the Pulsar message will follow a specific Avro format.
        * *schema.definition*: This only applies when an Avro schema type is specified and the value is the (full) file path that contains the Avro schema definition.
* **Pulsar Client related settings**:
    * All settings under this section starts with **client.** prefix.
    * This section defines all configuration settings that are related with defining a PulsarClient object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#default-broker-urls-for-standalone-clusters)
* **Pulsar Producer related settings**:
    * All settings under this section starts with **producer** prefix.
    * This section defines all configuration settings that are related with defining a Pulsar Producer object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer)
* **Pulsar Consumer related settings**:
    * All settings under this section starts with **consumer** prefix.
    * This section defines all configuration settings that are related with defining a Pulsar Consumer object.
        * See [Pulsar Doc Reference](http://pulsar.apache.org/docs/en/client-libraries-java/#configure-consumer)
* **Pulsar Reader related settings**:
    * All settings under this section starts with **reader** prefix.
    * This section defines all configuration settings that are related with defining a Pulsar Reader object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#reader)

In the future, when the support for other types of Pulsar workloads is added in NB Pulsar driver, there will be corresponding configuration sections in this file as well.

## 1.3. NB Pulsar Driver Yaml File - High Level Structure

Just like other NB driver types, the actual Pulsar workload generation is determined by the statement blocks in the NB driver Yaml file. Depending on the Pulsar workload type, the corresponding statement block may have different contents.

At high level, Pulsar driver yaml file has the following structure:
* **description**: (optional) general description of the yaml file
* **bindings**: defines NB bindings
* **params**: document level Pulsar driver parameters that apply to all command blocks. Currently there are two valid parameters:
    * **topic_url**: Pulsar topic uri ([persistent|non-persistent]://<tenant>/<namespace>/<topic>). This can be statically assigned or dynamically generated via NB bindings.
    * **async_api**: Whether to use asynchronous Pulsar API (**note**: more on this later)
* **blocks**: includes a series of command blocks. Each command block defines one major Pulsar operation such as *producer*, *consumer*, etc. Right now, the following command blocks are already supported or will be added in the near future. We'll go through each of these command blocks with more details in later sections.
    * (future) **admin-block**: support for Pulsar Admin API, starting with using NB to create tenants and namespaces.
    * **batch-producer-block**: Pulsar batch producer
    * **producer-block**: Pulsar producer
    * **consumer-block**: Pulsar consumer
    * **reader-block**: Pulsar reader

```yaml
description: |
  ... ...

bindings:
  ... ...

# global parameters that apply to all Pulsar client types:
params:
  topic_uri: "<pulsar_topic_name>"
  async_api: "false"

blocks:
  - name: <command_block_1>
    tags:
      phase: <command_bock_filtering_identifier>
    statements:
      - name: <statement_name_1>
        optype: <statement_filtering_identifier>
        ... <statement_specific_parameters> ...
      - name: <statement_name_2>
        ... ...

  - name: <command_block_2>
    tags:
      ...
    statements:
      ...
```

Each time when you execute the NB command, you can only choose one command block to execute. This is achieved by applying a filtering condition against **phase** tag, as below:
```bash
<nb_cmd> driver=pulsar tags=phase:<command_bock_filtering_identifier> ...
```

An example of executing Pulsar producer/consumer API using NB is like this:
```bash
# producer
<nb_cmd> driver=pulsar tags=phase:producer ...

# consumer
<nb_cmd> driver=pulsar tags=phase:consumer ...
```

### 1.3.1. NB Cycle Level Parameters vs. Global Level Parameters

Some parameters, especially topic name and producer/consumer/reader/etc. name, can be set at the global level in **config.properties** file, or at NB cycle level via **pulsar.yaml** file. An example of setting a topic name in both levels is as below:

```bash
# Global level setting (config.properties):
producer.topicName = ...

# Cycle level setting (pulsar.yaml)
params:
  topic_uri: ...
```

In theory, all Pulsar client settings can be made as cycle level settings for maximum flexibility. But practically speaking (and also for simplicity purposes), only the following parameters are made to be configurable at both levels, listed by cycle level setting names with their corresponding global level setting names:
* topic_uri (Mandatory)
    * producer.topicName
    * consumer.topicNames
    * reader.topicName
* topic_names (Optional for Consumer)
    * consumer.topicNames
* subscription_name (Mandatory for Consumer)
    * consumer.subscriptionName
* subscription_type (Mandatory for Consumer, default to **exclusive** type)
    * consumer.subscriptionType
* topics_pattern (Optional for Consumer)
    * consumer.topicsPattern
* producer_name (Optional)
    * producer.producerName
* consumer_name (Optional)
    * consumer.consumerName
* reader_name (Optional)
    * reader.readerName

One key difference between setting a parameter at the global level vs. at the cycle level is that the global level setting is always static and stays the same for all NB cycle execution. The cycle level setting, on the other side, can be dynamically bound and can be different from cycle to cycle.

Because of this, setting these parameters at the NB cycle level allows us to run Pulsar testing against multiple topics and/or multiple producers/consumers/readers/etc all at once within one NB activity. This makes the testing more flexible and effective.

**NOTE**, when a configuration is set at both the global level and the cycle level, **the ycle level setting will take priority!**

## 1.4. Pulsar Driver Yaml File - Command Block Details

### 1.4.1. Pulsar Admin API Command Block

**NOTE**: this functionality is only partially implemented at the moment and doesn't function yet.

Currently, the Pulsar Admin API Block is (planned) to only support creating Pulsar tenants and namespaces. It has the following format:

```yaml
  - name: admin-block
    tags:
        phase: create-tenant-namespace
    statements:
        - name: s1
          optype: create-tenant
          tenant: "{tenant}"
        - name: s2
          optype: create-namespace
          namespace: "{namespace}"
```

In this command block, there are 2 statements (s1 and s2):
* Statement **s1** is used for creating a Pulsar tenant
    * (Mandatory) **optype (create-tenant)** is the statement identifier for this statement
    * (Mandatory) **tenant** is the only statement parameter that specifies the Pulsar tenant name which can either be dynamically bound or statically assigned.
* Statement **s2** is used for creating a Pulsar namespace
    * (Mandatory) **optype (create-namespace)** is the statement identifier for this statement
    * (Mandatory) **namespace** is the only statement parameter that specifies the Pulsar namespace under the tenant created by statement s1. Its name can either be dynamically bound or statically assigned.

### 1.4.2. Batch Producer Command Block

Batch producer command block is used to produce a batch of messages all at once by one NB cycle execution. A typical format of this command block is as below:

```yaml
- name: batch-producer-block
    tags:
      phase: batch-producer
    statements:
      - name: s1
        optype: batch-msg-send-start
        # For batch producer, "producer_name" should be associated with batch start
        batch_producer_name: {batch_producer_name}
        ratio: 1
      - name: s2
        optype: batch-msg-send
        msg_key: "{mykey}"
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

This command block has 3 statements (s1, s2, and s3) with the following ratios: 1, <batch_num>, 1.
* Statement **s1** is used to mark the start of a batch of message production within one NB cycle
    * (Mandatory) **optype (batch-msg-send-start)** is the statement identifier for this statement
    * (Optional)  **batch_producer_name**, when provided, specifies the Pulsar producer name that is associated with the batch production of the messages.
    * (Optional)  **ratio**, when provided, MUST be 1. If not provided, it is default to 1.
* Statement **s2** is the core statement that generates the message key and payload to be put in the batch.
    * (Mandatory) **optype (batch-msg-send)** is the statement identifier for this statement
    * (Optional)  **msg-key**, when provided, specifies the key of the generated message
    * (Mandatory) **msg-payload** specifies the payload of the generated message
    * (Optional)  **ratio**, when provided, specifies the batch size (how many messages to be put in one batch). If not provided, it is default to 1.
* Statement **s3** is used to mark the end of a batch within one NB cycle
    * (Mandatory) **optype (batch-msg-send-end)** is the statement identifier for this statement
    * (Optional)  **ratio**, when provided, MUST be 1. If not provided, it is default to 1.

### 1.4.3. Producer Command Block

This is the regular Pulsar producer command block that produces one Pulsar message per NB cycle execution. A typical format of this command block is as below:

```yaml
  - name: producer-block
    tags:
        phase: producer
    statements:
        - name: s1
          optype: msg-send
          # producer_name: {producer_name}
          msg_key: "{mykey}"
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
    * (Mandatory) **optype (msg-send)** is the statement identifier for this statement
    * (Optional)  **producer_name**, when provided, specifies the Pulsar producer name that is associated with the message production.
    * (Optional)  **msg-key**, when provided, specifies the key of the generated message
    * (Mandatory) **msg-payload** specifies the payload of the generated message

### 1.4.4. Consumer Command Block

This is the regular Pulsar consumer command block that consumes one Pulsar message per NB cycle execution. A typical format of this command block is as below:

```yaml
  - name: consumer-block
    tags:
        phase: consumer
    statements:
        - name: s1
          optype: msg-consume
          topic_names: "<pulsar_topic_1>, <pulsar_topic_2>"
          # topics_pattern: "<pulsar_topic_regex_pattern>"
          subscription_name:
          subscription_type:
          consumer_name:
```

This command block only has 1 statements (s1):
* Statement **s1** is used to consume one message from the Pulsar cluster and acknowledge it.
    * (Mandatory) **optype (msg-consume)** is the statement identifier for this statement
    * (Optional)  **topic_names**, when provided, specifies multiple topic names from which to consume messages. Default to document level parameter **topic_uri**.
    * (Optional)  **topics_pattern**, when provided, specifies pulsar topic regex pattern for multi-topic message consumption
    * (Mandatory) **subscription_name** specifies subscription name.
    * (Optional)  **subscription_type**, when provided, specifies subscription type. Default to **exclusive** subscription type.
    * (Optional)  **consumer_name**, when provided, specifies the associated consumer name.

### 1.4.5. Reader Command Block

This is the regular Pulsar reader command block that reads one Pulsar message per NB cycle execution. A typical format of this command block is as below:

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
* Statement **s1** is used to consume one message from the Pulsar cluster and acknowledge it.
    * (Mandatory) **optype (msg-consume)** is the statement identifier for this statement
    * (Optional)  **reader_name**, when provided, specifies the associated consumer name.

**TBD**: at the moment, the NB Pulsar driver Reader API only supports reading from the following positions:
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

## 1.5. Schema Support

Pulsar has built-in schema support. Other than primitive types, Pulsar also supports complex types like **Avro**, etc. At the moment, the NB Pulsar driver provides 2 schema support modes, via the global level schema related settings as below:
* Avro schema:
  ```properties
  shcema.type: avro
  schema.definition: file:///<file/path/to/the/definition/file>
  ```
* Default byte[] schema:
  ```properties
  shcema.type:
  schema.definition:
  ```

Take the previous Producer command block as an example, the **msg-value** parameter has the value of a JSON string that follows the following Avro schema definition (e.g. as in the sample schema definition file: **[iot-example.asvc](activities/iot-example.avsc)**)
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

## 1.6. NB Activity Execution Parameters

At the moment, the following NB Pulsar driver **specific** activity parameters are supported:

* service_url=<pulsar_driver_url>
* config=<file/path/to/global/configuration/properties/file>

Some other common NB activity parameters are listed as below. Please reference to NB documentation for more parameters

* driver=pulsar
* seq=concat (needed for **batch** producer)
* tags=phase:<command_block_identifier>
* threads=<NB_execution_thread_number>
* cycles=<total_NB_cycle_execution_number>
* --report-csv-to <metrics_output_dir_name>

## 1.7. NB Pulsar Driver Execution Example

1. Run Pulsar producer API to produce 100K messages using 100 NB threads
```bash
<nb_cmd> run driver=pulsar tags=phase:producer threads=100 cycles=100K config=<dir>/config.properties yaml=<dir>/pulsar.yaml
```

2. Run Pulsar producer batch API to produce 1M messages with 2 NB threads; put NB execution metrics in a specified metrics folder

```bash
<nb_cmd> run driver=pulsar seq=concat tags=phase:batch-producer threads=2 cycles=1M config=<dir>/config.properties yaml=<dir>/pulsar.yaml --report-csv-to <metrics_folder_path>
```

3. Run Pulsar consumer API to consume (and acknowledge) 100 messages using one single NB thread.
```bash
<nb_cmd> run driver=pulsar tags=phase:consumer cycles=100 config=<dir>/config.properties yaml=<dir>/pulsar.yaml
```


## 1.8. Appendix A. Template Global Setting File (config.properties)
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

**NOTE**: The following text is based on the original multi-layer API caching design which is not fully implemented at the moment. We need to revisit the original design at some point in order to achieve maximum testing flexibility.

To summarize, the original caching design has the following key requirements:
* **Requirement 1**: Each NB Pulsar activity is able to launch and cache multiple **client spaces**
* **Requirement 2**: Each client space can launch and cache multiple Pulsar operators of the same type (producer, consumer, etc.)
* **Requirement 3**: The size of each Pulsar operator specific cached space can be configurable.

In the current implementation, only requirement 2 is implemented.
* For requirement 1, the current implementation only supports one client space per NB Pulsar activity
* For requirement 3, the cache space size is not configurable (no limit at the moment)

## 2.1. Other Activity Parameters

- **maxcached** - A default value to be applied to `max_clients`,
  `max_producers`, `max_consumers`.
    - default: `max_cached=100`
- **max_clients** - Clients cache size. This is the number of client
  instances which are allowed to be cached in the NoSQLBench client
  runtime. The clients cache automatically maintains a cache of unique
  client instances internally. default: _maxcached_
- **max_operators** - Producers/Consumers/Readers cache size (per client instance). Limits
  the number of instances which are allowed to be cached per client instance. default: _maxcached_

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
binding of topics to produces and consumers. As well, clients may be
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
