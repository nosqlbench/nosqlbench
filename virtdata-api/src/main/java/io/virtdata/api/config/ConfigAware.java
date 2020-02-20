package io.virtdata.api.config;

import java.util.Map;

public interface ConfigAware {
    void applyConfig(Map<String,?> element);
    ConfigModel getConfigModel();
}
