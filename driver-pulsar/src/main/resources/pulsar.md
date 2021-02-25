- [1. NoSQLBench (NB) Pulsar Driver Overview](#1-nosqlbench-nb-pulsar-driver-overview)
    - [1.1. Issues Tracker](#11-issues-tracker)
    - [1.2. Global Level Pulsar Configuration Settings](#12-global-level-pulsar-configuration-settings)
    - [1.3. Pulsar Driver Yaml File: Statement Blocks](#13-pulsar-driver-yaml-file-statement-blocks)
        - [1.3.1. Producer Statement block](#131-producer-statement-block)
        - [1.3.2. Consumer Statement block](#132-consumer-statement-block)
    - [1.4. Schema Support](#14-schema-support)
    - [1.5. Activity Parameters](#15-activity-parameters)
    - [1.6. Pulsar NB Execution Example](#16-pulsar-nb-execution-example)
- [2. Advanced Driver Features -- TODO: Design Revisit](#2-advanced-driver-features----todo-design-revisit)
    - [2.1. Other Activity Parameters](#21-other-activity-parameters)
    - [2.2. API Caching](#22-api-caching)
        - [2.2.1. Instancing Controls](#221-instancing-controls)

# 1. NoSQLBench (NB) Pulsar Driver Overview

This driver allows you to simulate and run different types of workloads (as below) against a Pulsar cluster through NoSQLBench (NB).
* Producer
* Consumer
* (Future) Reader
* (Future) WebSocket Producer
* (Future) Managed Ledger

**NOTE**: At the moment, only Producer workload type is fully supported in NB. The support for Consumer type is partially added but not completed yet; and the support for other types of workloads will be added in NB in future releases.

## 1.1. Issues Tracker

If you have issues or new requirements for this driver, please add them at the [pulsar issues tracker](https://github.com/nosqlbench/nosqlbench/issues/new?labels=pulsar).

## 1.2. Global Level Pulsar Configuration Settings

The NB Pulsar driver relies on Pulsar's [Java Client API](https://pulsar.apache.org/docs/en/client-libraries-java/) to publish and consume messages from the Pulsar cluster. In order to do so, a [PulsarClient](https://pulsar.incubator.apache.org/api/client/2.7.0-SNAPSHOT/org/apache/pulsar/client/api/PulsarClient) object needs to be created first in order to establish the connection to the Pulsar cluster; then a workload-specific object (e.g. [Producer](https://pulsar.incubator.apache.org/api/client/2.7.0-SNAPSHOT/org/apache/pulsar/client/api/Producer) or [Consumer](https://pulsar.incubator.apache.org/api/client/2.7.0-SNAPSHOT/org/apache/pulsar/client/api/Consumer)) is required in order to execute workload-specific actions (e.g. publishing or consuming messages).

When creating these objects (e.g. PulsarClient, Producer), there are different configuration options that can be used. For example, [this document](https://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer) lists all possible configuration options when creating a Pulsar Producer object.

The NB pulsar driver supports these options via a global properties file (e.g. **config.properties**). An example of this file is as below:

```properties
### NB Pulsar driver related configuration - driver.xxx
driver.client-type = producer

### Schema related configurations - schema.xxx
schema.type = avro
schema.definition = file:///<path/to/avro/schema/definition/file>

### Pulsar client related configurations - client.xxx
client.serviceUrl = pulsar://<pulsar_broker_ip>:6650
client.connectionTimeoutMs = 5000

### Producer related configurations (global) - producer.xxx
producer.topicName = persistent://public/default/mynbtest
producer.producerName =
producer.sendTimeoutMs =
```

There are multiple sections in this file that correspond to different groups of configuration settings:
* **NB pulsar driver related settings**:
    * All settings under this section starts with **driver.** prefix.
    * Right now there is only valid option under this section:
        * *driver.client-type* determines what type of Pulsar workload to be simulated by NB.
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
    * All settings under this section starts with **producer.** prefix.
    * This section defines all configuration settings that are related with defining a Pulsar Producer object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer)

In the future, when the support for other types of Pulsar workloads is added in NB Pulsar driver, there will be corresponding configuration sections in this file as well.

## 1.3. Pulsar Driver Yaml File: Statement Blocks

Just like other NB driver types, the actual Pulsar workload generation is determined by the statement blocks in the NB driver Yaml file. Depending on the Pulsar workload type, the corresponding statement block may have different contents.

### 1.3.1. Producer Statement block

An example of defining Pulsar **Producer** workload is as below:

```yaml
blocks:
- name: producer-block
  tags:
    type: producer
  statements:
    - producer-stuff:
          # producer-name:
          # topic_uri: "persistent://public/default/{topic}"
          topic_uri: "persistent://public/default/nbpulsar"
          msg-key: "{mykey}"
          msg-value: |
            {
                "SensorID": "{sensor_id}",
                "SensorType": "Temperature",
                "ReadingTime": "{reading_time}",
                "ReadingValue": {reading_value}
            }
```

In the above statement block, there are 4 key statement parameters to provide values:
* **producer-name**: cycle-level Pulsar producer name (can be dynamically bound)
    * **Optional**
    * If not set, global level producer name in *config.properties* file will be used.
        * Use a default producer name, "default", if it is neither set at global level.
    * If set, cycle level producer name will take precedence over the global level setting

* **topic_uri**: cycle-level Pulsar topic name (can be dynamically bound)
    * **Optional**
    * If not set, global level topic_uri in *config.properties* file will be used
        * Throw a Runtime Error if it is neither set at global level
    * If set, cycle level topic_uri will take precedence over the global level setting; and the provided value must follow several guidelines:
        * It must be in valid Pulsar topic format as below:
          ```
          [persistent|non-persistent]://<tenant-name>/<namespace-name>/<short-topic-name>
          ```
        * At the moment, only "**\<short-topic-name\>**" part can be dynamically bound (e.g. through NB binding function). All other parts must be static values and the corresponding tenants and namespaces must be created in the Pulsar cluster in advance.

**TODO**: allow dynamic binding for "\<tenant-name\>" and "\<namespace-name\>" after adding a phase for creating "\<tenant-name\>" and/or "\<namespace-name\>", similar to C* CQL schema creation phase.!

* **msg-key**: Pulsar message key
    * **Optional**
    * If not set, the generated Pulsar messages (to be published by the Producer) doesn't have **keys**.

* **msg-value**: Pulsar message payload
    * **Mandatory**
    * If not set, throw a Runtime Error.

### 1.3.2. Consumer Statement block

**TBD ...**

## 1.4. Schema Support

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

For the previous Producer block statement example, the **msg-value** parameter has the value of a JSON string that follows the following Avro schema definition (e.g. from a file **iot-example.asvc**)
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

## 1.5. Activity Parameters

At the moment, the following Activity Parameter is supported:

- * config=<file/path/to/global/configuration/properties/file>

## 1.6. Pulsar NB Execution Example

```
<NB_Cmd> run type=pulsar -vv cycles=10 config=<dir>/config.properties yaml=<dir>/pulsar.yaml
```

**NOTE**:

* An example of **config.properties** file is [here](activities/config.properties)
* An example of **pulsar.yaml** file is [here](activities/pulsar.yaml)

---

# 2. Advanced Driver Features -- TODO: Design Revisit

**NOTE**: The following text is based on the original multi-layer API caching design which is not fully implemented at the moment. We need to revisit the original design at some point in order to achieve maximum testing flexibility.

To summarize, the original caching design has the following key requirements:
* **Requirement 1**: Each NB Pulsar activity is able to launch and cache multiple **client spaces**
* **Requirement 2**:Each client space can launch and cache multiple Pulsar operators of the same type (producer, consumer, etc.)

In the current implementation, only requirement 2 is implemented. Regarding requirement 1, the current implementation only supports one client space per NB Pulsar activity!

## 2.1. Other Activity Parameters

- **url** - The pulsar url to connect to.
    - **default** - `url=pulsar://localhost:6650`
- **maxcached** - A default value to be applied to `max_clients`,
  `max_producers`, `max_consumers`.
    - default: `max_cached=100`
- **max_clients** - Clients cache size. This is the number of client
  instances which are allowed to be cached in the NoSQLBench client
  runtime. The clients cache automatically maintains a cache of unique
  client instances internally. default: _maxcached_

- **max_producers** - Producers cache size (per client instance). Limits
  the number of producer instances which are allowed to be cached per
  client instance. default: _maxcached_
- **max_consumers** - Consumers cache size (per client instance). Limits
  the number of consumer instances which are allowed to be cached per
  client instance.

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
