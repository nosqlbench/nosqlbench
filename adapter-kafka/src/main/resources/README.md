# Overview

This NB Kafka driver allows publishing messages to or consuming messages from
* a Kafka cluster, or
* a Pulsar cluster with [S4K](https://github.com/datastax/starlight-for-kafka) or [KoP](https://github.com/streamnative/kop) Kafka Protocol handler for Pulsar.

At high level, this driver supports the following Kafka functionalities
* Publishing messages to one Kafka topic with sync. or async. message-send acknowledgements (from brokers)
* Subscribing messages from one or multiple Kafka topics with sync. or async. message-recv acknowlegements (to brokers) (aka, message commits)
    * auto message commit
    * manual message commit with a configurable number of message commits in one batch
* Kafka Transaction support

## Example NB Yaml
* [kafka_producer.yaml](./kafka_producer.yaml)
*
* [kafka_consumer.yaml](./kafka_consumer.yaml)

# Usage

```bash
## Kafka Producer
$ <nb_cmd> run driver=kafka -vv cycles=100 threads=2 num_clnt=2 yaml=kafka_producer.yaml config=kafka_config.properties bootstrap_server=PLAINTEXT://localhost:9092

## Kafka Consumer
$ <nb_cmd> run driver=kafka -vv cycles=100 threads=4 num_clnt=2 num_cons_grp=2 yaml=kafka_producer.yaml config=kafka_config.properties bootstrap_server=PLAINTEXT://localhost:9092
```

## NB Kafka driver specific CLI parameters

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
