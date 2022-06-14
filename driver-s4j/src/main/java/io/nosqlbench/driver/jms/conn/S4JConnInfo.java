package io.nosqlbench.driver.jms.conn;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.driver.jms.util.S4JConf;
import org.apache.commons.lang3.StringUtils;

import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

public class S4JConnInfo {

    private final String webSvcUrl;
    private final String pulsarSvcUrl;
    private final int sessionMode;
    private final Map<String, Object> s4jConfMap = new HashMap<>();
    private final S4JConf inputS4JConf;

    public S4JConnInfo(String webSvcUrl, String pulsarSvcUrl, String sessionModeStr, S4JConf s4JConf) {
        this.webSvcUrl = webSvcUrl;
        this.pulsarSvcUrl = pulsarSvcUrl;
        this.sessionMode = getSessionModeFromStr(sessionModeStr);
        this.inputS4JConf = s4JConf;

        this.s4jConfMap.put("webServiceUrl", this.webSvcUrl);
        this.s4jConfMap.put("brokerServiceUrl", this.pulsarSvcUrl);

        Map<String, Object> clientCfgMap = this.inputS4JConf.getClientConfMap();
        if (!clientCfgMap.isEmpty()) {
            this.s4jConfMap.putAll(clientCfgMap);
        }

        Map<String, Object> producerCfgMap = this.inputS4JConf.getProducerConfMap();
        if (!producerCfgMap.isEmpty()) {
            this.s4jConfMap.put("producerConfig", producerCfgMap);
        }

        Map<String, Object> consumerCfgMap = this.inputS4JConf.getConsumerConfMap();
        if (!consumerCfgMap.isEmpty()) {
            this.s4jConfMap.put("consumerConfig", consumerCfgMap);
        }

        Map<String, Object> jmsCfgMap = this.inputS4JConf.getJmsConfMap();
        if (!jmsCfgMap.isEmpty()) {
            this.s4jConfMap.putAll(jmsCfgMap);
        }
    }

    private int getSessionModeFromStr(String sessionModeStr) {
        int sessionMode = -1;

        if (StringUtils.equalsIgnoreCase(sessionModeStr, S4JActivityUtil.JMS_SESSION_MODES.AUTO_ACK.label))
            sessionMode = Session.AUTO_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, S4JActivityUtil.JMS_SESSION_MODES.AUTO_ACK.label))
            sessionMode = Session.CLIENT_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, S4JActivityUtil.JMS_SESSION_MODES.DUPS_OK_ACK.label))
            sessionMode = Session.DUPS_OK_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, S4JActivityUtil.JMS_SESSION_MODES.TRANSACT.label))
            sessionMode = Session.SESSION_TRANSACTED;

        return sessionMode;
    }

    public String getWebSvcUrl() { return this.webSvcUrl; }
    public String getPulsarSvcUrl() { return this.pulsarSvcUrl; }
    public int getSessionMode() { return this.sessionMode; }
    public S4JConf getInputS4JConf() { return this.inputS4JConf; }
    public Map<String, Object> getS4jConfMap() { return this.s4jConfMap; }
}
