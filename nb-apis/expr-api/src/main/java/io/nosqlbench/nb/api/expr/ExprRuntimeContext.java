package io.nosqlbench.nb.api.expr;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * Runtime view of the expression evaluation environment that service providers
 * can use to register functions or inspect workload level metadata prior to
 * expression evaluation.
 */
public interface ExprRuntimeContext {
    /**
     * @return the URI for the workload that is currently being processed if one is available
     */
    Optional<URI> sourceUri();

    /**
     * @return immutable view of parameters supplied alongside the workload
     */
    Map<String, ?> parameters();

    /**
     * Set a variable into the Groovy binding prior to evaluation.
     *
     * @param name variable name to expose
     * @param value value to bind to the provided name
     */
    void setVariable(String name, Object value);

    /**
     * Register a function that can be invoked directly from Groovy expressions.
     *
     * @param name name of the function as it will appear to Groovy callers
     * @param function implementation that backs the function call
     */
    void registerFunction(String name, ExprFunction function);
}
