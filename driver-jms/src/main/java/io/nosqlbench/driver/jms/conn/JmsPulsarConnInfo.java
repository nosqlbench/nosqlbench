package io.nosqlbench.driver.jms.conn;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;

public class JmsPulsarConnInfo extends JmsConnInfo {

    private String pulsarSvcUrl;
    private String webSvcUrl;

    public JmsPulsarConnInfo(String jmsProviderType, ActivityDef activityDef) {
        super(jmsProviderType);

        webSvcUrl =
            activityDef.getParams().getOptionalString("web_url").orElse("http://localhost:8080");
        pulsarSvcUrl =
            activityDef.getParams().getOptionalString("service_url").orElse("pulsar://localhost:6650");
    }

    public void setPulsarSvcUrl(String pulsarSvcUrl) { this.pulsarSvcUrl = pulsarSvcUrl; }
    public String getPulsarSvcUrl() { return this.pulsarSvcUrl; }

    public void setWebSvcUrl(String webSvcUrl) { this.webSvcUrl = webSvcUrl; }
    public String getWebSvcUrl() { return this.webSvcUrl; }
}
