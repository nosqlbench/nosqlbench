package io.nosqlbench.nb.api.expr.providers;

import io.nosqlbench.nb.api.expr.ExprFunctionProvider;
import io.nosqlbench.nb.api.expr.ExprRuntimeContext;

import java.util.Map;

/**
 * Provides helpers for accessing workload parameters from Groovy expressions.
 */
public class ParameterExprFunctionsProvider implements ExprFunctionProvider {
    @Override
    public void contribute(ExprRuntimeContext context) {
        context.registerFunction("param", args -> requireParam(context.parameters(), args));
        context.registerFunction("paramOr", args -> optionalParam(context.parameters(), args));
        context.registerFunction("hasParam", args -> hasParam(context.parameters(), args));
    }

    private Object requireParam(Map<String, ?> params, Object[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("param(name) requires exactly one argument");
        }
        String name = String.valueOf(args[0]);
        if (!params.containsKey(name)) {
            throw new IllegalArgumentException("Parameter '" + name + "' was not provided");
        }
        return params.get(name);
    }

    private Object optionalParam(Map<String, ?> params, Object[] args) {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("paramOr(name, default?) expects one or two arguments");
        }
        String name = String.valueOf(args[0]);
        Object value = params.get(name);
        if (value == null && args.length == 2) {
            return args[1];
        }
        return value;
    }

    private Object hasParam(Map<String, ?> params, Object[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("hasParam(name) requires exactly one argument");
        }
        String name = String.valueOf(args[0]);
        return params.containsKey(name);
    }
}
