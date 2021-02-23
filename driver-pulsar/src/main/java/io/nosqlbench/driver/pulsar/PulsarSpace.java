package io.nosqlbench.driver.pulsar;

import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.driver.pulsar.util.PulsarNBClientConf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An instance of a pulsar client, along with all the cached objects which are normally
 * associated with it during a client session in a typical application.
 * A PulsarSpace is simply a named and cached set of objects which must be used together.
 */
public class PulsarSpace {

    private final static Logger logger = LogManager.getLogger(PulsarSpace.class);

    private final ConcurrentHashMap<String, Producer<?>> producers = new ConcurrentHashMap<>();
    // TODO: add support for other client types: consumer, reader, websocket-producer, managed-ledger, etc.

    private final String name;
    private final PulsarNBClientConf pulsarNBClientConf;

    private PulsarClient pulsarClient = null;
    private Schema<?> pulsarSchema = null;

    public PulsarSpace( String name, PulsarNBClientConf pulsarClientConf ) {
        this.name = name;
        this.pulsarNBClientConf = pulsarClientConf;

        createPulsarClientFromConf();
        createPulsarSchemaFromConf();
    }

    private void createPulsarClientFromConf() {
        ClientBuilder clientBuilder = PulsarClient.builder();

        String dftSvcUrl = "pulsar://localhost:6650";
        if ( !pulsarNBClientConf.hasClientConfKey(PulsarActivityUtil.CLNT_CONF_KEY.serviceUrl.toString()) ) {
            pulsarNBClientConf.setClientConfValue(PulsarActivityUtil.CLNT_CONF_KEY.serviceUrl.toString(), dftSvcUrl);
        }

        try {
            Map<String, Object> clientConf = pulsarNBClientConf.getClientConfMap();
            pulsarClient = clientBuilder.loadConf(clientConf).build();
        }
        catch (PulsarClientException pce) {
            logger.error("Fail to create PulsarClient from global configuration!");
            throw new RuntimeException("Fail to create PulsarClient from global configuration!");
        }
    }

    private void createPulsarSchemaFromConf() {
        String schemaType = pulsarNBClientConf.getSchemaConfValue("schema.type").toString();

        if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType)) {
            String schemaDefStr = pulsarNBClientConf.getSchemaConfValue("schema.definition").toString();
            pulsarSchema = PulsarActivityUtil.getAvroSchema(schemaType, schemaDefStr);
        } else if (PulsarActivityUtil.isPrimitiveSchemaTypeStr(schemaType)) {
            pulsarSchema = PulsarActivityUtil.getPrimitiveTypeSchema((schemaType));
        } else {
            throw new RuntimeException("Unsupported schema type string: " + schemaType + "; " +
                "Only primitive type and Avro type are supported at the moment!");
        }
    }

    public PulsarClient getPulsarClient() {
        return pulsarClient;
    }

    public PulsarNBClientConf getPulsarClientConf() {
        return pulsarNBClientConf;
    }

    public Schema<?> getPulsarSchema() {
        return pulsarSchema;
    }

    // Producer name is NOT mandatory
    // - It can be set at either global level or cycle level
    // - If set at both levels, cycle level setting takes precedence
    private String getEffectiveProducerName(String cycleProducerName) {
        // TODO: Maybe using NB run specific string as the producer name?
        String producerName = "default";

        String globalProducerName = pulsarNBClientConf.getProducerName();
        if ((globalProducerName != null) && (!globalProducerName.isEmpty())) {
            producerName = globalProducerName;
        }
        if ((cycleProducerName != null) && (!cycleProducerName.isEmpty())) {
            producerName = cycleProducerName;
        }

        return producerName;
    }

    // Topic name is mandatory
    // - It must be set at either global level or cycle level
    // - If set at both levels, cycle level setting takes precedence
    private String getEffectiveTopicName(String cycleTopicName) {
        String globalTopicName = pulsarNBClientConf.getTopicName();
        String topicName = globalTopicName;

        if ( ((globalTopicName == null) || (globalTopicName.isEmpty())) &&
            ((cycleTopicName == null) || (cycleTopicName.isEmpty())) ) {
            throw new RuntimeException("Topic name must be set at either global level or cycle level!");
        } else if ((cycleTopicName != null) && (!cycleTopicName.isEmpty())) {
            topicName = cycleTopicName;
        }

        return topicName;
    }

    private Producer createPulsarProducer(String cycleTopicName, String cycleProducerName) {
        PulsarClient pulsarClient = getPulsarClient();
        Producer producer = null;

        String producerName = getEffectiveProducerName(cycleProducerName);
        String topicName = getEffectiveTopicName(cycleTopicName);

        // Get other possible producer settings that are set at global level
        Map<String, Object> producerConf = pulsarNBClientConf.getProducerConfMap();
        producerConf.put("topicName", topicName);
        if ((producerName != null) && (!producerName.isEmpty())) {
            producerConf.put("producerName", producerName);
        }

        try {
            producer = pulsarClient.newProducer(pulsarSchema).loadConf(producerConf).create();
        }
        catch (PulsarClientException ple) {
            throw new RuntimeException("Unable to create a client to connect to the Pulsar cluster!");
        }

        return producer;
    }

    public Producer<?> getProducer(String cycleProducerName, String cycleTopicName) {
        String producerName = getEffectiveProducerName(cycleProducerName);
        String topicName = getEffectiveTopicName(cycleTopicName);

        String identifierStr = producerName.toLowerCase() + "::" + topicName.toLowerCase();
        Producer producer = producers.get(identifierStr);

        if (producer == null) {
            producer = createPulsarProducer(cycleTopicName, cycleProducerName);
            producers.put(identifierStr, producer);
        }

        return producer;
    }
}
