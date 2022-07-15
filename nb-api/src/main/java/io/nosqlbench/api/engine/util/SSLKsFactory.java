/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.api.engine.util;

import io.nosqlbench.api.config.standard.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class SSLKsFactory implements NBMapConfigurable {
    private final static Logger logger = LogManager.getLogger(SSLKsFactory.class);

    private static final SSLKsFactory instance = new SSLKsFactory();

    private static final Pattern CERT_PATTERN = Pattern.compile("-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*CERTIFICATE[^-]*-+", 2);
    private static final Pattern KEY_PATTERN = Pattern.compile("-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", 2);
    public static final String SSL = "ssl";
    public static final String DEFAULT_TLSVERSION = "TLSv1.2";

    /**
     * Consider: https://gist.github.com/artem-smotrakov/bd14e4bde4d7238f7e5ab12c697a86a3
     */
    private SSLKsFactory() {
    }

    public static SSLKsFactory get() {
        return instance;
    }

    public ServerSocketFactory createSSLServerSocketFactory(NBConfiguration cfg) {
        SSLContext context = getContext(cfg);
        if (context == null) {
            throw new IllegalArgumentException("SSL is not enabled.");
        }
        return context.getServerSocketFactory();
    }

    public SocketFactory createSocketFactory(NBConfiguration cfg) {
        SSLContext context = getContext(cfg);
        if (context == null) {
            throw new IllegalArgumentException("SSL is not enabled.");
        }
        return context.getSocketFactory();
    }

    public SSLContext getContext(NBConfiguration cfg) {
        Optional<String> sslParam = cfg.getOptional(SSL);
        if (sslParam.isPresent()) {
            String tlsVersion = cfg.getOptional("tlsversion").orElse(DEFAULT_TLSVERSION);

            KeyStore keyStore;
            char[] keyPassword = null;
            KeyStore trustStore;

            if (sslParam.get().equals("jdk")) {

                final char[] keyStorePassword = cfg.getOptional("kspass")
                    .map(String::toCharArray)
                    .orElse(null);
                keyPassword = cfg.getOptional("keyPassword", "keypassword")
                    .map(String::toCharArray)
                    .orElse(keyStorePassword);

                keyStore = cfg.getOptional("keystore").map(ksPath -> {
                    try {
                        return KeyStore.getInstance(new File(ksPath), keyStorePassword);
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to load the keystore: " + e, e);
                    }
                }).orElse(null);

                trustStore = cfg.getOptional("truststore").map(tsPath -> {
                    try {
                        return KeyStore.getInstance(new File(tsPath),
                            cfg.getOptional("tspass")
                                .map(String::toCharArray)
                                .orElse(null));
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to load the truststore: " + e, e);
                    }
                }).orElse(null);

            } else if (sslParam.get().equals("openssl")) {
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");

                    keyStore = KeyStore.getInstance("JKS");
                    keyStore.load(null, null);

                    Certificate cert = cfg.getOptional("certFilePath").map(certFilePath -> {
                        try (InputStream is = new ByteArrayInputStream(loadCertFromPem(new File(certFilePath)))) {
                            return cf.generateCertificate(is);
                        } catch (Exception e) {
                            throw new RuntimeException(
                                String.format("Unable to load cert from %s: " + e, certFilePath),
                                e
                            );
                        }
                    }).orElse(null);

                    if (cert != null)
                        keyStore.setCertificateEntry("certFile", cert);

                    File keyFile = cfg.getOptional("keyfilepath").map(File::new)
                        .orElse(null);

                    if (keyFile != null) {
                        try {
                            keyPassword = cfg.getOptional("keyPassword", "keypassword")
                                .map(String::toCharArray)
                                .orElse("temp_key_password".toCharArray());

                            KeyFactory kf = KeyFactory.getInstance("RSA");
                            PrivateKey key = kf.generatePrivate(new PKCS8EncodedKeySpec(loadKeyFromPem(keyFile)));
                            keyStore.setKeyEntry("key", key, keyPassword,
                                cert != null ? new Certificate[]{cert} : null);
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Unable to load key from %s: " + e,
                                keyFile),
                                e);
                        }
                    }

                    trustStore = cfg.getOptional("caCertFilePath", "cacertfilepath").map(caCertFilePath -> {
                        try (InputStream is = new FileInputStream(new File(caCertFilePath))) {
                            KeyStore ts = KeyStore.getInstance("JKS");
                            ts.load(null, null);

                            Certificate caCert = cf.generateCertificate(is);
                            ts.setCertificateEntry("caCertFile", caCert);
                            return ts;
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Unable to load caCert from %s: " + e,
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
                throw new RuntimeException("Unable to init KeyManagerFactory. Please check password and location: " + e, e);
            }

            TrustManagerFactory tmf;
            try {
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustStore != null ? trustStore : keyStore);
            } catch (Exception e) {
                throw new RuntimeException("Unable to init TrustManagerFactory: " + e, e);
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

    @Override
    public void applyConfig(Map<String, ?> providedConfig) {

    }

    public NBConfigModel getConfigModel() {
        return ConfigModel.of(SSLKsFactory.class,
            Param.optional("ssl")
                .setDescription("Enable ssl and set the mode")
                .setRegex("jdk|openssl"),
            Param.defaultTo("tlsversion", DEFAULT_TLSVERSION),
            Param.optional("kspass"),
            Param.optional("keyPassword"),
            Param.optional("keystore"),
            Param.optional("truststore"),
            Param.optional("tspass"),
            Param.optional(List.of("keyFilePath","keyfilepath")),
            Param.optional("caCertFilePath"),
            Param.optional("certFilePath")
        ).asReadOnly();
    }
}
