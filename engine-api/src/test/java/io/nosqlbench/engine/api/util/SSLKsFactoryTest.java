/*
 *
 *    Copyright 2020 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.util;

import org.junit.Test;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;

import static org.assertj.core.api.Assertions.assertThat;

public class SSLKsFactoryTest
{
    @Test
    public void testJdkGetContextWithTruststoreAndKeystore() {
        String[] params = {
                "ssl=jdk",
                "truststore=src/test/resources/ssl/truststore.jks",
                "tspass=cassandra",
                "keystore=src/test/resources/ssl/keystore.jks",
                "kspass=cassandra",
                "keyPassword=cassandra"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testJdkGetContextWithTruststore() {
        String[] params = {
                "ssl=jdk",
                "truststore=src/test/resources/ssl/truststore.jks",
                "tspass=cassandra"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testOpenSSLGetContextWithCaCertAndClientCert() {
        String[] params = {
                "ssl=openssl",
                "caCertFilePath=src/test/resources/ssl/cassandra.pem",
                "certFilePath=src/test/resources/ssl/client_cert.pem",
                "keyFilePath=src/test/resources/ssl/client_key.pem"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testOpenSSLGetContextWithCaCert() {
        String[] params = {
                "ssl=openssl",
                "caCertFilePath=src/test/resources/ssl/cassandra.pem"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testGetContext()
    {
        String[] params = {
                "ssl=jdk",
                "tlsversion=TLSv1.2",
                "truststore=src/test/resources/ssl/truststore.jks",
                "tspass=cassandra"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }
}