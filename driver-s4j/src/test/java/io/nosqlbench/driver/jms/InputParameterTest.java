package io.nosqlbench.driver.jms;

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

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import io.nosqlbench.driver.jms.conn.S4JConnInfo;
import io.nosqlbench.driver.jms.util.S4JConf;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import javax.jms.JMSContext;
import java.io.File;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class InputParameterTest {

    @Test
    public void defaultSessionMode() {
        String sessionModeStr = "";
        S4JConnInfo s4JConnInfo = new S4JConnInfo("dummy_webSvcUrl", "dummy_pulsarSvcUrl", sessionModeStr, null);
        assert (s4JConnInfo.getSessionMode() == JMSContext.AUTO_ACKNOWLEDGE);
    }

    @Test
    public void useCredentialsFromCreateConnectionParam() {
        String fileName = "test/s4j_config_usecred.properties";
        URL fileUrl = InputParameterTest.class.getClassLoader().getResource(fileName);
        if (fileUrl != null) {
            File inputCfgPropFile = new File(fileUrl.getPath());

            if (inputCfgPropFile.exists()) {
                S4JConf s4JConf = new S4JConf(fileName);
                String webSvcUrl = s4JConf.getClientConfMap().get("webServiceUrl").toString();
                String brkrSvcUrl = s4JConf.getClientConfMap().get("brokerServiceUrl").toString();
                S4JConnInfo s4JConnInfo = new S4JConnInfo(webSvcUrl, brkrSvcUrl, "", s4JConf);
                Map<String, Object> connInfoS4jConfMap = s4JConnInfo.getS4jConfMap();
                String userName = "dummy";
                String passWord = s4JConnInfo.getCredentialPassword();

                // Only applies when authParams starts with "token:"
                if (StringUtils.startsWith(passWord, "token:")) {

                    // Use regular Pulsar approach to establish the connection
                    connInfoS4jConfMap.put("jms.useCredentialsFromCreateConnection", false);
                    boolean conn1Succes = false;
                    try {
                        PulsarConnectionFactory pulsarConnectionFactory1 = new PulsarConnectionFactory(connInfoS4jConfMap);
                        JMSContext jmsContext1 = pulsarConnectionFactory1.createContext();
                        jmsContext1.createProducer().send(jmsContext1.createQueue("testQueue"), "foo1");
                        conn1Succes = true;
                    } catch (Exception ok) {
                    }

                    // Use the simulated username/password approach to establish the connection
                    connInfoS4jConfMap.put("jms.useCredentialsFromCreateConnection", true);
                    boolean conn2Succes = false;
                    try {

                        connInfoS4jConfMap.remove("authParams");
                        PulsarConnectionFactory pulsarConnectionFactory2 = new PulsarConnectionFactory(connInfoS4jConfMap);
                        JMSContext jmsContext2 = pulsarConnectionFactory2.createContext(userName, passWord);
                        jmsContext2.createProducer().send(jmsContext2.createQueue("testQueue"), "foo2");
                        conn2Succes = true;
                    } catch (Exception ok) {
                    }

                    if (conn1Succes != conn2Succes) {
                        fail();
                    }
                    else {
                        System.out.println("NB S4J test 'useCredentialsFromCreateConnectionParam' succeeded (" + conn1Succes + ")");
                    }
                }
                else {
                    System.out.println("NB S4J test 'useCredentialsFromCreateConnectionParam' ignored - " +
                        "'client.authParams' must follows format 'token:<token_value_...>.");
                }
            }
            else {
                System.out.println("NB S4J test 'useCredentialsFromCreateConnectionParam' ignored - " +
                    "testing resource file 'resources/test/s4j_config_usecred.properties' doesn't exist.");
            }
        }
        else {
            System.out.println("NB S4J test 'useCredentialsFromCreateConnectionParam' ignored - " +
                "testing resource file 'resources/test/s4j_config_usecred.properties' doesn't exist.");
        }
    }
}
