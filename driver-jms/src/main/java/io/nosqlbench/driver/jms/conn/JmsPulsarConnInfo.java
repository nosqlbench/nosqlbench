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

package io.nosqlbench.driver.jms.conn;

import io.nosqlbench.driver.jms.util.PulsarConfig;

import java.util.Map;

public class JmsPulsarConnInfo extends JmsConnInfo {

    private final String webSvcUrl;
    private final String pulsarSvcUrl;
    private final PulsarConfig extraPulsarConfig;

    public JmsPulsarConnInfo(String jmsProviderType, String webSvcUrl, String pulsarSvcUrl, PulsarConfig pulsarConfig) {
        super(jmsProviderType);

        this.webSvcUrl = webSvcUrl;
        this.pulsarSvcUrl = pulsarSvcUrl;
        this.extraPulsarConfig = pulsarConfig;

        this.addJmsConnConfigItem("webServiceUrl", this.webSvcUrl);
        this.addJmsConnConfigItem("brokerServiceUrl", this.pulsarSvcUrl);

        Map<String, Object> clientCfgMap = this.extraPulsarConfig.getClientConfMap();
        if (!clientCfgMap.isEmpty()) {
            this.addJmsConnConfigItems(clientCfgMap);
        }

        Map<String, Object> producerCfgMap = this.extraPulsarConfig.getProducerConfMap();
        if (!producerCfgMap.isEmpty()) {
            this.addJmsConnConfigItem("producerConfig", producerCfgMap);
        }

        Map<String, Object> consumerCfgMap = this.extraPulsarConfig.getConsumerConfMap();
        if (!consumerCfgMap.isEmpty()) {
            this.addJmsConnConfigItem("consumerConfig", consumerCfgMap);
        }
    }

    public String getWebSvcUrl() { return this.webSvcUrl; }
    public String getPulsarSvcUrl() { return this.pulsarSvcUrl; }
    public PulsarConfig getExtraPulsarConfig() { return this.extraPulsarConfig; }
}
