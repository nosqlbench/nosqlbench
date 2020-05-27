package com.datastax.ebdrivers.kafkaproducer;

import io.nosqlbench.activitytype.stdout.StdoutActivity;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class KafkaProducerActivity extends StdoutActivity {
    private final static Logger logger = LoggerFactory.getLogger(KafkaProducerActivity.class);
    private Producer<Long,String> producer = null;
    private String topic;

    public KafkaProducerActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    public synchronized Producer<Long,String> getKafkaProducer() {
        if (producer!=null) {
            return producer;
        }
        Properties props = new Properties();
        String servers = Arrays.stream(activityDef.getParams().getOptionalString("host","hosts")
                .orElse("localhost" + ":9092")
                .split(","))
                .map(x ->  x.indexOf(':') == -1 ? x + ":9092" : x)
                .collect(Collectors.joining(","));
        String clientId = activityDef.getParams().getOptionalString("clientid","client.id","client_id")
                .orElse("TestProducerClientId");
        String key_serializer =
                activityDef.getParams().getOptionalString("key_serializer").orElse(LongSerializer.class.getName());
        String value_serializer =
                activityDef.getParams().getOptionalString("value_serializer").orElse(StringSerializer.class.getName());

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,  servers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, key_serializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, value_serializer);

        producer = new KafkaProducer<>(props);
        return producer;
    }

    @Override
    public synchronized void write(String statement) {
        Producer<Long, String> kafkaProducer = getKafkaProducer();
        ProducerRecord<Long, String> record = new ProducerRecord<>(topic, statement);
        Future<RecordMetadata> send = kafkaProducer.send(record);
        try {
            RecordMetadata result = send.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        this.topic = activityDef.getParams().getOptionalString("topic").orElse("default-topic");
        super.onActivityDefUpdate(activityDef);
    }
}
