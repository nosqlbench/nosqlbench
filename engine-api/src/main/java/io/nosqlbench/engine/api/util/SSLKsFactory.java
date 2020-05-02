/*
 *
 *    Copyright 2016 jshook
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

import java.io.File;
import java.security.KeyStore;
import java.util.Optional;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;

public class SSLKsFactory {
    private final static Logger logger = LoggerFactory.getLogger(SSLKsFactory.class);

    private static final SSLKsFactory instance = new SSLKsFactory();

    /**
     * Consider: https://gist.github.com/artem-smotrakov/bd14e4bde4d7238f7e5ab12c697a86a3
     */
    private SSLKsFactory() {
    }

    public static SSLKsFactory get() {
        return instance;
    }

    public ServerSocketFactory createSSLServerSocketFactory(ActivityDef def) {
        SslContext context = getContext(def);
        if (context == null) {
            throw new IllegalArgumentException("SSL is not enabled.");
        }
        // FIXME: potential incompatibility issue
        return ((JdkSslContext) context).context().getServerSocketFactory();
    }

    public SocketFactory createSocketFactory(ActivityDef def) {
        SslContext context = getContext(def);
        if (context == null) {
            throw new IllegalArgumentException("SSL is not enabled.");
        }
        // FIXME: potential incompatibility issue
        return ((JdkSslContext) context).context().getSocketFactory();
    }

    public SslContext getContext(ActivityDef def) {
        Optional<String> sslParam = def.getParams().getOptionalString("ssl");
        if (sslParam.isPresent()) {
            String tlsVersion = def.getParams().getOptionalString("tlsversion").orElse("TLSv1.2");

            if (sslParam.get().equals("jdk") || sslParam.get().equals("true")) {
                if (sslParam.get().equals("true")) {
                    logger.warn("Please update your 'ssl=true' parameter to 'ssl=jdk'");
                }

                Optional<String> keystorePath = def.getParams().getOptionalString("keystore");
                Optional<String> keystorePass = def.getParams().getOptionalString("kspass");
                char[] keyPassword = def.getParams().getOptionalString("keyPassword")
                                        .map(String::toCharArray)
                                        .orElse(null);
                Optional<String> truststorePath = def.getParams().getOptionalString("truststore");
                Optional<String> truststorePass = def.getParams().getOptionalString("tspass");

                KeyStore ks = keystorePath.map(ksPath -> {
                    try {
                        return KeyStore.getInstance(new File(ksPath),
                                                    keystorePass.map(String::toCharArray).orElse(null));
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to load the keystore. Please check.", e);
                    }
                }).orElse(null);

                KeyManagerFactory kmf;
                try {
                    kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(ks, keyPassword);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to init KeyManagerFactory. Please check.", e);
                }

                KeyStore ts = truststorePath.map(tsPath -> {
                    try {
                        return KeyStore.getInstance(new File(tsPath),
                                                    truststorePass.map(String::toCharArray).orElse(null));
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to load the truststore. Please check.", e);
                    }
                }).orElse(null);

                TrustManagerFactory tmf;
                try {
                    tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(ts != null ? ts : ks);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to init TrustManagerFactory. Please check.", e);
                }

                try {
                    return SslContextBuilder.forClient()
                                            .protocols(tlsVersion)
                                            .trustManager(tmf)
                                            .keyManager(kmf)
                                            .build();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (sslParam.get().equals("openssl")) {
                File caCertFileLocation = def.getParams().getOptionalString("caCertFilePath").map(File::new).orElse(null);
                File certFileLocation = def.getParams().getOptionalString("certFilePath").map(File::new).orElse(null);
                File keyFileLocation = def.getParams().getOptionalString("keyFilePath").map(File::new).orElse(null);

                try {
                    return SslContextBuilder.forClient()
                                            .protocols(tlsVersion)
                                            /* configured with the TrustManagerFactory that has the cert from the ca.cert
                                             * This tells the driver to trust the server during the SSL handshake */
                                            .trustManager(caCertFileLocation)
                                            /* These are needed if the server is configured with require_client_auth
                                             * In this case the client's public key must be in the truststore on each DSE
                                             * server node and the CA configured */
                                            .keyManager(certFileLocation, keyFileLocation)
                                            .build();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("The 'ssl' parameter must have one of jdk, or openssl");
            }
        } else {
            return null;
        }
    }
}