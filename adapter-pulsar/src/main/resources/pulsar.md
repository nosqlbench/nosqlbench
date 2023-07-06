---
weight: 0
title: Pulsar
---

- [1. Overview](#1-overview)
    - [1.1. Issues Tracker](#11-issues-tracker)
- [2. Execute the NB Pulsar Driver Workload](#2-execute-the-nb-pulsar-driver-workload)
    - [2.1. NB Pulsar Driver Yaml File High Level Structure](#21-nb-pulsar-driver-yaml-file-high-level-structure)
    - [2.2. NB Pulsar Driver Configuration Parameters](#22-nb-pulsar-driver-configuration-parameters)
        - [2.2.1. Global Level Parameters](#221-global-level-parameters)
        - [2.2.2. Document Level Parameters](#222-document-level-parameters)
- [3. NB Pulsar Driver OpTemplates](#3-nb-pulsar-driver-optemplates)
- [4. Message Generation and Schema Support](#4-message-generation-and-schema-support)
    - [4.1. Message Generation](#41-message-generation)
    - [4.2. Schema Support](#42-schema-support)

---

# 1. Overview

The NB Pulsar driver allows you to simulate and run different types of workloads (as below) against a Pulsar cluster through NoSQLBench (NB).
* Admin API - create/delete tenants
* Admin API - create/delete namespaces
* Admin API - create/delete topics
    * Topics can be partitioned or non-partitioned
* Producer - publish messages with schema support
    * Default schema type is byte[]
    * Avro schema and KeyValue schema are also supported
* Consumer - consume messages with schema support and the following support
    * Different subscription types
    * Multi-topic subscription (including Topic patterns)
    * Subscription initial position
    * Dead letter topic policy
    * Negative acknowledgement and acknowledgement timeout redelivery backoff policy


## 1.1. Issues Tracker

If you have issues or new requirements for this driver, please add them at the [pulsar issues tracker](https://github.com/nosqlbench/nosqlbench/issues/new?labels=pulsar).

# 2. Execute the NB Pulsar Driver Workload

In order to run a NB Pulsar driver workload, it follows similar command as other NB driver types. But it does have its unique execution parameters. The general command has the following format:

```shell
<nb_cmd> run driver=pulsar threads=<thread_num> cycles=<cycle_count> web_url=<pulsar_web_svc_url> service_url=<pulsar_svc_url> config=<pulsar_client_config_property_file> yaml=<nb_scenario_yaml_file> [<other_common_NB_execution_parameters>]
```

In the above command, make sure the driver type is **pulsar** and provide the following Pulsar driver specific parameters:
* ***web_url***: Pulsar web service url and default to "http://localhost:8080"
* ***service_url***: Pulsar native protocol service url and default to "pulsar://localhost:6650"
* ***config***: Pulsar schema/client/producer/consumer related configuration (as a property file)

## 2.1. NB Pulsar Driver Yaml File High Level Structure

Just like other NB driver types, the actual NB Pulsar workload is defined in a YAML file with the following high level structure:

```yaml
description: |
  ...

bindings:
  ...

params:
  ...

blocks:
  <block_1>:
    ops:
      op1:
        <OpTypeIdentifier>: "<static_or_dynamic_value>"
        <op_param_1>: "<some_value>"
        <op_param_2>: "<some_value>"
        ...

  <block_2>:
  ...
```

* ***description***: This is an (optional) section where to provide general description of the Pulsar NB workload defined in this file.
* ***bindings***: This section defines all NB bindings that are required in all OpTemplate blocks
* ***params***: This section defines **Document level** configuration parameters that apply to all OpTemplate blocks.
* ***blocks***: This section defines the OpTemplate blocks that are needed to execute Pulsar specific workloads. Each OpTemplate block may contain multiple OpTemplates.

## 2.2. NB Pulsar Driver Configuration Parameters

The NB Pulsar driver configuration parameters can be set at 3 different levels:
* Global level
* Document  level
    * The parameters at this level are those within a NB yaml file that impact all OpTemplates
* Op level (or Cycle level)
    * The parameters at this level are those within a NB yaml file that are associated with each individual OpTemplate

Please **NOTE** that when a parameter is specified at multiple levels, the one at the lowest level takes precedence.

### 2.2.1. Global Level Parameters

The parameters at this level are those listed in the command line config properties file.

The NB Pulsar driver relies on Pulsar's [Java Client API](https://pulsar.apache.org/docs/en/client-libraries-java/) complete its workloads such as creating/deleting tenants/namespaces/topics, generating messages, creating producers to send messages, and creating consumers to receive messages. The Pulsar client API has different configuration parameters to control the execution behavior. For example, [this document](https://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer) lists all possible configuration parameters for how a Pulsar producer can be created.

All these Pulsar "native" parameters are supported by the NB Pulsar driver, via the global configuration properties file (e.g. **config.properties**). An example of the structure of this file looks like below:

```properties
### Schema related configurations - MUST start with prefix "schema."
#schema.key.type=avro
#schema.key.definition=</path/to/avro-key-example.avsc>
schema.type=avro
schema.definition=</path/to/avro-value-example.avsc>

### Pulsar client related configurations - MUST start with prefix "client."
# http://pulsar.apache.org/docs/en/client-libraries-java/#client
client.connectionTimeoutMs=5000
client.authPluginClassName=org.apache.pulsar.client.impl.auth.AuthenticationToken
client.authParams=
# ...

### Producer related configurations (global) - MUST start with prefix "producer."
# http://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer
producer.sendTimeoutMs=
producer.blockIfQueueFull=true
# ...

### Consumer related configurations (global) - MUST start with prefix "consumer."
# http://pulsar.apache.org/docs/en/client-libraries-java/#configure-consumer
consumer.subscriptionInitialPosition=Earliest
consumer.deadLetterPolicy={"maxRedeliverCount":"5","retryLetterTopic":"public/default/retry","deadLetterTopic":"public/default/dlq","initialSubscriptionName":"dlq-sub"}
consumer.ackTimeoutRedeliveryBackoff={"minDelayMs":"10","maxDelayMs":"20","multiplier":"1.2"}
# ...
```

There are multiple sections in this file that correspond to different
categories of the configuration parameters:
* **`Pulsar Schema` related settings**:
    * All settings under this section starts with **schema.** prefix.
    * At the moment, there are 3 schema types supported
        * Default raw ***byte[]***
        * Avro schema for the message payload
        * KeyValue based Avro schema for both message key and message payload
* **`Pulsar Client` related settings**:
    * All settings under this section starts with **client.** prefix.
    * This section defines all configuration parameters that are related with defining a PulsarClient object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#default-broker-urls-for-standalone-clusters)
* **`Pulsar Producer` related settings**:
    * All settings under this section starts with **producer** prefix.
    * This section defines all configuration parameters that are related with defining a Pulsar Producer object.
        * See [Pulsar Doc Reference](https://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer)
* **`Pulsar Consumer` related settings**:
    * All settings under this section starts with **consumer** prefix.
    * This section defines all configuration parameters that are related with defining a Pulsar Consumer object.
        * See [Pulsar Doc Reference](http://pulsar.apache.org/docs/en/client-libraries-java/#configure-consumer)

### 2.2.2. Document Level Parameters

For the Pulsar NB driver, Document level parameters can only be statically bound; and currently, the following Document level configuration parameters are supported:

* ***async_api*** (boolean):
    * When true, use async Pulsar client API.
* ***use_transaction*** (boolean):
    * When true, use Pulsar transaction.
* ***admin_delop*** (boolean):
    * When true, delete Tenants/Namespaces/Topics. Otherwise, create them.
    * Only applicable to administration related operations
* ***seq_tracking*** (boolean):
    * When true, a sequence number is created as part of each message's properties
    * This parameter is used in conjunction with the next one in order to simulate abnormal message processing errors and then be able to detect such errors successfully.
* ***seqerr_simu***:
    * A list of error simulation types separated by comma (,)
    * Valid error simulation types
        * `out_of_order`: simulate message out of sequence
        * `msg_loss`: simulate message loss
        * `msg_dup`: simulate message duplication
    * This value should be used only for testing purposes. It is not recommended to use this parameter in actual testing environments.
* ***e2e_starting_time_source***:
    * Starting timestamp for end-to-end operation. When specified, will update the `e2e_msg_latency` histogram with the calculated end-to-end latency. The latency is calculated by subtracting the starting time from the current time. The starting time is determined from a configured starting time source. The unit of the starting time is milliseconds since epoch.
    * The possible values for `e2e_starting_time_source`:
        * `message_publish_time` : uses the message publishing timestamp as the starting time
        * `message_event_time` : uses the message event timestamp as the starting time
        * `message_property_e2e_starting_time` : uses a message property `e2e_starting_time` as the starting time.

# 3. NB Pulsar Driver OpTemplates

For the NB Pulsar driver, each OpTemplate has the following format:
```yaml
blocks:
  <some_block_name>:
    ops:
      <some_op_name>:
        <OpTypeIdentifier>: <tenant|namespace|topic_name>
        <op_param_1>: "<some_value>"
        <op_param_2>: "<some_value>"
        ...
```

The `OpTypeIdentifier` determines which NB Pulsar workload type (`OpType`) to run, and it has the following value:

```java
public enum PulsarOpType {
    AdminTenant,
    AdminNamespace,
    AdminTopic,
    MessageProduce,
    MessageConsume
}
```

Its value is mandatory and depending on the actual identifier, its value can be one of the following:
* ***Tenant name***: for `AdminTenant` type
* ***Namespace name***: for `AdminNamespace` type and in format "<tenant>/<namespace>"
* ***Topic name***: for the rest of the types and in format [(persistent|non-persistent)://]<tenant>/<namespace>/<topic>
  is mandatory for each NB Pulsar operation type

Each Pulsar `OpType` may have optional Op specific parameters. Please refer to [here](yaml_examples) for the example NB Pulsar YAML files for each OpType

# 4. Message Generation and Schema Support

## 4.1. Message Generation

A Pulsar message has three main components: message key, message properties, and message payload. Among them, message payload is mandatory when creating a message.

When running the "message producing" workload, the NB Pulsar driver is able to generate a message with its full content via the following OpTemplate level parameters:
* `msg_key`: defines message key value
* `msg_property`: defines message property values
* `msg_value`: defines message payload value

The actual values of them can be static or dynamic (which are determined by NB data binding rules)

For `msg_key`, its value can be either
* a plain text string, or
* a JSON string that follows the specified "key" Avro schema (when KeyValue schema is used)

For `msg_property`, its value needs to be a JSON string that contains a list of key-value pairs. An example is as below. Please **NOTE** that if the provided value is not a valid JSON string, the NB Pulsar driver will ignore it and treat the message as having no properties.
```
  msg_property: |
    {
      "prop1": "{myprop1}",
      "prop2": "{myprop2}"
    }
```

For `msg_value`, its value can be either
* a plain simple text, or
* a JSON string that follows the specified "value" Avro schema (when Avro schema or KeyValue schema is used)

## 4.2. Schema Support

The NB Pulsar driver supports the following Pulsar schema types:
* Primitive schema types
* Avro schema type (only for message payload - `msg_value`)
* KeyValue schema type (with both key and value follows an Avro schema)

The following 2 global configuration parameters define the required schema type
* `schema.key.type`: defines message key type
* `schema.type`: defines message value type
  For them, if the parameter value is not specified, it means using the default `byte[]/BYTES` type as the schema type. Otherwise, if it is specified as "avro", it means using Avro as the schema type.

The following 2 global configuration parameters define the schema specification (**ONLY** needed when Avro is the schema type)
* `schema.key.definition`: a file path that defines the message key Avro schema specification
* `schema.definition`: a file path the message value Avro schema specification
  The NB Pulsar driver will throw an error if the schema type is Avro but no schema specification definition file is not provided or is not valid.
