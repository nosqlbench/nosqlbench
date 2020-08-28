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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

public class SSLKsFactory {
    private final static Logger logger = LoggerFactory.getLogger(SSLKsFactory.class);

    private static final SSLKsFactory instance = new SSLKsFactory();

    private static final Pattern CERT_PATTERN = Pattern.compile("-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*CERTIFICATE[^-]*-+", 2);
    private static final Pattern KEY_PATTERN = Pattern.compile("-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", 2);

    /**
     * Consider: https://gist.github.com/artem-smotrakov/bd14e4bde4d7238f7e5ab12c697a86a3
     */
    private SSLKsFactory() {
    }

    public static SSLKsFactory get() {
        return instance;
    }

    public ServerSocketFactory createSSLServerSocketFactory(ActivityDef def) {
        SSLContext context = getContext(def);
        if (context == null) {
            throw new IllegalArgumentException("SSL is not enabled.");
        }
        return context.getServerSocketFactory();
    }

    public SocketFactory createSocketFactory(ActivityDef def) {
        SSLContext context = getContext(def);
        if (context == null) {
            throw new IllegalArgumentException("SSL is not enabled.");
        }
        return context.getSocketFactory();
    }

    public SSLContext getContext(ActivityDef def) {
        Optional<String> sslParam = def.getParams().getOptionalString("ssl");
        if (sslParam.isPresent()) {
            String tlsVersion = def.getParams().getOptionalString("tlsversion").orElse("TLSv1.2");

            KeyStore keyStore;
            char[] keyPassword = null;
            KeyStore trustStore;

            if (sslParam.get().equals("jdk") || sslParam.get().equals("true")) {
                if (sslParam.get().equals("true")) {
                    logger.warn("Please update your 'ssl=true' parameter to 'ssl=jdk'");
                }

                keyPassword = def.getParams().getOptionalString("keyPassword")
                                 .map(String::toCharArray)
                                 .orElse(null);

                keyStore = def.getParams().getOptionalString("keystore").map(ksPath -> {
                    try {
                        return KeyStore.getInstance(new File(ksPath),
                                                    def.getParams().getOptionalString("kspass")
                                                       .map(String::toCharArray)
                                                       .orElse(null));
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to load the keystore. Please check.", e);
                    }
                }).orElse(null);

                trustStore = def.getParams().getOptionalString("truststore").map(tsPath -> {
                    try {
                        return KeyStore.getInstance(new File(tsPath),
                                                    def.getParams().getOptionalString("tspass")
                                                       .map(String::toCharArray)
                                                       .orElse(null));
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to load the truststore. Please check.", e);
                    }
                }).orElse(null);

            } else if (sslParam.get().equals("openssl")) {
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");

                    keyStore = KeyStore.getInstance("JKS");
                    keyStore.load(null, null);

                    Certificate cert = def.getParams().getOptionalString("certFilePath").map(certFilePath -> {
                        try (InputStream is = new ByteArrayInputStream(loadCertFromPem(new File(certFilePath)))) {
                            return cf.generateCertificate(is);
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Unable to load cert from %s. Please check.",
                                                                     certFilePath),
                                                       e);
                        }
                    }).orElse(null);

                    if (cert != null)
                        keyStore.setCertificateEntry("certFile", cert);

                    File keyFile = def.getParams().getOptionalString("keyFilePath").map(File::new)
                                      .orElse(null);
                    if (keyFile != null) {
                        try {
                            keyPassword = def.getParams().getOptionalString("keyPassword")
                                             .map(String::toCharArray)
                                             .orElse("temp_key_password".toCharArray());

                            KeyFactory kf = KeyFactory.getInstance("RSA");
                            PrivateKey key = kf.generatePrivate(new PKCS8EncodedKeySpec(loadKeyFromPem(keyFile)));
                            keyStore.setKeyEntry("key", key, keyPassword,
                                                 cert != null ? new Certificate[]{ cert } : null);
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Unable to load key from %s. Please check.",
                                                                     keyFile),
                                                       e);
                        }
                    }

                    trustStore = def.getParams().getOptionalString("caCertFilePath").map(caCertFilePath -> {
                        try (InputStream is = new FileInputStream(new File(caCertFilePath))) {
                            KeyStore ts = KeyStore.getInstance("JKS");
                            ts.load(null, null);

                            Certificate caCert = cf.generateCertificate(is);
                            ts.setCertificateEntry("caCertFile", caCert);
                            return ts;
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Unable to load caCert from %s. Please check.",
                                                                     caCertFilePath),
                                                       e);
                        }
                    }).orElse(null);

                } catch (RuntimeException re) {
                    throw re;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("The 'ssl' parameter must have one of jdk, or openssl");
            }

            KeyManagerFactory kmf;
            try {
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, keyPassword);
            } catch (Exception e) {
                throw new RuntimeException("Unable to init KeyManagerFactory. Please check password and location.", e);
            }

            TrustManagerFactory tmf;
            try {
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustStore != null ? trustStore : keyStore);
            } catch (Exception e) {
                throw new RuntimeException("Unable to init TrustManagerFactory. Please check.", e);
            }

            try {
                SSLContext sslContext = SSLContext.getInstance(tlsVersion);
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
                return sslContext;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    private static byte[] loadPem(Pattern pattern, File pemFile) throws IOException {
        try (InputStream in = new FileInputStream(pemFile)) {
            String pem = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
            String encoded = pattern.matcher(pem).replaceFirst("$1");
            return Base64.getMimeDecoder().decode(encoded);
        }
    }

    private static byte[] loadKeyFromPem(File keyPemFile) throws IOException {
        return loadPem(KEY_PATTERN, keyPemFile);
    }

    private static byte[] loadCertFromPem(File certPemFile) throws IOException {
        return loadPem(CERT_PATTERN, certPemFile);
    }
}
