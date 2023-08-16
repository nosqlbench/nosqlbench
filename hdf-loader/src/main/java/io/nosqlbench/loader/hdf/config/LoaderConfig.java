/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.loader.hdf.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LoaderConfig {
    private static final Logger logger = LogManager.getLogger(LoaderConfig.class);
    private static final Yaml yaml = new Yaml();
    private final Map<String, Object> configMap;

    public LoaderConfig(String filePath) throws IOException {
        FileReader fileReader = new FileReader(filePath);
        configMap = yaml.load(fileReader);
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            logger.debug(entry.getKey() + " : " + entry.getValue());
        }
    }

    public Object getRawValue(String key) {
        return configMap.get(key);
    }

    public String getStringValue(String key) {
        return configMap.get(key).toString();
    }

    public List<String> getDatasets() {
        return (List<String>) configMap.get("datasets");
    }

    public String getFormat() {
        return (String) configMap.getOrDefault("format", "HD5");
    }

    public Map<String,String> getAstra() {
        return (Map<String,String>) configMap.get("astra");
    }

    public String getEmbedding() {
        return (String) configMap.getOrDefault("embedding", "Deeplearning4j");
    }

    public String getWriter() {
        return (String) configMap.getOrDefault("writer", "filewriter");
    }

    public String getSourceFile() {
        return (String) configMap.get("sourceFile");
    }

    public String getTargetFile() {
        return (String) configMap.getOrDefault("targetFile", "./vectors.txt");
    }

    public int getThreads() {
        return (int) configMap.getOrDefault("threads", 5);
    }

    public int getQueueSize() {
        return (int) configMap.getOrDefault("queueSize", 1000);
    }
}
