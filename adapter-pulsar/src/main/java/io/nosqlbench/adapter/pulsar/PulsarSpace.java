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

package io.nosqlbench.adapter.pulsar;

import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil;
import io.nosqlbench.adapter.pulsar.util.PulsarClientConf;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminBuilder;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.common.schema.KeyValueEncodingType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class PulsarSpace implements  AutoCloseable {

    private final static Logger logger = LogManager.getLogger(PulsarSpace.class);

    private final String spaceName;
    private final NBConfiguration cfg;

    private final String pulsarSvcUrl;
    private final String webSvcUrl;

    private PulsarClientConf pulsarClientConf;
    private PulsarClient pulsarClient;
    private PulsarAdmin pulsarAdmin;
    private Schema<?> pulsarSchema;

    public record ProducerCacheKey(String producerName, String topicName) {
    }
    private final ConcurrentHashMap<ProducerCacheKey, Producer<?>> producers = new ConcurrentHashMap<>();

    public record ConsumerCacheKey(String consumerName,
                                   String subscriptionName,
                                   List<String> topicNameList,
                                   String topicPattern) {
    }
    private final ConcurrentHashMap<ConsumerCacheKey, Consumer<?>> consumers = new ConcurrentHashMap<>();

    public record ReaderCacheKey(String readerName, String topicName, String startMsgPosStr) {
    }
    private final ConcurrentHashMap<ReaderCacheKey, Reader<?>> readers = new ConcurrentHashMap<>();


    public PulsarSpace(String spaceName, NBConfiguration cfg) {
        this.spaceName = spaceName;
        this.cfg = cfg;

        this.pulsarSvcUrl = cfg.get("service_url");
        this.webSvcUrl = cfg.get("web_url");
        this.pulsarClientConf = new PulsarClientConf(cfg.get("config"));

        initPulsarAdminAndClientObj();
        createPulsarSchemaFromConf();
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(PulsarSpace.class)
            .add(Param.defaultTo("service_url", "pulsar://localhost:6650")
                .setDescription("Pulsar broker service URL."))
            .add(Param.defaultTo("web_url", "http://localhost:8080")
                .setDescription("Pulsar web service URL."))
            .add(Param.defaultTo("config", "conf/pulsar_config.properties")
                .setDescription("Pulsar client connection configuration property file."))
            .add(Param.defaultTo("cyclerate_per_thread", false)
                .setDescription("Apply cycle rate per NB thread"))
            .asReadOnly();
    }

    public String getPulsarSvcUrl() { return pulsarSvcUrl; }
    public String getWebSvcUrl() { return webSvcUrl; }
    public PulsarClientConf getPulsarNBClientConf() { return pulsarClientConf; }
    public PulsarClient getPulsarClient() { return pulsarClient; }
    public PulsarAdmin getPulsarAdmin() { return pulsarAdmin; }
    public Schema<?> getPulsarSchema() { return pulsarSchema; }
    public int getProducerSetCnt() { return producers.size(); }
    public int getConsumerSetCnt() { return consumers.size(); }
    public int getReaderSetCnt() { return readers.size(); }

    public Producer<?> getProducer(ProducerCacheKey key, Supplier<Producer<?>> producerSupplier) {
        return producers.computeIfAbsent(key, __ -> producerSupplier.get());
    }

    public Consumer<?> getConsumer(ConsumerCacheKey key, Supplier<Consumer<?>> consumerSupplier) {
        return consumers.computeIfAbsent(key, __ -> consumerSupplier.get());
    }

    public Reader<?> getReader(ReaderCacheKey key, Supplier<Reader<?>> readerSupplier) {
        return readers.computeIfAbsent(key, __ -> readerSupplier.get());
    }


    /**
     * Initialize
     * - PulsarAdmin object for adding/deleting tenant, namespace, and topic
     * - PulsarClient object for message publishing and consuming
     */
    private void initPulsarAdminAndClientObj() {
        PulsarAdminBuilder adminBuilder =
            PulsarAdmin.builder()
                .serviceHttpUrl(webSvcUrl);

        ClientBuilder clientBuilder = PulsarClient.builder();

        try {
            Map clientConfMap = pulsarClientConf.getClientConfMapRaw();

            // Override "client.serviceUrl" setting in pulsar_config.properties
            clientConfMap.remove("serviceUrl");
            clientBuilder.loadConf(clientConfMap).serviceUrl(pulsarSvcUrl);

            // Pulsar Authentication
            String authPluginClassName =
                pulsarClientConf.getClientConfValueRaw(PulsarAdapterUtil.CLNT_CONF_KEY.authPulginClassName.label);
            String authParams =
                pulsarClientConf.getClientConfValueRaw(PulsarAdapterUtil.CLNT_CONF_KEY.authParams.label);

            if ( !StringUtils.isAnyBlank(authPluginClassName, authParams) ) {
                adminBuilder.authentication(authPluginClassName, authParams);
                clientBuilder.authentication(authPluginClassName, authParams);
            }

            boolean useTls = StringUtils.contains(pulsarSvcUrl, "pulsar+ssl");
            if ( useTls ) {
                String tlsHostnameVerificationEnableStr =
                    pulsarClientConf.getClientConfValueRaw(PulsarAdapterUtil.CLNT_CONF_KEY.tlsHostnameVerificationEnable.label);
                boolean tlsHostnameVerificationEnable = BooleanUtils.toBoolean(tlsHostnameVerificationEnableStr);

                adminBuilder
                    .enableTlsHostnameVerification(tlsHostnameVerificationEnable);
                clientBuilder
                    .enableTlsHostnameVerification(tlsHostnameVerificationEnable);

                String tlsTrustCertsFilePath =
                    pulsarClientConf.getClientConfValueRaw(PulsarAdapterUtil.CLNT_CONF_KEY.tlsTrustCertsFilePath.label);
                if (!StringUtils.isBlank(tlsTrustCertsFilePath)) {
                    adminBuilder.tlsTrustCertsFilePath(tlsTrustCertsFilePath);
                    clientBuilder.tlsTrustCertsFilePath(tlsTrustCertsFilePath);
                }

                String tlsAllowInsecureConnectionStr =
                    pulsarClientConf.getClientConfValueRaw(PulsarAdapterUtil.CLNT_CONF_KEY.tlsAllowInsecureConnection.label);
                boolean tlsAllowInsecureConnection = BooleanUtils.toBoolean(tlsAllowInsecureConnectionStr);
                adminBuilder.allowTlsInsecureConnection(tlsAllowInsecureConnection);
                clientBuilder.allowTlsInsecureConnection(tlsAllowInsecureConnection);
            }

            pulsarAdmin = adminBuilder.build();
            pulsarClient = clientBuilder.build();

        } catch (PulsarClientException e) {
            logger.error("Fail to create PulsarAdmin and/or PulsarClient object from the global configuration!");
            throw new RuntimeException("Fail to create PulsarAdmin and/or PulsarClient object from global configuration!");
        }
    }

    public void shutdownSpace() {
        try {
            for (Producer<?> producer : producers.values()) {
                if (producer != null) producer.close();
            }
            for (Consumer<?> consumer : consumers.values()) {
                if (consumer != null) consumer.close();
            }
            for (Reader<?> reader : readers.values()) {
                if (reader != null) reader.close();
            }
            if (pulsarAdmin != null) pulsarAdmin.close();
            if (pulsarClient != null) pulsarClient.close();
        }
        catch (Exception ex) {
            String exp = "Unexpected error when shutting down the Pulsar adaptor space";
            logger.error(exp, ex);
        }
    }

    /**
     * Get Pulsar schema from the definition string
     */

    private Schema<?> buildSchemaFromDefinition(String schemaTypeConfEntry,
                                                String schemaDefinitionConfEntry) {
        String schemaType = pulsarClientConf.getSchemaConfValueRaw(schemaTypeConfEntry);
        String schemaDef = pulsarClientConf.getSchemaConfValueRaw(schemaDefinitionConfEntry);

        Schema<?> result;
        if (PulsarAdapterUtil.isAvroSchemaTypeStr(schemaType)) {
            result = PulsarAdapterUtil.getAvroSchema(schemaType, schemaDef);
        } else if (PulsarAdapterUtil.isPrimitiveSchemaTypeStr(schemaType)) {
            result = PulsarAdapterUtil.getPrimitiveTypeSchema(schemaType);
        } else if (PulsarAdapterUtil.isAutoConsumeSchemaTypeStr(schemaType)) {
            result = Schema.AUTO_CONSUME();
        } else {
            throw new RuntimeException("Unsupported schema type string: " + schemaType + "; " +
                "Only primitive type, Avro type and AUTO_CONSUME are supported at the moment!");
        }
        return result;
    }
    private void createPulsarSchemaFromConf() {
        pulsarSchema = buildSchemaFromDefinition("schema.type", "schema.definition");

        // this is to allow KEY_VALUE schema
        if (pulsarClientConf.hasSchemaConfKey("schema.key.type")) {
            Schema<?> pulsarKeySchema = buildSchemaFromDefinition("schema.key.type", "schema.key.definition");
            KeyValueEncodingType keyValueEncodingType = KeyValueEncodingType.SEPARATED;

            String encodingType = pulsarClientConf.getSchemaConfValueRaw("schema.keyvalue.encodingtype");
            if (StringUtils.isNotBlank(encodingType)) {
                keyValueEncodingType = KeyValueEncodingType.valueOf(encodingType);
            }

            pulsarSchema = Schema.KeyValue(pulsarKeySchema, pulsarSchema, keyValueEncodingType);
        }
    }

    @Override
    public void close() {
        shutdownSpace();
    }
}


