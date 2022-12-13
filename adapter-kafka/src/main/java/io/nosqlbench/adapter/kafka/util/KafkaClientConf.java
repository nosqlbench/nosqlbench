/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.kafka.util;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class KafkaClientConf {
    private final static Logger logger = LogManager.getLogger(KafkaClientConf.class);

    public static final String TOPIC_CONF_PREFIX = "topic";
    public static final String PRODUCER_CONF_PREFIX = "producer";
    public static final String CONSUMER_CONF_PREFIX = "consumer";

    // https://kafka.apache.org/documentation/#topicconfigs
    private Map<String, String> topicConfMap = new HashMap<>();
    private Map<String, String> producerConfMap = new HashMap<>();
    private Map<String, String> consumerConfMap = new HashMap<>();


    public KafkaClientConf(String clientConfFileName) {

        //////////////////
        // Read related Pulsar client configuration settings from a file
        readRawConfFromFile(clientConfFileName);


        //////////////////
        // Ignores the following Kafka producer/consumer configurations since
        // they're either not supported in the Kafka API or they must be specified
        // as the NB CLI parameters or the NB yaml file parameters.

        // <<< https://kafka.apache.org/documentation/#producerconfigs >>>
        // producer config
        //   * bootstrap.servers
        producerConfMap.remove("bootstrap.servers");

        // <<< https://kafka.apache.org/documentation/#consumerconfigs >>>
        // consumer config
        //   * bootstrap.servers
        consumerConfMap.remove("bootstrap.servers");

    }

    public void readRawConfFromFile(String fileName) {
        File file = new File(fileName);

        try {
            String canonicalFilePath = file.getCanonicalPath();

            Parameters params = new Parameters();

            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                    .configure(params.properties()
                        .setFileName(fileName));

            Configuration config = builder.getConfiguration();

            for (Iterator<String> it = config.getKeys(); it.hasNext(); ) {
                String confKey = it.next();
                String confVal = config.getProperty(confKey).toString();

                if (!StringUtils.isBlank(confVal)) {
                    // Get client connection specific configuration settings, removing "topic." prefix
                    if (StringUtils.startsWith(confKey, TOPIC_CONF_PREFIX)) {
                        topicConfMap.put(confKey.substring(TOPIC_CONF_PREFIX.length() + 1), confVal);
                    }
                    // Get producer specific configuration settings, removing "producer." prefix
                    else if (StringUtils.startsWith(confKey, PRODUCER_CONF_PREFIX)) {
                        producerConfMap.put(confKey.substring(PRODUCER_CONF_PREFIX.length() + 1), confVal);
                    }
                    // Get consumer specific configuration settings, removing "consumer." prefix
                    else if (StringUtils.startsWith(confKey, CONSUMER_CONF_PREFIX)) {
                        consumerConfMap.put(confKey.substring(CONSUMER_CONF_PREFIX.length() + 1), confVal);
                    }
                }
            }
        } catch (IOException ioe) {
            logger.error("Can't read the specified config properties file: " + fileName);
            ioe.printStackTrace();
        } catch (ConfigurationException cex) {
            logger.error("Error loading configuration items from the specified config properties file: " + fileName + ":" + cex.getMessage());
            cex.printStackTrace();
        }
    }

    public Map<String, String> getTopicConfMap() { return topicConfMap; }
    public Map<String, String> getProducerConfMap() { return producerConfMap; }
    public Map<String, String> getConsumerConfMap() { return consumerConfMap; }

    public String toString() {
        return new ToStringBuilder(this).
            append("topicConfMap", topicConfMap).
            append("producerConfMap", producerConfMap).
            append("consumerConfMap", consumerConfMap).
            toString();
    }
}
