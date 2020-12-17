package io.nosqlbench.nb.api.config;

import java.util.Map;

public interface ConfigAware {
    void applyConfig(Map<String, ?> providedConfig);

    ConfigModel getConfigModel();
}
