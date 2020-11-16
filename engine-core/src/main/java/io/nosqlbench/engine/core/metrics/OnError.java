package io.nosqlbench.engine.core.metrics;

public enum OnError {
    Warn,
    Throw;

    public static OnError valueOfName(String name) {
        for (OnError value : OnError.values()) {
            if (value.toString().toLowerCase().equals(name.toLowerCase())) {
                return value;
            }
        }
        throw new RuntimeException("No matching OnError enum value for '" + name + "'");
    }
}
