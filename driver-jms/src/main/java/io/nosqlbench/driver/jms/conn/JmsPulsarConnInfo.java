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
