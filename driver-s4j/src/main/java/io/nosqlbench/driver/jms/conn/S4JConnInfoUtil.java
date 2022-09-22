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
import io.nosqlbench.driver.jms.util.RawS4JConfConverter;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.JMSContext;
import java.util.HashMap;
import java.util.Map;

public class S4JConnInfoUtil {
    private final static Logger logger = LogManager.getLogger(S4JConnInfoUtil.class);

    // Merge extra Pulsar client related config
    public static Map<String, Object> mergeExtraClientConfig(
        S4JConnInfo s4JConnInfo,
        Map<String, String> extraClientConfigRaw)
    {
        if ( (extraClientConfigRaw == null) || (extraClientConfigRaw.isEmpty()) ) {
            return s4JConnInfo.getS4jConfObjMap();
        }
        else {
            Map<String, Object> origClientConfigObjMap = s4JConnInfo.getS4jConfMapObj_client();
            Map<String, Object> extraClientConfigObjMap =
                RawS4JConfConverter.convertRawClientConf(extraClientConfigRaw);
            Map<String, Object> mergedClientConfigObjMap =
                mergeConfigObjMaps(origClientConfigObjMap, extraClientConfigObjMap);

            Map<String, Object> mergedS4JConfObjMap = s4JConnInfo.getS4jConfObjMap();
            mergedS4JConfObjMap.putAll(mergedClientConfigObjMap);

            return mergedS4JConfObjMap;
        }
    }

    // Merge extra producer related config
    public static Map<String, Object> mergeExtraProducerConfig(
        S4JConnInfo s4JConnInfo,
        Map<String, String> extraProducerConfigRaw)
    {
        if ( (extraProducerConfigRaw == null) || (extraProducerConfigRaw.isEmpty()) ) {
            return s4JConnInfo.getS4jConfObjMap();
        }
        else {
            Map<String, Object> origProducerConfigObjMap = s4JConnInfo.getS4jConfMapObj_producer();
            Map<String, Object> extraProducerConfigObjMap =
                RawS4JConfConverter.convertRawProducerConf(extraProducerConfigRaw);
            Map<String, Object> mergedProducerConfigObjMap =
                mergeConfigObjMaps(origProducerConfigObjMap, extraProducerConfigObjMap);

            Map<String, Object> mergedS4JConfObjMap = s4JConnInfo.getS4jConfObjMap();
            mergedS4JConfObjMap.put("producerConfig", mergedProducerConfigObjMap);

            return mergedS4JConfObjMap;
        }
    }

    // Merge extra consumer related config
    public static Map<String, Object> mergeExtraConsumerConfig(
        S4JConnInfo s4JConnInfo,
        Map<String, String> extraConsumerConfigRaw)
    {
            if ( (extraConsumerConfigRaw == null) || (extraConsumerConfigRaw.isEmpty()) ) {
                return s4JConnInfo.getS4jConfObjMap();
            }
            else {
                Map<String, Object> origConsumerConfigObjMap = s4JConnInfo.getS4jConfMapObj_consumer();
                Map<String, Object> extraConsumerConfigObjMap =
                    RawS4JConfConverter.convertRawConsumerConf(extraConsumerConfigRaw);
                Map<String, Object> mergedConsumerConfigObjMap =
                    mergeConfigObjMaps(origConsumerConfigObjMap, extraConsumerConfigObjMap);

                Map<String, Object> mergedS4JConfObjMap = s4JConnInfo.getS4jConfObjMap();
                mergedS4JConfObjMap.put("consumerConfig", mergedConsumerConfigObjMap);

                return mergedS4JConfObjMap;
            }
    }

    // Merge extra S4J JMS related config
    public static Map<String, Object> mergeExtraS4jJmsConfig(
        S4JConnInfo s4JConnInfo,
        Map<String, String> extraS4jJmsConfigRaw)
    {
        if ( (extraS4jJmsConfigRaw == null) || (extraS4jJmsConfigRaw.isEmpty()) ) {
            return s4JConnInfo.getS4jConfObjMap();
        }
        else {
            Map<String, Object> origS4jJmsConfigObjMap = s4JConnInfo.getS4jConfMapObj_jms();
            Map<String, Object> extraS4jJmsConfigObjMap =
                RawS4JConfConverter.convertRawJmsConf(extraS4jJmsConfigRaw);
            Map<String, Object> mergedJmsConfigObjMap =
                mergeConfigObjMaps(origS4jJmsConfigObjMap, extraS4jJmsConfigObjMap);

            Map<String, Object> mergedS4JConfObjMap = s4JConnInfo.getS4jConfObjMap();
            mergedS4JConfObjMap.putAll(mergedJmsConfigObjMap);

            return mergedS4JConfObjMap;
        }
    }

