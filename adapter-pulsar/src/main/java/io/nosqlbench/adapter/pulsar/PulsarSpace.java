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
import io.nosqlbench.adapter.pulsar.util.PulsarNBClientConf;
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
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.schema.KeyValueEncodingType;

import java.util.Map;

public class PulsarSpace {

    private final static Logger logger = LogManager.getLogger(PulsarSpace.class);

    private final String name;
    private final NBConfiguration cfg;

    private final String pulsarSvcUrl;
    private final String webSvcUrl;

    private PulsarNBClientConf pulsarNBClientConf;
    private PulsarClient pulsarClient;
    private PulsarAdmin pulsarAdmin;
    private Schema<?> pulsarSchema;

    public PulsarSpace(String name, NBConfiguration cfg) {
        this.name = name;
        this.cfg = cfg;

        this.pulsarSvcUrl = cfg.get("service_url");
        this.webSvcUrl = cfg.get("web_url");

        this.pulsarNBClientConf = new PulsarNBClientConf(cfg.get("config"));

        initPulsarAdminAndClientObj();
        createPulsarSchemaFromConf();
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(PulsarSpace.class)
            .add(Param.defaultTo("service_url", "pulsar://localhost:6650")
                .setDescription("Pulsar broker service URL."))
            .add(Param.defaultTo("web_url", "http://localhost:8080")
                .setDescription("Pulsar web service URL."))
            .add(Param.defaultTo("config", "config.properties")
                .setDescription("Pulsar client connection configuration property file."))
            .add(Param.defaultTo("cyclerate_per_thread", false)
                .setDescription("Apply cycle rate per NB thread"))
            .asReadOnly();
    }

    public String getPulsarSvcUrl() { return pulsarSvcUrl; }
    public String getWebSvcUrl() { return webSvcUrl; }
    public PulsarNBClientConf getPulsarNBClientConf() { return pulsarNBClientConf; }
    public PulsarClient getPulsarClient() { return pulsarClient; }
    public PulsarAdmin getPulsarAdmin() { return pulsarAdmin; }
    public Schema<?> getPulsarSchema() { return pulsarSchema; }

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
            Map<String, Object> clientConfMap = pulsarNBClientConf.getClientConfMap();

            // Override "client.serviceUrl" setting in config.properties
            clientConfMap.remove("serviceUrl");
            clientBuilder.loadConf(clientConfMap).serviceUrl(pulsarSvcUrl);

            // Pulsar Authentication
            String authPluginClassName =
                (String) pulsarNBClientConf.getClientConfValue(PulsarAdapterUtil.CLNT_CONF_KEY.authPulginClassName.label);
            String authParams =
                (String) pulsarNBClientConf.getClientConfValue(PulsarAdapterUtil.CLNT_CONF_KEY.authParams.label);

            if ( !StringUtils.isAnyBlank(authPluginClassName, authParams) ) {
                adminBuilder.authentication(authPluginClassName, authParams);
                clientBuilder.authentication(authPluginClassName, authParams);
            }

            boolean useTls = StringUtils.contains(pulsarSvcUrl, "pulsar+ssl");
            if ( useTls ) {
                String tlsHostnameVerificationEnableStr =
                    (String) pulsarNBClientConf.getClientConfValue(PulsarAdapterUtil.CLNT_CONF_KEY.tlsHostnameVerificationEnable.label);
                boolean tlsHostnameVerificationEnable = BooleanUtils.toBoolean(tlsHostnameVerificationEnableStr);

                adminBuilder
                    .enableTlsHostnameVerification(tlsHostnameVerificationEnable);
                clientBuilder
                    .enableTlsHostnameVerification(tlsHostnameVerificationEnable);

                String tlsTrustCertsFilePath =
                    (String) pulsarNBClientConf.getClientConfValue(PulsarAdapterUtil.CLNT_CONF_KEY.tlsTrustCertsFilePath.label);
                if (!StringUtils.isBlank(tlsTrustCertsFilePath)) {
                    adminBuilder.tlsTrustCertsFilePath(tlsTrustCertsFilePath);
                    clientBuilder.tlsTrustCertsFilePath(tlsTrustCertsFilePath);
                }

                String tlsAllowInsecureConnectionStr =
                    (String) pulsarNBClientConf.getClientConfValue(PulsarAdapterUtil.CLNT_CONF_KEY.tlsAllowInsecureConnection.label);
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

    /**
     * Get Pulsar schema from the definition string
     */

    private Schema<?> buildSchemaFromDefinition(String schemaTypeConfEntry,
                                                String schemaDefinitionConfEntry) {
        Object value = pulsarNBClientConf.getSchemaConfValue(schemaTypeConfEntry);
        Object schemaDefinition = pulsarNBClientConf.getSchemaConfValue(schemaDefinitionConfEntry);
        String schemaType = (value != null) ? value.toString() : "";

        Schema<?> result;
        if (PulsarAdapterUtil.isAvroSchemaTypeStr(schemaType)) {
            String schemaDefStr = (schemaDefinition != null) ? schemaDefinition.toString() : "";
            result = PulsarAdapterUtil.getAvroSchema(schemaType, schemaDefStr);
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
        if (pulsarNBClientConf.hasSchemaConfKey("schema.key.type")) {
            Schema<?> pulsarKeySchema = buildSchemaFromDefinition("schema.key.type", "schema.key.definition");
            Object encodingType =  pulsarNBClientConf.getSchemaConfValue("schema.keyvalue.encodingtype");
            KeyValueEncodingType keyValueEncodingType = KeyValueEncodingType.SEPARATED;
            if (encodingType != null) {
                keyValueEncodingType = KeyValueEncodingType.valueOf(encodingType.toString());
            }
            pulsarSchema = Schema.KeyValue(pulsarKeySchema, pulsarSchema, keyValueEncodingType);
        }
    }
}


