package io.nosqlbench.engine.api.activityimpl.uniform;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * If you provide a type of invokable element in this list, then it should
 * automatically be handled by NB.
 */
public enum NBInvokerType {
    NBRunnable(Runnable.class),
    NBCallable(Callable.class),
    NBFunction(Function.class);

    private final Class<?> typeclass;

    NBInvokerType(Class<?> typeClass) {
        this.typeclass=typeClass;
    }

    public static Optional<NBInvokerType> valueOfType(Class<?> c) {
        for (NBInvokerType type : NBInvokerType.values()) {
            if (type.typeclass.equals(c)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}
