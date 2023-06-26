- [1. Overview](#1-overview)
- [2. NB S4R Usage](#2-nb-s4r-usage)
    - [2.1. CLI Examples](#21-cli-examples)
    - [2.2. CLI parameters](#22-cli-parameters)
    - [2.3. Workload Definition](#23-workload-definition)
    - [2.4. Configuration Properties](#24-configuration-properties)
        - [2.4.1. Global Properties File](#241-global-properties-file)
        - [2.4.2. Scenario Document Level Properties](#242-scenario-document-level-properties)

---

# 1. Overview

This NB S4R adapter allows sending messages to or receiving messages from
* an AMQP 0-9-1 based server (e.g. RabbitMQ), or
* a Pulsar cluster with [S4R](https://github.com/datastax/starlight-for-rabbitmq) AMQP (0-9-1) Protocol handler for Pulsar.

At high level, this adapter supports the following AMQP 0-9-1 functionalities
* Creating AMQP connections and channels
* Declaring AMQP exchanges
    * The following exchange types are supported: `direct`, `fanout`, `topic`, and `headers`
* Sending messages to AMQP exchanges with sync. or async. publisher confirms
    * For sync confirms, it supports both single and batch confirms
    * Supports message-send based on routing keys
* Declaring and binding AMQP queues
    * Supports message-receive based on binding keys
* Receiving messages from AMQP queues with async. consumer acks

# 2. NB S4R Usage

## 2.1. CLI Examples

```bash
## AMQP Message Sender
$ <nb_cmd> run driver=s4r -vv cycles=200 strict_msg_error_handling=0 \
  threads=8 num_conn=1 num_channel=2 num_exchange=2 num_msg_clnt=2 \
  workload=./s4r_msg_sender.yaml \
  config=./s4r_config.properties

## AMQP Message Receiver
$ <nb_cmd> run driver=s4r -vv cycles=200 strict_msg_error_handling=0 \
  threads=8 num_conn=1 num_channel=2 num_exchange=2 num_queue=2 num_msg_clnt=2 \
  workload=./s4r_msg_receiver.yaml \
  config=./s4r_config.properties
```

## 2.2. CLI parameters

The following CLI parameters are unique to the S4R adapter:

* `num_conn`: the number of AMQP connections to create
* `num_channel`: the number of AMQP channels to create for each connection
* `num_exchange`: the number of AMQP exchanges to create for each channel
* `num_queue`: the number of AMQP queues to create for each channel (only relevant for message receiver workload)
* `num_msg_client`: the number of message clients to create for each channel
    * for message sender workload, it is the number of message publishers for each exchange
    * for message receiver workload, it is the number of message consumers for each queue

## 2.3. Workload Definition

The example workload YAML files can be found from:

* [s4r_msg_sender.yaml](s4r_msg_sender.yaml)
* [s4r_msg_receiver.yaml](s4r_msg_receiver.yaml)

## 2.4. Configuration Properties

### 2.4.1. Global Properties File

A global S4R properties file can be specified via the `config` CLI parameter. It includes the following required properties:
* `amqpSrvHost`: AMQP server host (e.g. An Astra Streaming cluster with S4R enabled)
* `amqpSrvPort`: AMQP server port (for S4R enabled Astra Streaming, it is 5671)
* `virtualHost`: AMQP server virtual host (for S4R enabled Astra Streaming, it is "<tenant>/rabbitmq")
* `amqpUser`: AMQP user (for S4R enabled Astra Streaming, it is an empty string)
* `amqpPassword`: AMQP password (for S4R enabled Astra Streaming, it is the JWT token file path)
* `useTls`: whether to use TLS (for S4R enabled Astra Streaming, it is true)
* `exchangeType`: AMQP exchange type (e.g. `direct`, `fanout`, `topic`, or `headers`)

An example of this file can be found from: [s4r_config.properties](./s4r_config.properties)

### 2.4.2. Scenario Document Level Properties

For message sender workload, the following Document level configuration parameters are supported in the YAML file:
* `publisher_confirm`: whether to use publisher confirms
* `confirm_mode`: When `publisher_confirm` is true, the following 3 confirm modes are supported:
    * `individual`: wait for confirm individually
    * `batch`: wait for confirm in batch
    * `async`: [default] no wait for confirm
* `confirm_batch_num`: batch size for waiting for **sync** publisher confirms
    * Only relevant when `publisher_confirm` is true and `confirm_mode` is "batch"
* `dft_confirm_timeout_ms`: batch size for waiting for publisher confirms
    * Only relevant when `publisher_confirm` is true and `confirm_mode` is **NOT** "async"
