/*
 * Copyright (c) 2022-2023 nosqlbench
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
    private static final Logger logger = LogManager.getLogger(KafkaClientConf.class);

    public static final String TOPIC_CONF_PREFIX = "topic";
    public static final String PRODUCER_CONF_PREFIX = "producer";
    public static final String CONSUMER_CONF_PREFIX = "consumer";

    // https://kafka.apache.org/documentation/#topicconfigs
    private final Map<String, String> topicConfMap = new HashMap<>();
    private final Map<String, String> producerConfMap = new HashMap<>();
    private final Map<String, String> consumerConfMap = new HashMap<>();


    public KafkaClientConf(final String clientConfFileName) {

        //////////////////
        // Read related Kafka client configuration settings from a file
        this.readRawConfFromFile(clientConfFileName);


        //////////////////
        // Ignores the following Kafka producer/consumer configurations since
        // they're either not supported in the Kafka API or they must be specified
        // as the NB CLI parameters or the NB yaml file parameters.

        // <<< https://kafka.apache.org/documentation/#producerconfigs >>>
        // producer config
        //   * bootstrap.servers
        this.producerConfMap.remove("bootstrap.servers");

        // <<< https://kafka.apache.org/documentation/#consumerconfigs >>>
        // consumer config
        //   * bootstrap.servers
        this.consumerConfMap.remove("bootstrap.servers");

    }

    public void readRawConfFromFile(final String fileName) {
        final File file = new File(fileName);

        try {
            final String canonicalFilePath = file.getCanonicalPath();

            final Parameters params = new Parameters();

            final FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                    .configure(params.properties()
                        .setFileName(fileName));

            final Configuration config = builder.getConfiguration();

            for (final Iterator<String> it = config.getKeys(); it.hasNext(); ) {
                final String confKey = it.next();
                final String confVal = config.getProperty(confKey).toString();

                // Get client connection specific configuration settings, removing "topic." prefix
                if (!StringUtils.isBlank(confVal))
                    if (StringUtils.startsWith(confKey, KafkaClientConf.TOPIC_CONF_PREFIX))
                        this.topicConfMap.put(confKey.substring(KafkaClientConf.TOPIC_CONF_PREFIX.length() + 1), confVal);
                        // Get producer specific configuration settings, removing "producer." prefix
                    else if (StringUtils.startsWith(confKey, KafkaClientConf.PRODUCER_CONF_PREFIX))
                        this.producerConfMap.put(confKey.substring(KafkaClientConf.PRODUCER_CONF_PREFIX.length() + 1), confVal);
                        // Get consumer specific configuration settings, removing "consumer." prefix
                    else if (StringUtils.startsWith(confKey, KafkaClientConf.CONSUMER_CONF_PREFIX))
                        this.consumerConfMap.put(confKey.substring(KafkaClientConf.CONSUMER_CONF_PREFIX.length() + 1), confVal);
            }
        } catch (final IOException ioe) {
            KafkaClientConf.logger.error("Can't read the specified config properties file: {}", fileName);
            ioe.printStackTrace();
        } catch (final ConfigurationException cex) {
            KafkaClientConf.logger.error("Error loading configuration items from the specified config properties file: {}:{}", fileName, cex.getMessage());
            cex.printStackTrace();
        }
    }

    public Map<String, String> getTopicConfMap() { return this.topicConfMap; }
    public Map<String, String> getProducerConfMap() { return this.producerConfMap; }
    public Map<String, String> getConsumerConfMap() { return this.consumerConfMap; }

    public String toString() {
        return new ToStringBuilder(this).
            append("topicConfMap", this.topicConfMap).
            append("producerConfMap", this.producerConfMap).
            append("consumerConfMap", this.consumerConfMap).
            toString();
    }
}
