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
    // TODO: add support for other client types: consumer, reader, websocket-producer, managed-ledger, etc.

    protected final String name;
    protected final PulsarNBClientConf pulsarNBClientConf;

    protected PulsarClient pulsarClient = null;
    protected Schema<?> pulsarSchema = null;

    public PulsarSpace(String name, PulsarNBClientConf pulsarClientConf) {
        this.name = name;
        this.pulsarNBClientConf = pulsarClientConf;

        createPulsarClientFromConf();
        createPulsarSchemaFromConf();
    }

    protected void createPulsarClientFromConf() {
        ClientBuilder clientBuilder = PulsarClient.builder();

        String dftSvcUrl = "pulsar://localhost:6650";
        if (!pulsarNBClientConf.hasClientConfKey(PulsarActivityUtil.CLNT_CONF_KEY.serviceUrl.toString())) {
            pulsarNBClientConf.setClientConfValue(PulsarActivityUtil.CLNT_CONF_KEY.serviceUrl.toString(), dftSvcUrl);
        }

        try {
            Map<String, Object> clientConf = pulsarNBClientConf.getClientConfMap();
            pulsarClient = clientBuilder.loadConf(clientConf).build();
        } catch (PulsarClientException pce) {
            logger.error("Fail to create PulsarClient from global configuration!");
            throw new RuntimeException("Fail to create PulsarClient from global configuration!");
        }
    }

    protected void createPulsarSchemaFromConf() {
        Object value = pulsarNBClientConf.getSchemaConfValue("schema.type");
        String schemaType = (value != null) ? value.toString() : "";

        if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType)) {
            value = pulsarNBClientConf.getSchemaConfValue("schema.definition");
            String schemaDefStr = (value != null) ? value.toString() : "";
            pulsarSchema = PulsarActivityUtil.getAvroSchema(schemaType, schemaDefStr);
        } else if (PulsarActivityUtil.isPrimitiveSchemaTypeStr(schemaType)) {
            pulsarSchema = PulsarActivityUtil.getPrimitiveTypeSchema((schemaType));
        } else {
            throw new RuntimeException("Unsupported schema type string: " + schemaType + "; " +
                "Only primitive type and Avro type are supported at the moment!");
        }
    }

    public PulsarClient getPulsarClient() { return pulsarClient; }
    public PulsarNBClientConf getPulsarClientConf() {
        return pulsarNBClientConf;
    }
    public Schema<?> getPulsarSchema() { return pulsarSchema; }
}
