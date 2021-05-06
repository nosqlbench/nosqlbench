package io.nosqlbench.driver.jms.util;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PulsarConfig {
    private final static Logger logger = LogManager.getLogger(PulsarConfig.class);

    public static final String SCHEMA_CONF_PREFIX = "schema";
    public static final String CLIENT_CONF_PREFIX = "client";
    public static final String PRODUCER_CONF_PREFIX = "producer";
    public static final String CONSUMER_CONF_PREFIX = "consumer";

    private final Map<String, Object> schemaConfMap = new HashMap<>();
    private final Map<String, Object> clientConfMap = new HashMap<>();
    private final Map<String, Object> producerConfMap = new HashMap<>();
    private final Map<String, Object> consumerConfMap = new HashMap<>();

    public PulsarConfig(String fileName) {
        File file = new File(fileName);

        try {
            String canonicalFilePath = file.getCanonicalPath();

            Parameters params = new Parameters();

            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                    .configure(params.properties()
                        .setFileName(fileName));

            Configuration config = builder.getConfiguration();

            // Get schema specific configuration settings
            for (Iterator<String> it = config.getKeys(SCHEMA_CONF_PREFIX); it.hasNext(); ) {
                String confKey = it.next();
                String confVal = config.getProperty(confKey).toString();
                if (!StringUtils.isBlank(confVal))
                    schemaConfMap.put(confKey.substring(SCHEMA_CONF_PREFIX.length() + 1), config.getProperty(confKey));
            }

            // Get client connection specific configuration settings
            for (Iterator<String> it = config.getKeys(CLIENT_CONF_PREFIX); it.hasNext(); ) {
                String confKey = it.next();
                String confVal = config.getProperty(confKey).toString();
                if (!StringUtils.isBlank(confVal))
                    clientConfMap.put(confKey.substring(CLIENT_CONF_PREFIX.length() + 1), config.getProperty(confKey));
            }

            // Get producer specific configuration settings
            for (Iterator<String> it = config.getKeys(PRODUCER_CONF_PREFIX); it.hasNext(); ) {
                String confKey = it.next();
                String confVal = config.getProperty(confKey).toString();
                if (!StringUtils.isBlank(confVal))
                    producerConfMap.put(confKey.substring(PRODUCER_CONF_PREFIX.length() + 1), config.getProperty(confKey));
            }

            // Get consumer specific configuration settings
            for (Iterator<String> it = config.getKeys(CONSUMER_CONF_PREFIX); it.hasNext(); ) {
                String confKey = it.next();
                String confVal = config.getProperty(confKey).toString();
                if (!StringUtils.isBlank(confVal))
                    consumerConfMap.put(confKey.substring(CONSUMER_CONF_PREFIX.length() + 1), config.getProperty(confKey));
            }
        } catch (IOException ioe) {
            logger.error("Can't read the specified config properties file: " + fileName);
            ioe.printStackTrace();
        } catch (ConfigurationException cex) {
            logger.error("Error loading configuration items from the specified config properties file: " + fileName);
            cex.printStackTrace();
        }
    }

    public Map<String, Object> getSchemaConfMap() {
        return this.schemaConfMap;
    }
    public Map<String, Object> getClientConfMap() {
        return this.clientConfMap;
    }
    public Map<String, Object> getProducerConfMap() {
        return this.producerConfMap;
    }
    public Map<String, Object> getConsumerConfMap() {
        return this.consumerConfMap;
    }
}
