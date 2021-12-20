package io.nosqlbench.engine.api.templating;

import java.util.function.LongFunction;

public class NamedTarget<E extends Enum<E>> {
    public final E enumId;
    public final String field;
    public final LongFunction<?> targetFunction;

    public NamedTarget(E enumId, String matchingOpFieldName, LongFunction<?> value) {
        this.enumId = enumId;
        this.field = matchingOpFieldName;
        this.targetFunction = value;
    }
}
