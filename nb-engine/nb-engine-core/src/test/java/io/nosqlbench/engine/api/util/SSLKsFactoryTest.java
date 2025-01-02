/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.util;

import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.nb.api.engine.util.SSLKsFactory;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SSLKsFactoryTest {
    @Test
    public void testJdkGetContext() {
        NBConfiguration sslCfg = sslCfg("ssl=jdk", "tlsversion=TLSv1.2");
        assertThat(SSLKsFactory.get().getContext(sslCfg)).isNotNull();
    }

    private NBConfiguration sslCfg(String... params) {
        return SSLKsFactory.get().getConfigModel()
            .extractConfig(Activity.configFor(String.join(";",params)));
    }

    @Test
    public void testJdkGetContextWithTruststoreAndKeystore() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=jdk",
            "truststore=src/test/resources/ssl/server_truststore.p12",
            "tspass=nosqlbench_server",
            "keystore=src/test/resources/ssl/client.p12",
            "kspass=nosqlbench_client"
        );
        assertThat(SSLKsFactory.get().getContext(sslCfg)).isNotNull();
    }

    @Test
    public void testJdkGetContextWithTruststoreAndKeystoreAndDifferentKeyPassword() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=jdk",
            "truststore=src/test/resources/ssl/server_truststore.p12",
            "tspass=nosqlbench_server",
            "keystore=src/test/resources/ssl/client_diff_password.p12",
            "kspass=nosqlbench_client",
            "keyPassword=nosqlbench"
        );
        assertThat(SSLKsFactory.get().getContext(sslCfg)).isNotNull();
    }

    @Test
    public void testJdkGetContextWithTruststore() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=jdk",
            "truststore=src/test/resources/ssl/server_truststore.p12",
            "tspass=nosqlbench_server"
        );
        assertThat(SSLKsFactory.get().getContext(sslCfg)).isNotNull();
    }

    @Test
    public void testJdkGetContextWithKeystore() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=jdk", "keystore=src/test/resources/ssl/client.p12", "kspass=nosqlbench_client"
        );
        assertThat(SSLKsFactory.get().getContext(sslCfg)).isNotNull();
    }

    @Test
    public void testJdkGetContextWithKeystoreAndDifferentKeyPassword() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=jdk",
            "keystore=src/test/resources/ssl/client_diff_password.p12",
            "kspass=nosqlbench_client",
            "keyPassword=nosqlbench"
        );
        assertThat(SSLKsFactory.get().getContext(sslCfg)).isNotNull();
    }

    @Test
    public void testOpenSSLGetContext() {
        NBConfiguration sslCfg = sslCfg("ssl=openssl", "tlsversion=TLSv1.2");
        assertThat(SSLKsFactory.get().getContext(sslCfg)).isNotNull();
    }

    @Test
    public void testOpenSSLGetContextWithCaCertAndCertAndKey() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=openssl",
            "caCertFilePath=src/test/resources/ssl/cacert.crt",
            "certFilePath=src/test/resources/ssl/client_cert.pem",
            "keyFilePath=src/test/resources/ssl/client.key"
        );
        assertThat(SSLKsFactory.get().getContext(sslCfg)).isNotNull();
    }

    @Test
    public void testOpenSSLGetContextWithCaCert() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=openssl", "caCertFilePath=src/test/resources/ssl/cacert.crt"
        );
        assertThat(SSLKsFactory.get().getContext(sslCfg)).isNotNull();
    }

    @Test
    public void testOpenSSLGetContextWithCertAndKey() {
        NBConfiguration sslCfg = sslCfg(

            "ssl=openssl",
            "certFilePath=src/test/resources/ssl/client_cert.pem",
            "keyFilePath=src/test/resources/ssl/client.key"
        );
        assertThat(SSLKsFactory.get().getContext(sslCfg)).isNotNull();
    }

    @Test
    public void testLoadKeystoreError() {
        NBConfiguration sslCfg = sslCfg(

            "ssl=jdk",
            "keystore=src/test/resources/ssl/non_existing.p12",
            "kspass=nosqlbench_client",
            "keyPassword=nosqlbench_client"
        );
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> SSLKsFactory.get().getContext(sslCfg))
            .withMessageMatching("Unable to load the keystore: .*");
    }

    @Test
    public void testInitKeyManagerFactoryError() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=jdk",
            "keystore=src/test/resources/ssl/client.p12",
            "kspass=nosqlbench_client",
            "keyPassword=incorrect_password"
        );
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> SSLKsFactory.get().getContext(sslCfg))
            .withMessageMatching("Unable to init KeyManagerFactory. Please check.*");
    }

    @Test
    public void testLoadTruststoreError() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=jdk",
            "truststore=src/test/resources/ssl/non_existing.p12",
            "tspass=nosqlbench_server"
        );
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> SSLKsFactory.get().getContext(sslCfg))
            .withMessageMatching("Unable to load the truststore: .*");
    }

    @Test
    void testSSLValidationActive() {
        {
            NBConfiguration sslCfg1 = sslCfg(
                "ssl=openssl",
                "certFilePath=src/test/resources/ssl/client_cert.pem",
                "keyFilePath=src/test/resources/ssl/client.key",
                "sslValidation=true"
            );
            assertThat(SSLKsFactory.get().getContext(sslCfg1)).isNotNull();
        }

        {
            NBConfiguration sslCfg2 = sslCfg(                "ssl=jdk",
                "keystore=src/test/resources/ssl/client_diff_password.p12",
                "kspass=nosqlbench_client",
                "keyPassword=nosqlbench",
                "sslValidation=true"
            );
            assertThat(SSLKsFactory.get().getContext(sslCfg2)).isNotNull();
        }
    }

    @Test
    void testSSLValidationNotActive() {
        {
            NBConfiguration sslCfg1 = sslCfg(
                "ssl=openssl",
                "certFilePath=src/test/resources/ssl/client_cert.pem",
                "keyFilePath=src/test/resources/ssl/client.key",
                "sslValidation=false"
            );
            assertThat(SSLKsFactory.get().getContext(sslCfg1)).isNotNull();
        }

        {
            NBConfiguration sslCfg2 = sslCfg(                "ssl=jdk",
                "keystore=src/test/resources/ssl/client_diff_password.p12",
                "kspass=nosqlbench_client",
                "keyPassword=nosqlbench",
                "sslValidation=false"
            );
            assertThat(SSLKsFactory.get().getContext(sslCfg2)).isNotNull();
        }
    }

    @Test
    public void testOpenSSLGetContextWithCaCertError() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=openssl", "caCertFilePath=src/test/resources/ssl/non_existing.pem"
        );
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> SSLKsFactory.get().getContext(sslCfg))
            .withMessageContaining("Unable to load caCert from")
            .withCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testOpenSSLGetContextWithCertError() {
        NBConfiguration sslCfg = sslCfg(
            "ssl=openssl", "certFilePath=src/test/resources/ssl/non_existing.pem"
        );
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> SSLKsFactory.get().getContext(sslCfg))
            .withMessageContaining("Unable to load cert from")
            .withCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testOpenSSLGetContextWithKeyError() {
        NBConfiguration sslCfg = sslCfg(

            "ssl=openssl", "keyFilePath=src/test/resources/ssl/non_existing.pem"
        );
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> SSLKsFactory.get().getContext(sslCfg))
            .withMessageContaining("Unable to load key from")
            .withCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testOpenSSLGetContextWithMissingCertError() {
        NBConfiguration sslCfg = sslCfg(

            "ssl=openssl",
            "caCertFilePath=src/test/resources/ssl/cacert.crt",
            "keyFilePath=src/test/resources/ssl/client.key"
        );
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> SSLKsFactory.get().getContext(sslCfg))
            .withMessageContaining("Unable to load key from")
            .withCauseInstanceOf(IllegalArgumentException.class);
    }

}
