package io.nosqlbench.nb.api.expr;

import groovy.lang.Binding;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Default runtime context implementation that uses a Groovy {@link Binding} as
 * the storage mechanism for both variables and function adapters.
 */
final class GroovyExprRuntimeContext implements ExprRuntimeContext {

    private final Binding binding;
    private final Optional<URI> sourceUri;
    private final Map<String, ?> parameters;

    GroovyExprRuntimeContext(Binding binding, Optional<URI> sourceUri, Map<String, ?> parameters) {
        this.binding = Objects.requireNonNull(binding, "binding");
        this.sourceUri = Objects.requireNonNull(sourceUri, "sourceUri");
        this.parameters = Map.copyOf(parameters);
    }

    Binding binding() {
        return binding;
    }

    @Override
    public Optional<URI> sourceUri() {
        return sourceUri;
    }

    @Override
    public Map<String, ?> parameters() {
        return parameters;
    }

    @Override
    public void setVariable(String name, Object value) {
        binding.setVariable(name, value);
    }

    @Override
    public void registerFunction(String name, ExprFunction function) {
        binding.setVariable(name, new GroovyExprFunctionAdapter(function));
    }
}
