package io.nosqlbench.adapter.cqld4;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum Cqld4Processors {
    print(Cqld4PrintProcessor::new);

    private final Function<Map<String, ?>, ResultSetProcessor> initializer;

    Cqld4Processors(Function<Map<String,?>,ResultSetProcessor> initializer) {
        this.initializer = initializer;
    }

    public static ResultSetProcessor resolve(Map<String,?> cfg) {
        String type = Optional.ofNullable(cfg.get("type"))
            .map(Object::toString)
            .orElseThrow(() -> new RuntimeException("Map config provided for a processor, but with no type field."));

        Cqld4Processors procType = Cqld4Processors.valueOf(type);
        ResultSetProcessor p = procType.initializer.apply(cfg);
        return p;
    }
}
