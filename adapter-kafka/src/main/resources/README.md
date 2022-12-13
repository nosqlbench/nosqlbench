# Usage

```bash
## Kafka Producer
$ <nb_cmd> run driver=kafka -vv cycles=100 threads=2 num_clnt=2 yaml=kafka_producer.yaml config=kafka_config.properties bootstrap_server=PLAINTEXT://10.166.90.94:9092

## Kafka Consumer
$ <nb_cmd> run driver=kafka -vv cycles=100 threads=4 num_clnt=2 num_cons_grp=2 yaml=kafka_producer.yaml config=kafka_config.properties bootstrap_server=PLAINTEXT://10.166.90.94:9092
```

# Example NB Yaml

[kafka_producer.yaml](./kafka_producer.yaml)

[kafka_consumer.yaml](./kafka_consumer.yaml)
