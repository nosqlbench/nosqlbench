package io.nosqlbench.nb.api.config.params;

import java.util.Map;
import java.util.Optional;

/**
 * A generic type-safe reader interface for parameters.
 * TODO: This should be consolidated with the design of ConfigLoader once the features of these two APIs are stabilized.
 *
 * The source data for a param reader is intended to be a collection of something, not a single value.
 * As such, if a single value is provided, an attempt will be made to convert it from JSON if it starts with
 * object or array notation. If not, the value is assumed to be in the simple ParamsParser form.
 */
public interface Element {
    String getElementName();

    <T> Optional<T> get(String name, Class<? extends T> classOfT);

    <T> T getOr(String name, T defaultValue);

    Map<String, Object> getMap();
}
