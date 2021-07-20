package io.nosqlbench.nb.api.config.standard;

import java.util.Map;

/**
 * This configuration model describes what is valid to submit
 * for configuration for a given configurable object.
 */
public interface NBConfigModel {

    Map<String, Param<?>> getElements();

    Class<?> getOf();

    void assertValidConfig(Map<String, ?> config);

    NBConfiguration apply(Map<String, ?> config);
}
