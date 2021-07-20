package io.nosqlbench.nb.api.config.standard;

import java.util.Map;

public interface NBMapConfigurable extends NBCanValidateConfig {
    void applyConfig(Map<String, ?> providedConfig);
}
