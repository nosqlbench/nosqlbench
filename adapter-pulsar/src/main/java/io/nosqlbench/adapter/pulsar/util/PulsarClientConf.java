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

package io.nosqlbench.adapter.pulsar.util;

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

public class PulsarClientConf {

    private final static Logger logger = LogManager.getLogger(PulsarClientConf.class);

    private String canonicalFilePath = "";

    public static final String SCHEMA_CONF_PREFIX = "schema";
    public static final String CLIENT_CONF_PREFIX = "client";
    public static final String PRODUCER_CONF_PREFIX = "producer";
    public static final String CONSUMER_CONF_PREFIX = "consumer";
    public static final String READER_CONF_PREFIX = "reader";
    private final Map<String, String> schemaConfMapRaw = new HashMap<>();
    private final Map<String, String> clientConfMapRaw = new HashMap<>();

    // "Raw" map is what is read from the config properties file
    // "Tgt" map is what is really needed in the Pulsar producer/consumer/reader API
    private final Map<String, String> producerConfMapRaw = new HashMap<>();
    private final Map<String, Object> producerConfMapTgt = new HashMap<>();

    private final Map<String, String> consumerConfMapRaw = new HashMap<>();
    private final Map<String, Object> consumerConfMapTgt = new HashMap<>();

    private final Map<String, String> readerConfMapRaw = new HashMap<>();
    private final Map<String, Object> readerConfMapTgt = new HashMap<>();

    public PulsarClientConf(String fileName) {

        //////////////////
        // Read related Pulsar client configuration settings from a file
        readRawConfFromFile(fileName);


        //////////////////
        // Ignores the following Pulsar client/producer/consumer configurations since
        // they need to be specified either as the NB CLI parameters or as the NB yaml
        // OpTemplate parameters.
        clientConfMapRaw.remove("brokerServiceUrl");
        clientConfMapRaw.remove("webServiceUrl");


        //////////////////
        //  Convert the raw configuration map (<String,String>) to the required map (<String,Object>)
        producerConfMapTgt.putAll(PulsarConfConverter.convertRawProducerConf(producerConfMapRaw));
        consumerConfMapTgt.putAll(PulsarConfConverter.convertRawConsumerConf(consumerConfMapRaw));
        // TODO: Reader API is not disabled at the moment. Revisit when needed
    }


