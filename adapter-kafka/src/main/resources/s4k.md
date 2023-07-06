---
weight: 0
title: Kafka
---
- [1. Overview](#1-overview)
    - [1.1. Example NB Yaml](#11-example-nb-yaml)
- [2. Usage](#2-usage)
    - [2.1. NB Kafka adapter specific CLI parameters](#21-nb-kafka-adapter-specific-cli-parameters)

---

# 1. Overview

The NB Kafka adapter allows publishing messages to or consuming messages from
* a Kafka cluster, or
* a Pulsar cluster with [S4K](https://github.com/datastax/starlight-for-kafka) or [KoP](https://github.com/streamnative/kop) Kafka Protocol handler for Pulsar.

At high level, this adapter supports the following Kafka functionalities
* Publishing messages to one Kafka topic with sync. or async. message-send acknowledgements (from brokers)
* Subscribing messages from one or multiple Kafka topics with sync. or async. message-recv acknowledgements (to brokers) (aka, message commits)
    * auto message commit
    * manual message commit with a configurable number of message commits in one batch
* Kafka Transaction support

## 1.1. Example NB Yaml
* [kafka_producer.yaml](scenarios/kafka_producer.yaml)
*
* [kafka_consumer.yaml](scenarios/kafka_consumer.yaml)

# 2. Usage

```bash
## Kafka Producer
$ <nb_cmd> run driver=kafka -vv cycles=100 threads=2 num_clnt=2 yaml=kafka_producer.yaml config=kafka_config.properties bootstrap_server=PLAINTEXT://localhost:9092

## Kafka Consumer
$ <nb_cmd> run driver=kafka -vv cycles=100 threads=4 num_clnt=2 num_cons_grp=2 yaml=kafka_producer.yaml config=kafka_config.properties bootstrap_server=PLAINTEXT://localhost:9092
```

## 2.1. NB Kafka adapter specific CLI parameters

* `num_clnt`: the number of Kafka clients to publish messages to or to receive messages from
    * For producer workload, this is the number of the producer threads to publish messages to the same topic
        * Can have multiple producer threads for one topic/partition (`KafkaProducer` is thread-safe)
        * `threads` and `num_clnt` values MUST be the same.
    * For consumer workload, this is the partition number of a topic
        * Consumer workload supports to subscribe from multiple topics. If so, it requires all topics having the same partition number.
        * Only one consumer thread for one topic/partition (`KafkaConsumer` is NOT thread-safe)
        * `threads` MUST be equal to `num_clnt`*`num_cons_grp`

* `num_cons_grp`: the number of consumer groups
    * Only relevant for consumer workload

For the Kafka NB adapter, Document level parameters can only be statically bound; and currently, the following Document level configuration parameters are supported:

* `async_api` (boolean):
    * When true, use async Kafka client API.
* `seq_tracking` (boolean):
    * When true, a sequence number is created as part of each message's properties
    * This parameter is used in conjunction with the next one in order to simulate abnormal message processing errors and then be able to detect such errors successfully.
* `seqerr_simu`:
    * A list of error simulation types separated by comma (,)
    * Valid error simulation types
        * `out_of_order`: simulate message out of sequence
        * `msg_loss`: simulate message loss
        * `msg_dup`: simulate message duplication
    * This value should be used only for testing purposes. It is not recommended to use this parameter in actual testing environments.
* `e2e_starting_time_source`:
    * Starting timestamp for end-to-end operation. When specified, will update the `e2e_msg_latency` histogram with the calculated end-to-end latency. The latency is calculated by subtracting the starting time from the current time. The starting time is determined from a configured starting time source. The unit of the starting time is milliseconds since epoch.
    * The possible values for `e2e_starting_time_source`:
        * `message_publish_time` : uses the message publishing timestamp as the starting time. The message publishing time, in this case, [is computed by the Kafka client on record generation](https://kafka.apache.org/34/javadoc/org/apache/kafka/clients/producer/ProducerRecord.html). This is the case, as [`CreateTime` is the default](https://docs.confluent.io/platform/current/installation/configuration/topic-configs.html#message-timestamp-type).
