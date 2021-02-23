package io.nosqlbench.driver.pulsar.util;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PulsarNBClientConf {

    private final static Logger logger = LogManager.getLogger(PulsarNBClientConf.class);

    private String canonicalFilePath = "";

    private static final String DRIVER_CONF_PREFIX = "driver";
    private static final String SCHEMA_CONF_PREFIX = "schema";
    private static final String CLIENT_CONF_PREFIX = "client";
    private static final String PRODUCER_CONF_PREFIX = "producer";
    private HashMap<String, Object> driverConfMap = new HashMap<>();
    private HashMap<String, Object> schemaConfMap = new HashMap<>();
    private HashMap<String, Object> clientConfMap = new HashMap<>();
    private HashMap<String, Object> producerConfMap = new HashMap<>();
    // TODO: add support for other operation types: consumer, reader, websocket-producer, managed-ledger

    public PulsarNBClientConf(String fileName) {
        File file = new File(fileName);

        try {
            canonicalFilePath = file.getCanonicalPath();

            Parameters params = new Parameters();

            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                    .configure(params.properties()
                        .setFileName(fileName)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));

            Configuration config = builder.getConfiguration();

            // Get driver specific configuration settings
            for (Iterator<String> it = config.getKeys(DRIVER_CONF_PREFIX); it.hasNext(); ) {
                String confKey = it.next();
                driverConfMap.put(confKey.substring(DRIVER_CONF_PREFIX.length()+1), config.getProperty(confKey));
            }

            // Get schema specific configuration settings
            for (Iterator<String> it = config.getKeys(SCHEMA_CONF_PREFIX); it.hasNext(); ) {
                String confKey = it.next();
                schemaConfMap.put(confKey.substring(SCHEMA_CONF_PREFIX.length()+1), config.getProperty(confKey));
            }

            // Get client connection specific configuration settings
            for (Iterator<String> it = config.getKeys(CLIENT_CONF_PREFIX); it.hasNext(); ) {
                String confKey = it.next();
                clientConfMap.put(confKey.substring(CLIENT_CONF_PREFIX.length()+1), config.getProperty(confKey));
            }

            // Get producer specific configuration settings
            for (Iterator<String> it = config.getKeys(PRODUCER_CONF_PREFIX); it.hasNext(); ) {
                String confKey = it.next();
                producerConfMap.put(confKey.substring(PRODUCER_CONF_PREFIX.length()+1), config.getProperty(confKey));
            }
        }
        catch (IOException ioe) {
            logger.error("Can't read the specified config properties file!");
            ioe.printStackTrace();
        }
        catch (ConfigurationException cex) {
            logger.error("Error loading configuration items from the specified config properties file!");
            cex.printStackTrace();
        }
    }

    // Get NB Driver related config
    public Map<String, Object> getDriverConfMap() {
        return this.driverConfMap;
    }
    public boolean hasDriverConfKey(String key) {
        if (key.contains(DRIVER_CONF_PREFIX))
            return driverConfMap.containsKey(key.substring(DRIVER_CONF_PREFIX.length()+1));
        else
            return driverConfMap.containsKey(key);
    }
    public Object getDriverConfValue(String key) {
        if (key.contains(DRIVER_CONF_PREFIX))
            return driverConfMap.get(key.substring(DRIVER_CONF_PREFIX.length()+1));
        else
            return driverConfMap.get(key);
    }
    public void setDriverConfValue(String key, Object value) {
        if (key.contains(DRIVER_CONF_PREFIX))
            driverConfMap.put(key.substring(DRIVER_CONF_PREFIX.length()+1), value);
        else
            driverConfMap.put(key, value);
    }

    // Get Schema related config
    public Map<String, Object> getSchemaConfMap() {
        return this.schemaConfMap;
    }
    public boolean hasSchemaConfKey(String key) {
        if (key.contains(SCHEMA_CONF_PREFIX))
            return schemaConfMap.containsKey(key.substring(SCHEMA_CONF_PREFIX.length()+1));
        else
            return schemaConfMap.containsKey(key);
    }
    public Object getSchemaConfValue(String key) {
        if (key.contains(SCHEMA_CONF_PREFIX))
            return schemaConfMap.get(key.substring(SCHEMA_CONF_PREFIX.length()+1));
        else
            return schemaConfMap.get(key);
    }
    public void setSchemaConfValue(String key, Object value) {
        if (key.contains(SCHEMA_CONF_PREFIX))
            schemaConfMap.put(key.substring(SCHEMA_CONF_PREFIX.length()+1), value);
        else
            schemaConfMap.put(key, value);
    }

    // Get Pulsar client related config
    public Map<String, Object> getClientConfMap() {
        return this.clientConfMap;
    }
    public boolean hasClientConfKey(String key) {
        if (key.contains(CLIENT_CONF_PREFIX))
            return clientConfMap.containsKey(key.substring(CLIENT_CONF_PREFIX.length()+1));
        else
            return clientConfMap.containsKey(key);
    }
    public Object getClientConfValue(String key) {
        if (key.contains(CLIENT_CONF_PREFIX))
            return clientConfMap.get(key.substring(CLIENT_CONF_PREFIX.length()+1));
        else
            return clientConfMap.get(key);
    }
    public void setClientConfValue(String key, Object value) {
        if (key.contains(CLIENT_CONF_PREFIX))
            clientConfMap.put(key.substring(CLIENT_CONF_PREFIX.length()+1), value);
        else
            clientConfMap.put(key, value);
    }

    // Get Pulsar producer related config
    public Map<String, Object> getProducerConfMap() {
        return this.producerConfMap;
    }
    public boolean hasProducerConfKey(String key) {
        if (key.contains(PRODUCER_CONF_PREFIX))
            return producerConfMap.containsKey(key.substring(PRODUCER_CONF_PREFIX.length()+1));
        else
            return producerConfMap.containsKey(key);
    }
    public Object getProducerConfValue(String key) {
        if (key.contains(PRODUCER_CONF_PREFIX))
            return producerConfMap.get(key.substring(PRODUCER_CONF_PREFIX.length()+1));
        else
            return producerConfMap.get(key);
    }
    public void setProducerConfValue(String key, Object value) {
        if (key.contains(PRODUCER_CONF_PREFIX))
            producerConfMap.put(key.substring(PRODUCER_CONF_PREFIX.length()+1), value);
        else
            producerConfMap.put(key, value);
    }

    public String getPulsarClientType() {
        Object confValue = getDriverConfValue("driver.client-type");

        // If not explicitly specifying Pulsar client type, "producer" is the default type
        if (confValue == null)
            return PulsarActivityUtil.CLIENT_TYPES.PRODUCER.toString();
        else
            return confValue.toString();
    }

    public String getProducerName() {
        Object confValue = getProducerConfValue("producer.producerName");

        // If not explicitly specifying Pulsar client type, "producer" is the default type
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }

    public String getTopicName() {
        Object confValue = getProducerConfValue("producer.topicName");

        // If not explicitly specifying Pulsar client type, "producer" is the default type
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }
}
