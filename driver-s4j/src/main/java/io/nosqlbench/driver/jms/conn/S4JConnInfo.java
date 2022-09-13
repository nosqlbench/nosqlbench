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

import io.nosqlbench.driver.jms.excption.S4JDriverParamException;
import io.nosqlbench.driver.jms.util.RawS4JConfConverter;
import io.nosqlbench.driver.jms.util.S4JConfFromFile;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class S4JConnInfo {

    private final static Logger logger = LogManager.getLogger(S4JConnInfo.class);
    private final String webSvcUrl;
    private final String pulsarSvcUrl;
    private final int sessionMode;
    private final S4JConfFromFile s4JConfFromFile;
    private final Map<String, Object> s4jConfMapObj = new HashMap<>();

    private Map<String, Object> s4jConfMapObj_client = new HashMap<>();
    private Map<String, Object> s4jConfMapObj_producer = new HashMap<>();
    private Map<String, Object> s4jConfMapObj_consumer = new HashMap<>();
    private Map<String, Object> s4jConfMapObj_jms = new HashMap<>();
    private Map<String, Object> s4jConfMapObj_misc = new HashMap<>();


    public S4JConnInfo(String webSvcUrl, String pulsarSvcUrl, String sessionModeStr, S4JConfFromFile s4JConfFromFile) {
        this.webSvcUrl = webSvcUrl;
        this.pulsarSvcUrl = pulsarSvcUrl;
        this.sessionMode = S4JConnInfoUtil.getSessionModeFromStr(sessionModeStr);
        this.s4JConfFromFile = s4JConfFromFile;

        this.s4jConfMapObj.put("webServiceUrl", this.webSvcUrl);
        this.s4jConfMapObj.put("brokerServiceUrl", this.pulsarSvcUrl);

        if (this.s4JConfFromFile != null) {
            this.s4jConfMapObj_client =
                RawS4JConfConverter.convertRawClientConf(this.s4JConfFromFile.getClientConfMapRaw());
            this.s4jConfMapObj.putAll(this.s4jConfMapObj_client);

            this.s4jConfMapObj_producer =
                RawS4JConfConverter.convertRawProducerConf(this.s4JConfFromFile.getProducerConfMapRaw());
            this.s4jConfMapObj.put("producerConfig", s4jConfMapObj_producer);

            this.s4jConfMapObj_consumer =
                RawS4JConfConverter.convertRawConsumerConf(this.s4JConfFromFile.getConsumerConfMapRaw());
            this.s4jConfMapObj.put("consumerConfig", s4jConfMapObj_consumer);

            this.s4jConfMapObj_jms = RawS4JConfConverter.convertRawJmsConf(this.s4JConfFromFile.getJmsConfMapRaw());
            this.s4jConfMapObj.putAll(s4jConfMapObj_jms);

            this.s4jConfMapObj_misc = RawS4JConfConverter.convertRawMiscConf(this.s4JConfFromFile.getS4jMiscConfMapRaw());
            this.s4jConfMapObj.putAll(s4jConfMapObj_misc);

            if (!S4JConnInfoUtil.validTransactSetting(this)) {
                String errMsg =
                    "S4J config parameter \"enableTransaction\" and \"jms.emulateTransactions\" can't be both set to \"true\") " +
                        "in order to support JMS transaction - sessionModeStr:\"" + sessionModeStr + "\"(" + sessionMode + ")";
                throw new S4JDriverParamException(errMsg);
            }
        }
    }

    public String getWebSvcUrl() { return this.webSvcUrl; }
    public String getPulsarSvcUrl() { return this.pulsarSvcUrl; }
    public int getSessionMode() { return this.sessionMode; }
    public S4JConfFromFile getS4JConfFromFile() { return this.s4JConfFromFile; }
    public Map<String, Object> getS4jConfObjMap() { return this.s4jConfMapObj; }
    public Map<String, Object> getS4jConfMapObj_client() { return this.s4jConfMapObj_client; }
    public Map<String, Object> getS4jConfMapObj_producer() { return this.s4jConfMapObj_producer; }
    public Map<String, Object> getS4jConfMapObj_consumer() { return this.s4jConfMapObj_consumer; }
    public Map<String, Object> getS4jConfMapObj_jms() { return this.s4jConfMapObj_jms; }
    public Map<String, Object> getS4jConfMapObj_misc() { return this.s4jConfMapObj_misc; }


    public String toString() {
        return new ToStringBuilder(this).
            append("webSvcUrl", webSvcUrl).
            append("pulsarSvcUrl", pulsarSvcUrl).
            append("sessionMode", sessionMode).
            append("s4JConfFromFile", s4JConfFromFile).
            append("effectiveS4jConfMap", s4jConfMapObj).
            toString();
    }
}
