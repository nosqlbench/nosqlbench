package io.nosqlbench.nb.api.config.standard;

import java.util.List;
import java.util.Map;

/**
 * <p>This configuration model describes what is valid to submit
 * for configuration for a given configurable object. Once this
 * is provided by a configurable element, it is used internally
 * by NoSQLBench to ensure that only valid configuration are
 * given to newly built objects.</p>
 *
 * <p>It is conventional to put the config model at the bottom of any
 * implementing class for quick reference.</p>
 */
public interface NBConfigModel {

    Map<String, Param<?>> getNamedParams();

    List<Param<?>> getParams();

    Class<?> getOf();

    void assertValidConfig(Map<String, ?> config);

    NBConfiguration apply(Map<String, ?> config);

    <V> Param<V> getParam(String... name);

    /**
     * Extract the fields from the shared config into a separate config,
     * removing those that are defined in this model and leaving
     * extraneous config fields in the provided model.
     *
     * <em>This method mutates the map that is provided.</em>
     *
     * @param sharedConfig A config map which can provide fields to multiple models
     * @return A new configuration for the extracted fields only.
     */
    NBConfiguration extract(Map<String, ?> sharedConfig);
    NBConfiguration extract(NBConfiguration cfg);

    NBConfigModel add(NBConfigModel otherModel);

}
