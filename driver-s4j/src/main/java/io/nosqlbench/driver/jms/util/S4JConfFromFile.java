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

package io.nosqlbench.driver.jms.util;

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

public class S4JConfFromFile {
    private final static Logger logger = LogManager.getLogger(S4JConfFromFile.class);

    public static final String CLIENT_CONF_PREFIX = "client";
    public static final String PRODUCER_CONF_PREFIX = "producer";
    public static final String CONSUMER_CONF_PREFIX = "consumer";
    public static final String JMS_CONF_PREFIX = "jms";

    private final Map<String, String> s4jMiscConfMapRaw = new HashMap<>();
    private final Map<String, String> clientConfMapRaw = new HashMap<>();
    private final Map<String, String> producerConfMapRaw = new HashMap<>();
    private final Map<String, String> consumerConfMapRaw = new HashMap<>();
    private final Map<String, String> jmsConfMapRaw = new HashMap<>();


    public S4JConfFromFile(String fileName) {
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
                    // Get client connection specific configuration settings, removing "client." prefix
                    if (StringUtils.startsWith(confKey, CLIENT_CONF_PREFIX)) {
                        clientConfMapRaw.put(confKey.substring(CLIENT_CONF_PREFIX.length() + 1), confVal);
                    }
                    // Get producer specific configuration settings, removing "producer." prefix
                    else if (StringUtils.startsWith(confKey, PRODUCER_CONF_PREFIX)) {
                        producerConfMapRaw.put(confKey.substring(PRODUCER_CONF_PREFIX.length() + 1), confVal);
                    }
                    // Get consumer specific configuration settings, removing "consumer." prefix
                    else if (StringUtils.startsWith(confKey, CONSUMER_CONF_PREFIX)) {
                        consumerConfMapRaw.put(confKey.substring(CONSUMER_CONF_PREFIX.length() + 1), confVal);
                    }
                    // Get JMS specific configuration settings, keeping "jms." prefix
                    else if (StringUtils.startsWith(confKey, JMS_CONF_PREFIX)) {
                        jmsConfMapRaw.put(confKey, confVal);
                    }
                    // For all other configuration settings (not having any of the above prefixes), keep as is
                    else {
                        s4jMiscConfMapRaw.put(confKey, confVal);
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

        // Ignores the following Pulsar client/producer/consumer configurations since
        // they're either not supported in the S4J API or the property must be specified
        // as the NB CLI parameter or the NB yaml file parameter.

        // <<< https://pulsar.apache.org/docs/reference-configuration/#client >>>
        // pulsar client confi
        //   * webServiceUrl
        //   * brokerServiceUrl
        clientConfMapRaw.remove("brokerServiceUrl");
        clientConfMapRaw.remove("webServiceUrl");

        // <<< https://pulsar.apache.org/docs/client-libraries-java/#configure-producer >>>
        // producer config
        //   * topicName
        producerConfMapRaw.remove("topicName");

        // <<< https://pulsar.apache.org/docs/client-libraries-java/#configure-consumer >>>
        // consumer config
        //   * topicNames
        //   * topicsPattern
        //   * subscriptionName
        //   * subscriptionType
        consumerConfMapRaw.remove("topicNames");
        consumerConfMapRaw.remove("topicPattern");
        consumerConfMapRaw.remove("subscriptionName");
        consumerConfMapRaw.remove("subscriptionType");
        consumerConfMapRaw.remove("subscriptionInitialPosition");
        consumerConfMapRaw.remove("regexSubscriptionMode");
    }

    public Map<String, String> getS4jMiscConfMapRaw() { return  this.s4jMiscConfMapRaw; }
    public Map<String, String> getClientConfMapRaw() { return this.clientConfMapRaw; }
    public Map<String, String> getProducerConfMapRaw() { return this.producerConfMapRaw; }
    public Map<String, String> getConsumerConfMapRaw() { return this.consumerConfMapRaw; }
    public Map<String, String> getJmsConfMapRaw() { return this.jmsConfMapRaw; }

    public String toString() {
        return new ToStringBuilder(this).
            append("s4jMiscConfMapRaw", s4jMiscConfMapRaw.toString()).
            append("clientConfMapRaw", clientConfMapRaw.toString()).
            append("producerConfMapRaw", producerConfMapRaw.toString()).
            append("consumerConfMapRaw", consumerConfMapRaw.toString()).
            append("jmsConfMapRaw", jmsConfMapRaw.toString()).
            toString();
    }
}
