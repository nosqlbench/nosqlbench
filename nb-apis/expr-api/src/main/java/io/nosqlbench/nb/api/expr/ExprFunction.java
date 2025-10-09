package io.nosqlbench.nb.api.expr;

/**
 * Functional contract for a function that can be exposed to Groovy expressions.
 * Implementations are expected to provide side-effect free logic when possible.
 */
@FunctionalInterface
public interface ExprFunction {
    /**
     * Apply the function to the provided argument list.
     *
     * @param args ordered arguments supplied from the expression invocation
     * @return the value to substitute into the rendered workload
     */
    Object apply(Object... args);
}
