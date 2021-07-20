package io.nosqlbench.nb.api.config.standard;

/**
 * All implementation types which wish to have a type-marshalled configuration
 * should implement this interface.
 */
public interface NBConfigurable extends NBCanConfigure, NBCanValidateConfig {

    @Override
    void applyConfig(NBConfiguration cfg);

    @Override
    NBConfigModel getConfigModel();
}
