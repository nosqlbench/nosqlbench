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

package io.nosqlbench.adapter.amqp.util;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AmqpClientConf {
    private static final Logger logger = LogManager.getLogger(AmqpClientConf.class);

    // https://docs.datastax.com/en/streaming/starlight-for-rabbitmq/2.10.1.x/configuration/configuration.html
    private final Map<String, String> configMap = new HashMap<>();


    public AmqpClientConf(final String clientConfFileName) {

        //////////////////
        // Read related AMQP client configuration settings from a file
        this.readRawConfFromFile(clientConfFileName);
    }

    public void readRawConfFromFile(final String fileName) {
        final File file = new File(fileName);

        try {
            final String canonicalFilePath = file.getCanonicalPath();

            final Parameters params = new Parameters();

            final FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                    .configure(params.properties()
                        .setFileName(fileName));

            final Configuration config = builder.getConfiguration();

            for (final Iterator<String> it = config.getKeys(); it.hasNext(); ) {
                final String confKey = it.next();
                final String confVal = config.getProperty(confKey).toString();

                // Get client connection specific configuration settings, removing "topic." prefix
                if (!StringUtils.isBlank(confVal))
                    this.configMap.put(confKey, confVal);
            }
        } catch (final IOException ioe) {
            AmqpClientConf.logger.error("Can't read the specified config properties file: {}", fileName);
            ioe.printStackTrace();
        } catch (final ConfigurationException cex) {
            AmqpClientConf.logger.error("Error loading configuration items from the specified config properties file: {}:{}", fileName, cex.getMessage());
            cex.printStackTrace();
        }
    }

    public Map<String, String> getConfigMap() { return this.configMap; }

    public String toString() {
        return new ToStringBuilder(this).
            append("configMap", this.configMap).
            toString();
    }
}
