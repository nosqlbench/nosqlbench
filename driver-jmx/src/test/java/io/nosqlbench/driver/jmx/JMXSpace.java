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

package io.nosqlbench.driver.jmx;

import io.nosqlbench.adapter.jmx.SecureUtils;
import io.nosqlbench.engine.api.util.SSLKsFactory;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.errors.OpConfigError;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class JMXSpace implements NBConfigurable {
    private final String name;
    private NBConfiguration config;
    private SSLContext sslContext;
    private JMXConnector connector;

    public JMXSpace(String name, NBConfiguration config) {
        this.name = name;
        this.config = config;

        SSLKsFactory.get().getContext(config);
    }

    public synchronized JMXConnector getConnector() {
        if (this.connector == null) {
            this.connector = bindConnector();
        }
        return this.connector;
    }

    private JMXConnector bindConnector() {

        Map<String, Object> connectorEnv = new HashMap<>();
        String username = config.get("username", String.class);
        String password = config.get("password", String.class);
        username = SecureUtils.readSecret("JMX username", username);
        password = SecureUtils.readSecret("JMX password", password);
        if (username != null && password != null) {
            connectorEnv.put(JMXConnector.CREDENTIALS, new String[]{username, password});
        }

        JMXConnector connector = null;
        try {
            JMXServiceURL url = bindJMXServiceURL();
            connector = JMXConnectorFactory.connect(url, connectorEnv);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connector;
    }

    private JMXServiceURL bindJMXServiceURL() {
        JMXServiceURL url = null;
        try {
            url = config.getOptional("url")
                .map(u -> {
                    try {
                        return new JMXServiceURL(u);
                    } catch (MalformedURLException e) {
                        throw new OpConfigError("Error while configuring JMX service URL: " + e.getMessage());
                    }
                })
                .orElse(null);

            if (url==null) {
                String host = config.get("host");
                String protocol = config.get("protocol");
                int port = config.get("port", Integer.class);
                String path = config.get("path");
                url = new JMXServiceURL(protocol, host, port, path);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    @Override
    public void applyConfig(NBConfiguration config) {
        this.config = config;
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(JMXSpace.class)
            .add(Param.optional("url"))
            .add(Param.optional("host"))
            .add(Param.optional("protocol"))
            .add(Param.optional("port",Integer.class))
            .add(Param.optional("path"))
            .add(Param.optional("username"))
            .add(Param.optional("password"))
            .add(SSLKsFactory.get().getConfigModel())
            .asReadOnly();
    }

//    NBConfiguration sslCfg = SSLKsFactory.get().getConfigModel().extractConfig(activityDef.getParams());
//        this.sslContext= SSLKsFactory.get().getContext(sslCfg);

}
