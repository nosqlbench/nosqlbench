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

package io.nosqlbench.adapter.s4j.util;

import io.nosqlbench.adapter.s4j.exception.S4JAdapterUnexpectedException;
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

public class S4JClientConf {
    private final static Logger logger = LogManager.getLogger(S4JClientConf.class);

    public static final String CLIENT_CONF_PREFIX = "client";
    public static final String PRODUCER_CONF_PREFIX = "producer";
    public static final String CONSUMER_CONF_PREFIX = "consumer";
    public static final String JMS_CONF_PREFIX = "jms";


    // "Raw" map is what is read from the config properties file
    // "Tgt" map is what is really needed in the Pulsar producer/consumer API
    private Map<String, String> clientConfMapRaw = new HashMap<>();
    private Map<String, String> producerConfMapRaw = new HashMap<>();
    private Map<String, String> consumerConfMapRaw = new HashMap<>();
    private Map<String, String> jmsConfMapRaw = new HashMap<>();
    private Map<String, String> miscConfMapRaw = new HashMap<>();

    private final Map<String, Object> s4jConfMapTgt = new HashMap<>();
    private Map<String, Object> clientConfMapTgt = new HashMap<>();
    private Map<String, Object> producerConfMapTgt = new HashMap<>();

    private Map<String, Object> consumerConfMapTgt = new HashMap<>();
    private Map<String, Object> jmsConfMapTgt = new HashMap<>();
    private Map<String, Object> miscConfMapTgt = new HashMap<>();



    public S4JClientConf(String webSvcUrl, String pulsarSvcUrl, String s4jConfFileName)
    throws S4JAdapterUnexpectedException {

        //////////////////
        // Read related Pulsar client configuration settings from a file
        readRawConfFromFile(s4jConfFileName);


        //////////////////
        // Ignores the following Pulsar client/producer/consumer configurations since
        // they're either not supported in the S4J API or the property must be specified
        // as the NB CLI parameter or the NB yaml file parameter.

        // <<< https://pulsar.apache.org/docs/reference-configuration/#client >>>
        // pulsar client config
        //   * webServiceUrl
        //   * brokerServiceUrl
        clientConfMapRaw.put("brokerServiceUrl", pulsarSvcUrl);
        clientConfMapRaw.put("webServiceUrl", webSvcUrl);


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


        //////////////////
        //  Convert the raw configuration map (<String,String>) to the required map (<String,Object>)
        clientConfMapTgt.putAll(S4JClientConfConverter.convertRawClientConf(clientConfMapRaw));
        producerConfMapTgt.putAll(S4JClientConfConverter.convertRawProducerConf(producerConfMapRaw));
        consumerConfMapTgt.putAll(S4JClientConfConverter.convertRawConsumerConf(consumerConfMapRaw));
        jmsConfMapTgt.putAll(S4JClientConfConverter.convertRawJmsConf(jmsConfMapRaw));
        miscConfMapTgt.putAll(S4JClientConfConverter.convertRawMiscConf(miscConfMapRaw));

        s4jConfMapTgt.putAll(clientConfMapTgt);
        s4jConfMapTgt.put("producerConfig", producerConfMapTgt);
        s4jConfMapTgt.put("consumerConfig", consumerConfMapTgt);
        s4jConfMapTgt.putAll(jmsConfMapTgt);
        s4jConfMapTgt.putAll(miscConfMapTgt);
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
                        miscConfMapRaw.put(confKey, confVal);
                    }
                }
            }
        } catch (IOException | ConfigurationException ex) {
            ex.printStackTrace();
            throw new S4JAdapterUnexpectedException("Can't read the specified config properties file: " + fileName);
        }
    }

    public Map<String, Object> getS4jConfObjMap() { return this.s4jConfMapTgt; }
    public Map<String, Object> getS4jConfMapObj_client() { return this.clientConfMapTgt; }
    public Map<String, Object> getS4jConfMapObj_producer() { return this.producerConfMapTgt; }
    public Map<String, Object> getS4jConfMapObj_consumer() { return this.consumerConfMapTgt; }
    public Map<String, Object> getS4jConfMapObj_jms() { return this.jmsConfMapTgt; }
    public Map<String, Object> getS4jConfMapObj_misc() { return this.miscConfMapTgt; }

    private Map<String, Object> mergeConfigObjMaps(
        Map<String, Object> origConfigObjMap,
        Map<String, Object> extraConfigObjMap )
    {
        Map<String, Object> newConfigObjMap = new HashMap<>();

        // If there are the same settings in both "orig" and "extra" maps,
        // the one in the "extra" map will take over
        newConfigObjMap.putAll(origConfigObjMap);
        newConfigObjMap.putAll(extraConfigObjMap);

        return newConfigObjMap;
    }
    public Map<String, Object> mergeExtraConsumerConfig(
        Map<String, String> extraConsumerConfigRaw)
    {
        if ( (extraConsumerConfigRaw == null) || (extraConsumerConfigRaw.isEmpty()) ) {
            return getS4jConfObjMap();
        }
        else {
            Map<String, Object> origConsumerConfigObjMap = getS4jConfMapObj_consumer();
            Map<String, Object> extraConsumerConfigObjMap =
                S4JClientConfConverter.convertRawConsumerConf(extraConsumerConfigRaw);
            Map<String, Object> mergedConsumerConfigObjMap =
                mergeConfigObjMaps(origConsumerConfigObjMap, extraConsumerConfigObjMap);

            Map<String, Object> mergedS4JConfObjMap = getS4jConfObjMap();
            mergedS4JConfObjMap.put("consumerConfig", mergedConsumerConfigObjMap);

            return mergedS4JConfObjMap;
        }
    }

    public String toString() {
        return new ToStringBuilder(this).
            append("effectiveS4jConfMap", s4jConfMapTgt).
            toString();
    }
}
