package io.nosqlbench.nb.api.config;

import java.util.Map;

public interface ConfigModel {
    Map<String, ConfigElement> getElements();

    Class<?> getOf();

    void assertValidConfig(Map<String, ?> config);

    ConfigReader apply(Map<String, ?> config);
}
