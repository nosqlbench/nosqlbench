package io.nosqlbench.nb.api.expr;

/**
 * Service Provider contract for contributing functions and variables into the
 * expression evaluation environment that is prepared for each workload file.
 */
public interface ExprFunctionProvider {
    /**
     * Contribute functions or variables to the provided evaluation context.
     *
     * @param context mutable evaluation context scoped to the file being processed
     */
    void contribute(ExprRuntimeContext context);
}
