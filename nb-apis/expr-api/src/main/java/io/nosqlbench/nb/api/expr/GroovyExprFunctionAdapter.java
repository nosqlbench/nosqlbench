package io.nosqlbench.nb.api.expr;

import java.util.Objects;

/**
 * Wraps an {@link ExprFunction} with a Groovy friendly adapter that exposes a
 * {@code call} method. Groovy treats any object with a {@code call} method as a
 * first-class function reference, allowing expressions like {@code f(args...)}.
 */
final class GroovyExprFunctionAdapter {

    private final ExprFunction delegate;

    GroovyExprFunctionAdapter(ExprFunction delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    public Object call(Object... args) {
        return delegate.apply(args);
    }
}
