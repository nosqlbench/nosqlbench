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

package io.nosqlbench.adapter.jmx;

import io.nosqlbench.engine.api.util.SSLKsFactory;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.errors.OpConfigError;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class JMXSpace implements NBConfigurable {
    private final String name;
    private JMXConnector connector;
    private NBConfiguration cfg;

    public JMXSpace(String name) {
        this.name = name;
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        this.cfg = cfg;
    }

    public NBConfigModel getConfigModel() {
        return ConfigModel.of(JMXSpace.class)
            .add(Param.optional("username", String.class))
            .add(Param.optional("password", String.class))
            .add(SSLKsFactory.get().getConfigModel())
            .asReadOnly();
    }

    public synchronized JMXConnector getConnector() {
        if (this.connector == null) {
            this.connector = bindConnector();
        }
        return this.connector;
    }

    private JMXConnector bindConnector() {

        Map<String, Object> connectorEnv = new HashMap<>();

        String username = cfg.getOptional("username")
            .map(u -> SecureUtils.readSecret("JMX username", u))
            .orElse(null);

        String password = cfg.getOptional("password")
            .map(p -> SecureUtils.readSecret("JMX password", p))
            .orElse(null);

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
        url = cfg.getOptional("url")
            .map(
                u -> {
                    try {
                        return new JMXServiceURL(u);
                    } catch (MalformedURLException e) {
                        throw new OpConfigError("Error with JMX URL: " + e);
                    }
                })
            .orElse(null);
        if (url!=null) {
            return url;
        }

        String host = cfg.get("host");
        String protocol = cfg.get("protocol");
        int port = cfg.getOrDefault("port", 0);
        String path = cfg.get("path");
        try {
            return new JMXServiceURL(protocol, host, port, path);
        } catch (MalformedURLException e) {
            throw new OpConfigError("Error with JMX URL parameters: " + e);
        }

    }
}
