package io.nosqlbench.nb.api.config.standard;

import java.util.Optional;

public interface NBConfigReadable {
    <T> T getOrDefault(String name, T defaultValue);
    <T> Optional<T> getOptional(String name, Class<? extends T> type);
    <T> Optional<T> getOptional(String name);

}
