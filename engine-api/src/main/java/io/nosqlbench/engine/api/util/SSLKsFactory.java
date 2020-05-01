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

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class SSLKsFactory {
    private final static Logger logger = LoggerFactory.getLogger(SSLKsFactory.class);

    private static SSLKsFactory instance = new SSLKsFactory();

    /**
     * Consider: https://gist.github.com/artem-smotrakov/bd14e4bde4d7238f7e5ab12c697a86a3
     */
    private SSLKsFactory() {
    }

    public static SSLKsFactory get() {
        return instance;
    }

    public ServerSocketFactory createSSLServerSocketFactory(ActivityDef def) {
        return ((SSLContext) getContext(def)).getServerSocketFactory();
    }

    public SocketFactory createSocketFactory(ActivityDef def) {
        return ((SSLContext) getContext(def)).getSocketFactory();
    }

    public Object getContext(ActivityDef def) {
        Optional<String> sslParam = def.getParams().getOptionalString("ssl");
        if (sslParam.isPresent()) {
            if (sslParam.get().equals("jdk") || sslParam.get().equals("true")) {
                if (sslParam.get().equals("true")) {
                    logger.warn("Please update your 'ssl=true' parameter to 'ssl=jdk'");
                }

                Optional<String> keystorePath = def.getParams().getOptionalString("keystore");
                Optional<String> keystorePass = def.getParams().getOptionalString("kspass");
                Optional<String> truststorePath = def.getParams().getOptionalString("truststore");
                Optional<String> truststorePass = def.getParams().getOptionalString("tspass");
                String tlsVersion = def.getParams().getOptionalString("tlsversion").orElse("TLSv1.2");

                if (keystorePath.isPresent() && keystorePass.isPresent() && truststorePath.isPresent() && truststorePass.isPresent()) {
                    try {
                        KeyStore ks = KeyStore.getInstance("JKS");
                        ks.load(new FileInputStream(keystorePath.get()), keystorePass.get().toCharArray());

                        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                        kmf.init(ks, keystorePass.get().toCharArray());

                        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        if (!truststorePath.get().isEmpty()) {
                            KeyStore ts = KeyStore.getInstance("JKS");
                            InputStream trustStore = new FileInputStream(truststorePath.get());

                            String truststorePassword = truststorePass.get();
                            ts.load(trustStore, truststorePassword.toCharArray());
                            tmf.init(ts);
                        } else {
                            tmf.init(ks);
                        }

                        SSLContext sc = SSLContext.getInstance(tlsVersion);
                        sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                        return sc;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                } else if (keystorePath.isEmpty() && keystorePass.isEmpty() && truststorePath.isPresent() && truststorePass.isPresent()) {
                    try {
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        KeyStore ts = KeyStore.getInstance("JKS");
                        InputStream trustStore = new FileInputStream(truststorePath.get());
                        String truststorePassword = truststorePass.get();
                        ts.load(trustStore, truststorePassword.toCharArray());
                        tmf.init(ts);
                        SSLContext sc = SSLContext.getInstance(tlsVersion);
                        sc.init(null, tmf.getTrustManagers(), null);
                        return sc;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new RuntimeException("SSL arguments are incorrectly configured. Please Check.");
                }


            } else if (sslParam.get().equals("openssl")) {

                logger.info("Cluster builder proceeding with SSL and Client Auth");
                String keyPassword = def.getParams().getOptionalString("keyPassword").orElse(null);
                String caCertFileLocation = def.getParams().getOptionalString("caCertFilePath").orElse(null);
                String certFileLocation = def.getParams().getOptionalString("certFilePath").orElse(null);
                String keyFileLocation = def.getParams().getOptionalString("keyFilePath").orElse(null);
                String truststorePath = def.getParams().getOptionalString("truststore").orElse(null);
                String truststorePass = def.getParams().getOptionalString("tspass").orElse(null);

                try {
                    KeyStore ks = KeyStore.getInstance("JKS", "SUN");
                    char[] pass = keyPassword==null? null : keyPassword.toCharArray();
                    ks.load(null, pass);

                    X509Certificate cert = (X509Certificate) CertificateFactory.
                            getInstance("X509").
                            generateCertificate(new FileInputStream(caCertFileLocation));

                    //set alias to cert
                    ks.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert);

                    TrustManagerFactory tMF = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    //String truststorePath = System.getProperty("javax.net.ssl.trustStore");

                    if (truststorePath != null && !truststorePath.isEmpty() && truststorePass != null) {
                        KeyStore ts = KeyStore.getInstance("JKS");
                        InputStream trustStore = new FileInputStream(truststorePath);
                        ts.load(trustStore, truststorePass.toCharArray());
                        tMF.init(ts);
                    } else {
                        tMF.init(ks);
                    }

                    SslContext sslContext = SslContextBuilder
                            .forClient()
                            /* configured with the TrustManagerFactory that has the cert from the ca.cert
                             * This tells the driver to trust the server during the SSL handshake */
                            .trustManager(tMF)
                            /* These are needed because the server is configured with require_client_auth
                             * In this case the client's public key must be in the truststore on each DSE
                             * server node and the CA configured */
                            .keyManager(new File(certFileLocation), new File(keyFileLocation))
                            .build();

                    return sslContext;

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
