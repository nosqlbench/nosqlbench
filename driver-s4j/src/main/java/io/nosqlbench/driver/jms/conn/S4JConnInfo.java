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

import com.datastax.oss.pulsar.jms.PulsarJMSConstants;
import io.nosqlbench.driver.jms.S4JActivity;
import io.nosqlbench.driver.jms.excption.S4JDriverParamException;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.driver.jms.util.S4JConf;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.JMSContext;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

public class S4JConnInfo {

    private final static Logger logger = LogManager.getLogger(S4JConnInfo.class);
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

        if (this.inputS4JConf != null) {
            Map<String, Object> s4jMiscConfMap = this.inputS4JConf.getS4jMiscConfMap();
            if (!s4jMiscConfMap.isEmpty()) {
                this.s4jConfMap.putAll(s4jMiscConfMap);
            }

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

            if (!validTransactSetting(sessionMode, s4jConfMap)) {
                String errMsg =
                    "S4J config parameter \"enableTransaction\" or \"jms.emulateTransactions\" needs to be set to \"true\" (BUT not both) " +
                        "in order to support JMS transaction - sessionModeStr:\"" + sessionModeStr + "\"(" + sessionMode + ")";
                throw new S4JDriverParamException(errMsg);
            }
        }
    }

    public boolean isUseCredentialsEnabled() {
        boolean enabled = false;
        if (s4jConfMap.containsKey("jms.useCredentialsFromCreateConnection")) {
            enabled = BooleanUtils.toBoolean(s4jConfMap.get("jms.useCredentialsFromCreateConnection").toString());
        }
        return enabled;
    }

    public String getCredentialUserName() {
        return "dummy";
    }

    public String getCredentialPassword() {
        if (s4jConfMap.containsKey("authParams"))
            return s4jConfMap.get("authParams").toString();
        else
            return "";
    }

    private int getSessionModeFromStr(String sessionModeStr) {
        // default ack mode: auto_ack
        int sessionMode = -1;

        if (StringUtils.isBlank(sessionModeStr))
            sessionMode = JMSContext.AUTO_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, S4JActivityUtil.JMS_SESSION_MODES.AUTO_ACK.label))
            sessionMode = JMSContext.AUTO_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, S4JActivityUtil.JMS_SESSION_MODES.CLIENT_ACK.label))
            sessionMode = JMSContext.CLIENT_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, S4JActivityUtil.JMS_SESSION_MODES.DUPS_OK_ACK.label))
            sessionMode = JMSContext.DUPS_OK_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, S4JActivityUtil.JMS_SESSION_MODES.TRANSACT.label))
            sessionMode = JMSContext.SESSION_TRANSACTED;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, S4JActivityUtil.JMS_SESSION_MODES.INDIVIDUAL_ACK.label))
            sessionMode = PulsarJMSConstants.INDIVIDUAL_ACKNOWLEDGE;
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid session mode string \"{}\". Valid values are: {}. Use the default \"auto_ack\" mode!"
                    ,sessionModeStr, S4JActivityUtil.getValidJmsSessionModeList());
                sessionMode = JMSContext.AUTO_ACKNOWLEDGE;
            }
        }

        return sessionMode;
    }

    private boolean validTransactSetting(int jmsSessionMode, Map<String, Object> s4jConfMap) {
        boolean validSetting = true;

        if (jmsSessionMode == JMSContext.SESSION_TRANSACTED) {
            boolean enableTraction = BooleanUtils.toBoolean(s4jConfMap.get("enableTransaction").toString());
            boolean jmsTransactionEmulation = BooleanUtils.toBoolean(s4jConfMap.get("jms.emulateTransactions").toString());

            validSetting = (enableTraction != jmsTransactionEmulation);
        }

        return validSetting;
    }

    public String getWebSvcUrl() { return this.webSvcUrl; }
    public String getPulsarSvcUrl() { return this.pulsarSvcUrl; }
    public int getSessionMode() { return this.sessionMode; }
    public S4JConf getInputS4JConf() { return this.inputS4JConf; }
    public Map<String, Object> getS4jConfMap() { return this.s4jConfMap; }

    public String toString() {
        return new ToStringBuilder(this).
            append("webSvcUrl", webSvcUrl).
            append("pulsarSvcUrl", pulsarSvcUrl).
            append("sessionMode", sessionMode).
            append("inputS4JConf", inputS4JConf).
            toString();
    }
}
