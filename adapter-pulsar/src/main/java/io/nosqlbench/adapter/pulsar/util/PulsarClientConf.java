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
import org.apache.commons.lang3.math.NumberUtils;
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
        producerConfMapTgt.putAll(PulsarConfConverter.convertStdRawProducerConf(producerConfMapRaw));
        consumerConfMapTgt.putAll(PulsarConfConverter.convertStdRawConsumerConf(consumerConfMapRaw));
        // TODO: Reader API is not enabled at the moment. Revisit when needed
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
                    if (StringUtils.startsWith(confKey, PulsarAdapterUtil.CONF_GATEGORY.Schema.label)) {
                        schemaConfMapRaw.put(confKey.substring(PulsarAdapterUtil.CONF_GATEGORY.Schema.label.length() + 1), confVal);
                    }
                    // Get client connection specific configuration settings, removing "client." prefix
                    // <<< https://pulsar.apache.org/docs/reference-configuration/#client >>>
                    else if (StringUtils.startsWith(confKey, PulsarAdapterUtil.CONF_GATEGORY.Client.label)) {
                        clientConfMapRaw.put(confKey.substring(PulsarAdapterUtil.CONF_GATEGORY.Client.label.length() + 1), confVal);
                    }
                    // Get producer specific configuration settings, removing "producer." prefix
                    // <<< https://pulsar.apache.org/docs/client-libraries-java/#configure-producer >>>
                    else if (StringUtils.startsWith(confKey, PulsarAdapterUtil.CONF_GATEGORY.Producer.label)) {
                        producerConfMapRaw.put(confKey.substring(PulsarAdapterUtil.CONF_GATEGORY.Producer.label.length() + 1), confVal);
                    }
                    // Get consumer specific configuration settings, removing "consumer." prefix
                    // <<< https://pulsar.apache.org/docs/client-libraries-java/#configure-consumer >>>
                    else if (StringUtils.startsWith(confKey, PulsarAdapterUtil.CONF_GATEGORY.Consumer.label)) {
                        consumerConfMapRaw.put(confKey.substring(PulsarAdapterUtil.CONF_GATEGORY.Consumer.label.length() + 1), confVal);
                    }
                    // Get reader specific configuration settings, removing "reader." prefix
                    // <<< https://pulsar.apache.org/docs/2.10.x/client-libraries-java/#configure-reader >>>
                    else if (StringUtils.startsWith(confKey, PulsarAdapterUtil.CONF_GATEGORY.Reader.label)) {
                        readerConfMapRaw.put(confKey.substring(PulsarAdapterUtil.CONF_GATEGORY.Reader.label.length() + 1), confVal);
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
        if (key.contains(PulsarAdapterUtil.CONF_GATEGORY.Schema.label))
            return schemaConfMapRaw.containsKey(key.substring(PulsarAdapterUtil.CONF_GATEGORY.Schema.label.length() + 1));
        else
            return schemaConfMapRaw.containsKey(key);
    }
    public String getSchemaConfValueRaw(String key) {
        if (hasSchemaConfKey(key)) {
            if (key.contains(PulsarAdapterUtil.CONF_GATEGORY.Schema.label))
                return schemaConfMapRaw.get(key.substring(PulsarAdapterUtil.CONF_GATEGORY.Schema.label.length() + 1));
            else
                return schemaConfMapRaw.get(key);
        }
        else {
            return "";
        }
    }


    //////////////////
    // Get Pulsar client related config
    public boolean hasClientConfKey(String key) {
        if (key.contains(PulsarAdapterUtil.CONF_GATEGORY.Client.label))
            return clientConfMapRaw.containsKey(key.substring(PulsarAdapterUtil.CONF_GATEGORY.Client.label.length() + 1));
        else
            return clientConfMapRaw.containsKey(key);
    }
    public String getClientConfValueRaw(String key) {
        if (hasClientConfKey(key)) {
            if (key.contains(PulsarAdapterUtil.CONF_GATEGORY.Client.label))
                return clientConfMapRaw.get(key.substring(PulsarAdapterUtil.CONF_GATEGORY.Client.label.length() + 1));
            else
                return clientConfMapRaw.get(key);
        }
        else {
            return "";
        }
    }


    //////////////////
    // Get Pulsar producer related config
    public boolean hasProducerConfKey(String key) {
        if (key.contains(PulsarAdapterUtil.CONF_GATEGORY.Producer.label))
            return producerConfMapRaw.containsKey(key.substring(PulsarAdapterUtil.CONF_GATEGORY.Producer.label.length() + 1));
        else
            return producerConfMapRaw.containsKey(key);
    }
    public String getProducerConfValueRaw(String key) {
        if (hasProducerConfKey(key)) {
            if (key.contains(PulsarAdapterUtil.CONF_GATEGORY.Producer.label))
                return producerConfMapRaw.get(key.substring(PulsarAdapterUtil.CONF_GATEGORY.Producer.label.length()+1));
            else
                return producerConfMapRaw.get(key);
        }
        else {
            return "";
        }
    }


    //////////////////
    // Get Pulsar consumer related config
    public boolean hasConsumerConfKey(String key) {
        if (key.contains(PulsarAdapterUtil.CONF_GATEGORY.Consumer.label))
            return consumerConfMapRaw.containsKey(key.substring(PulsarAdapterUtil.CONF_GATEGORY.Consumer.label.length() + 1));
        else
            return consumerConfMapRaw.containsKey(key);
    }
    public String getConsumerConfValueRaw(String key) {
        if (hasConsumerConfKey(key)) {
            if (key.contains(PulsarAdapterUtil.CONF_GATEGORY.Consumer.label))
                return consumerConfMapRaw.get(key.substring(PulsarAdapterUtil.CONF_GATEGORY.Consumer.label.length() + 1));
            else
                return consumerConfMapRaw.get(key);
        }
        else {
            return "";
        }
    }
    // NOTE: Below are not a standard Pulsar consumer configuration parameter as
    //          listed in "https://pulsar.apache.org/docs/en/client-libraries-java/#configure-consumer"
    //       They're custom-made configuration properties for NB pulsar driver consumer.
    public int getConsumerTimeoutSeconds() {
        String confValue = getConsumerConfValueRaw(
            "consumer." + PulsarAdapterUtil.CONSUMER_CONF_CUSTOM_KEY.timeout.label);
        return NumberUtils.toInt(confValue, -1);
    }

    //////////////////
    // Get Pulsar reader related config
    public boolean hasReaderConfKey(String key) {
        if (key.contains(PulsarAdapterUtil.CONF_GATEGORY.Reader.label))
            return readerConfMapRaw.containsKey(key.substring(PulsarAdapterUtil.CONF_GATEGORY.Reader.label.length() + 1));
        else
            return readerConfMapRaw.containsKey(key);
    }
    public String getReaderConfValueRaw(String key) {
        if (hasReaderConfKey(key)) {
            if (key.contains(PulsarAdapterUtil.CONF_GATEGORY.Reader.label))
                return readerConfMapRaw.get(key.substring(PulsarAdapterUtil.CONF_GATEGORY.Reader.label.length() + 1));
            else
                return readerConfMapRaw.get(key);
        }
        else {
            return "";
        }
    }
}
