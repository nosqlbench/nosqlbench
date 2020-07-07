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

import java.io.FileNotFoundException;

import org.junit.Test;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SSLKsFactoryTest {
    @Test
    public void testJdkGetContext() {
        String[] params = {
                "ssl=jdk",
                "tlsversion=TLSv1.2",
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testJdkGetContextWithTruststoreAndKeystore() {
        String[] params = {
                "ssl=jdk",
                "truststore=src/test/resources/ssl/server_truststore.p12",
                "tspass=nosqlbench_server",
                "keystore=src/test/resources/ssl/client.p12",
                "kspass=nosqlbench_client",
                "keyPassword=nosqlbench_client"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testJdkGetContextWithTruststore() {
        String[] params = {
                "ssl=jdk",
                "truststore=src/test/resources/ssl/server_truststore.p12",
                "tspass=nosqlbench_server"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testJdkGetContextWithKeystore() {
        String[] params = {
                "ssl=jdk",
                "keystore=src/test/resources/ssl/client.p12",
                "kspass=nosqlbench_client",
                "keyPassword=nosqlbench_client"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testOpenSSLGetContext() {
        String[] params = {
                "ssl=openssl",
                "tlsversion=TLSv1.2",
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testOpenSSLGetContextWithCaCertAndCertAndKey() {
        String[] params = {
                "ssl=openssl",
                "caCertFilePath=src/test/resources/ssl/cacert.crt",
                "certFilePath=src/test/resources/ssl/client_cert.pem",
                "keyFilePath=src/test/resources/ssl/client.key"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testOpenSSLGetContextWithCaCert() {
        String[] params = {
                "ssl=openssl",
                "caCertFilePath=src/test/resources/ssl/cacert.crt"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testOpenSSLGetContextWithCertAndKey() {
        String[] params = {
                "ssl=openssl",
                "certFilePath=src/test/resources/ssl/client_cert.pem",
                "keyFilePath=src/test/resources/ssl/client.key"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThat(SSLKsFactory.get().getContext(activityDef)).isNotNull();
    }

    @Test
    public void testLoadKeystoreError() {
        String[] params = {
                "ssl=jdk",
                "keystore=src/test/resources/ssl/non_existing.p12",
                "kspass=nosqlbench_client",
                "keyPassword=nosqlbench_client"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> SSLKsFactory.get().getContext(activityDef))
                .withMessageMatching("Unable to load the keystore. Please check.");
    }

    @Test
    public void testInitKeyManagerFactoryError() {
        String[] params = {
                "ssl=jdk",
                "keystore=src/test/resources/ssl/client.p12",
                "kspass=nosqlbench_client",
                "keyPassword=incorrect_password"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> SSLKsFactory.get().getContext(activityDef))
                .withMessageMatching("Unable to init KeyManagerFactory. Please check.");
    }

    @Test
    public void testLoadTruststoreError() {
        String[] params = {
                "ssl=jdk",
                "truststore=src/test/resources/ssl/non_existing.p12",
                "tspass=nosqlbench_server"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> SSLKsFactory.get().getContext(activityDef))
                .withMessageMatching("Unable to load the truststore. Please check.");
    }

    @Test
    public void testOpenSSLGetContextWithCaCertError() {
        String[] params = {
                "ssl=openssl",
                "caCertFilePath=src/test/resources/ssl/non_existing.pem"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> SSLKsFactory.get().getContext(activityDef))
                .withMessageContaining("Unable to load caCert from")
                .withCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testOpenSSLGetContextWithCertError() {
        String[] params = {
                "ssl=openssl",
                "certFilePath=src/test/resources/ssl/non_existing.pem"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> SSLKsFactory.get().getContext(activityDef))
                .withMessageContaining("Unable to load cert from")
                .withCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testOpenSSLGetContextWithKeyError() {
        String[] params = {
                "ssl=openssl",
                "keyFilePath=src/test/resources/ssl/non_existing.pem"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> SSLKsFactory.get().getContext(activityDef))
                .withMessageContaining("Unable to load key from")
                .withCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testOpenSSLGetContextWithMissingCertError() {
        String[] params = {
                "ssl=openssl",
                "caCertFilePath=src/test/resources/ssl/cacert.crt",
                "keyFilePath=src/test/resources/ssl/client.key"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> SSLKsFactory.get().getContext(activityDef))
                .withMessageContaining("Unable to load key from")
                .withCauseInstanceOf(IllegalArgumentException.class);
    }
}
