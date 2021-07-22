package io.nosqlbench.nb.api.config.standard;

import java.util.Map;

public interface NBMapConfigurable extends NBConfigModelProvider {
    void applyConfig(Map<String, ?> providedConfig);
}