    public void readRawConfFromFile(String fileName) {
        File file = new File(fileName);

        try {
            canonicalFilePath = file.getCanonicalPath();

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

                    // Get schema specific configuration settings, removing "schema." prefix
                    if (StringUtils.startsWith(confKey, SCHEMA_CONF_PREFIX)) {
                        schemaConfMapRaw.put(confKey.substring(SCHEMA_CONF_PREFIX.length() + 1), confVal);
                    }
                    // Get client connection specific configuration settings, removing "client." prefix
                    // <<< https://pulsar.apache.org/docs/reference-configuration/#client >>>
                    else if (StringUtils.startsWith(confKey, CLIENT_CONF_PREFIX)) {
                        clientConfMapRaw.put(confKey.substring(CLIENT_CONF_PREFIX.length() + 1), confVal);
                    }
                    // Get producer specific configuration settings, removing "producer." prefix
                    // <<< https://pulsar.apache.org/docs/client-libraries-java/#configure-producer >>>
                    else if (StringUtils.startsWith(confKey, PRODUCER_CONF_PREFIX)) {
                        producerConfMapRaw.put(confKey.substring(PRODUCER_CONF_PREFIX.length() + 1), confVal);
                    }
                    // Get consumer specific configuration settings, removing "consumer." prefix
                    // <<< https://pulsar.apache.org/docs/client-libraries-java/#configure-consumer >>>
                    else if (StringUtils.startsWith(confKey, CONSUMER_CONF_PREFIX)) {
                        consumerConfMapRaw.put(confKey.substring(CONSUMER_CONF_PREFIX.length() + 1), confVal);
                    }
                    // Get reader specific configuration settings, removing "reader." prefix
                    // <<< https://pulsar.apache.org/docs/2.10.x/client-libraries-java/#configure-reader >>>
                    else if (StringUtils.startsWith(confKey, READER_CONF_PREFIX)) {
                        readerConfMapRaw.put(confKey.substring(READER_CONF_PREFIX.length() + 1), confVal);
                    }
                }
            }
        } catch (IOException ioe) {
            logger.error("Can't read the specified config properties file!");
            ioe.printStackTrace();
        } catch (ConfigurationException cex) {
            logger.error("Error loading configuration items from the specified config properties file: " + canonicalFilePath);
            cex.printStackTrace();
        }
    }


    public Map<String, String> getSchemaConfMapRaw() { return  this.schemaConfMapRaw; }
    public Map<String, String> getClientConfMapRaw() { return this.clientConfMapRaw; }
    public Map<String, String> getProducerConfMapRaw() { return this.producerConfMapRaw; }
    public Map<String, Object> getProducerConfMapTgt() { return this.producerConfMapTgt; }
    public Map<String, String> getConsumerConfMapRaw() { return this.consumerConfMapRaw; }
    public Map<String, Object> getConsumerConfMapTgt() { return this.consumerConfMapTgt; }
    public Map<String, String> getReaderConfMapRaw() { return this.readerConfMapRaw; }
    public Map<String, Object> getReaderConfMapTgt() { return this.readerConfMapTgt; }


    public String toString() {
        return new ToStringBuilder(this).
            append("schemaConfMapRaw", schemaConfMapRaw.toString()).
            append("clientConfMapRaw", clientConfMapRaw.toString()).
            append("producerConfMapRaw", producerConfMapRaw.toString()).
            append("consumerConfMapRaw", consumerConfMapRaw.toString()).
            append("readerConfMapRaw", readerConfMapRaw.toString()).
            toString();
    }

    //////////////////
    // Get Schema related config
    public boolean hasSchemaConfKey(String key) {
        if (key.contains(SCHEMA_CONF_PREFIX))
            return schemaConfMapRaw.containsKey(key.substring(SCHEMA_CONF_PREFIX.length() + 1));
        else
            return schemaConfMapRaw.containsKey(key);
    }
    public String getSchemaConfValue(String key) {
        if (key.contains(SCHEMA_CONF_PREFIX))
            return schemaConfMapRaw.get(key.substring(SCHEMA_CONF_PREFIX.length()+1));
        else
            return schemaConfMapRaw.get(key);
    }


    //////////////////
    // Get Pulsar client related config
    public String getClientConfValue(String key) {
        if (key.contains(CLIENT_CONF_PREFIX))
            return clientConfMapRaw.get(key.substring(CLIENT_CONF_PREFIX.length()+1));
        else
            return clientConfMapRaw.get(key);
    }


    //////////////////
    // Get Pulsar producer related config
    public Object getProducerConfValue(String key) {
        if (key.contains(PRODUCER_CONF_PREFIX))
            return producerConfMapTgt.get(key.substring(PRODUCER_CONF_PREFIX.length()+1));
        else
            return producerConfMapTgt.get(key);
    }
    // other producer helper functions ...
    public String getProducerName() {
        Object confValue = getProducerConfValue(
            "producer." + PulsarAdapterUtil.PRODUCER_CONF_STD_KEY.producerName.label);
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }
    public String getProducerTopicName() {
        Object confValue = getProducerConfValue(
            "producer." + PulsarAdapterUtil.PRODUCER_CONF_STD_KEY.topicName);
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }


    //////////////////
    // Get Pulsar consumer related config
    public String getConsumerConfValue(String key) {
        if (key.contains(CONSUMER_CONF_PREFIX))
            return consumerConfMapRaw.get(key.substring(CONSUMER_CONF_PREFIX.length() + 1));
        else
            return consumerConfMapRaw.get(key);
    }
    // Other consumer helper functions ...
    public String getConsumerTopicNames() {
        String confValue = getConsumerConfValue(
            "consumer." + PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.topicNames.label);
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }
    public String getConsumerTopicPattern() {
        Object confValue = getConsumerConfValue(
            "consumer." + PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label);
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }
    public String getConsumerSubscriptionName() {
        Object confValue = getConsumerConfValue(
            "consumer." + PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionName.label);
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }
    public String getConsumerSubscriptionType() {
        Object confValue = getConsumerConfValue(
            "consumer." + PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionType.label);
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }
    public String getConsumerName() {
        Object confValue = getConsumerConfValue(
            "consumer." + PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.consumerName.label);
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }
    // NOTE: Below are not a standard Pulsar consumer configuration parameter as
    //          listed in "https://pulsar.apache.org/docs/en/client-libraries-java/#configure-consumer"
    //       They're custom-made configuration properties for NB pulsar driver consumer.
    public int getConsumerTimeoutSeconds() {
        Object confValue = getConsumerConfValue(
            "consumer." + PulsarAdapterUtil.CONSUMER_CONF_CUSTOM_KEY.timeout.label);
        if (confValue == null)
            return -1; // infinite
        else
            return Integer.parseInt(confValue.toString());
    }

    //////////////////
    // Get Pulsar reader related config
    public boolean hasReaderConfKey(String key) {
        if (key.contains(READER_CONF_PREFIX))
            return readerConfMapRaw.containsKey(key.substring(READER_CONF_PREFIX.length() + 1));
        else
            return readerConfMapRaw.containsKey(key);
    }
    public Object getReaderConfValue(String key) {
        if (key.contains(READER_CONF_PREFIX))
            return readerConfMapRaw.get(key.substring(READER_CONF_PREFIX.length() + 1));
        else
            return readerConfMapRaw.get(key);
    }
    public void setReaderConfValue(String key, String value) {
        if (key.contains(READER_CONF_PREFIX))
            readerConfMapRaw.put(key.substring(READER_CONF_PREFIX.length() + 1), value);
        else
            readerConfMapRaw.put(key, value);
    }
    // Other reader helper functions ...
    public String getReaderTopicName() {
        Object confValue = getReaderConfValue(
            "reader." + PulsarAdapterUtil.READER_CONF_STD_KEY.topicName.label);
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }
    public String getReaderName() {
        Object confValue = getReaderConfValue(
            "reader." + PulsarAdapterUtil.READER_CONF_STD_KEY.readerName.label);
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }
    // NOTE: Below are not a standard Pulsar reader configuration parameter as
    //          listed in "https://pulsar.apache.org/docs/en/client-libraries-java/#reader"
    //       They're custom-made configuration properties for NB pulsar driver reader.
    public String getStartMsgPosStr() {
        Object confValue = getReaderConfValue(
            "reader." + PulsarAdapterUtil.READER_CONF_CUSTOM_KEY.startMessagePos.label);
        if (confValue == null)
            return "";
        else
            return confValue.toString();
    }
}