    // Merge extra S4J Misc related config
    public static Map<String, Object> mergeExtraS4jMiscConfig(
        S4JConnInfo s4JConnInfo,
        Map<String, String> extraS4jMiscConfigRaw)
    {
        if ( (extraS4jMiscConfigRaw == null) || (extraS4jMiscConfigRaw.isEmpty()) ) {
            return s4JConnInfo.getS4jConfObjMap();
        }
        else {
            Map<String, Object> origS4jMiscConfigObjMap = s4JConnInfo.getS4jConfMapObj_misc();
            Map<String, Object> extraS4jMiscConfigObjMap =
                RawS4JConfConverter.convertRawMiscConf(extraS4jMiscConfigRaw);
            Map<String, Object> mergedMiscConfigObjMap =
                mergeConfigObjMaps(origS4jMiscConfigObjMap, extraS4jMiscConfigObjMap);

            Map<String, Object> mergedS4JConfObjMap = s4JConnInfo.getS4jConfObjMap();
            mergedS4JConfObjMap.putAll(mergedMiscConfigObjMap);

            return mergedS4JConfObjMap;
        }
    }

    public static boolean isUseCredentialsEnabled(S4JConnInfo s4JConnInfo) {
        assert (s4JConnInfo != null);

        boolean enabled = false;
        Map<String, Object> s4jConfMap = s4JConnInfo.getS4jConfObjMap();

        if (s4jConfMap.containsKey("jms.useCredentialsFromCreateConnection")) {
            enabled = BooleanUtils.toBoolean(s4jConfMap.get("jms.useCredentialsFromCreateConnection").toString());
        }
        return enabled;
    }

    public static String getCredentialUserName(S4JConnInfo s4JConnInfo) {
        return "dummy";
    }

    public static String getCredentialPassword(S4JConnInfo s4JConnInfo) {
        Map<String, Object> s4jConfMap = s4JConnInfo.getS4jConfObjMap();
        if (s4jConfMap.containsKey("authParams"))
            return s4jConfMap.get("authParams").toString();
        else
            return "";
    }

    public static int getSessionModeFromStr(String sessionModeStr) {
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

    public static boolean validTransactSetting(S4JConnInfo s4JConnInfo ) {
        boolean validSetting = true;

        int jmsSessionMode = s4JConnInfo.getSessionMode();
        Map<String, Object> s4jConfMap = s4JConnInfo.getS4jConfObjMap();

        if (jmsSessionMode == JMSContext.SESSION_TRANSACTED) {
            Object enableTransactionObj = s4jConfMap.get("enableTransaction");
            Object jmsEmulateTransactionObj = s4jConfMap.get("jms.emulateTransactions");

            boolean enableTraction = false;
            boolean jmsTransactionEmulation = false;

            if (enableTransactionObj != null) {
                enableTraction = BooleanUtils.toBoolean(s4jConfMap.get("enableTransaction").toString());
            }

            if (jmsEmulateTransactionObj != null) {
                jmsTransactionEmulation = BooleanUtils.toBoolean(s4jConfMap.get("jms.emulateTransactions").toString());
            }

            validSetting =
                ( (!enableTraction && !jmsTransactionEmulation) ||
                    (enableTraction != jmsTransactionEmulation) ) ;
        }

        return validSetting;
    }

    ///////
    // Misc utility functions
    public static Map<String, Object> mergeConfigObjMaps(
        Map<String, Object> origConfigObjMap,
        Map<String, Object> extraConfigObjMap )
    {
        Map<String, Object> newConfigObjMap = new HashMap<>();

        // If there are the same settings in both "orig" and "extra" maps,
        // the one in the "extra" map will take over
        newConfigObjMap.putAll(origConfigObjMap);
        newConfigObjMap.putAll(extraConfigObjMap);

        return newConfigObjMap;
    }
}
