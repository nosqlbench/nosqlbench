package io.nosqlbench.nb.api.config.fieldreaders;

import java.util.function.LongFunction;

/**
 * An interface which captures the semantics and patterns of
 * reading field values that are rendered functionally.
 * This interface is meant to help standardize the user
 * interfaces for reading configuration and fields across
 * the NB codebase.
 * See also {@link StaticFieldReader}
 * and {@link EnvironmentReader}
 */
public interface DynamicFieldReader {
    boolean isDefinedDynamic(String field);

    <T> T get(String field, long input);

    <V> LongFunction<V> getAsFunctionOr(String name, V defaultValue);
}
