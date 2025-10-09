package io.nosqlbench.nb.api.expr.providers;

import io.nosqlbench.nb.api.expr.ExprFunctionProvider;
import io.nosqlbench.nb.api.expr.ExprRuntimeContext;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Provides core utility functions that are generally useful for workload preprocessing.
 */
public class CoreExprFunctionsProvider implements ExprFunctionProvider {

    @Override
    public void contribute(ExprRuntimeContext context) {
        context.registerFunction("env", this::lookupEnv);
        context.registerFunction("prop", this::lookupProperty);
        context.registerFunction("uuid", args -> UUID.randomUUID().toString());
        context.registerFunction("now", args -> Instant.now().toString());
        context.registerFunction("upper", args -> transformCase(args, true));
        context.registerFunction("lower", args -> transformCase(args, false));
        context.registerFunction("source", args -> context.sourceUri().map(Object::toString).orElse(""));
    }

    private Object lookupEnv(Object[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("env(name[, default]) requires at least a name");
        }
        String name = String.valueOf(args[0]);
        String value = System.getenv(name);
        if (value == null && args.length > 1) {
            return args[1];
        }
        return value;
    }

    private Object lookupProperty(Object[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("prop(name[, default]) requires at least a name");
        }
        String name = String.valueOf(args[0]);
        String value = System.getProperty(name);
        if (value == null && args.length > 1) {
            return args[1];
        }
        return value;
    }

    private Object transformCase(Object[] args, boolean upper) {
        if (args.length != 1) {
            throw new IllegalArgumentException((upper ? "upper" : "lower") + "(value) requires exactly one argument");
        }
        String value = String.valueOf(args[0]);
        return upper ? value.toUpperCase(Locale.ROOT) : value.toLowerCase(Locale.ROOT);
    }
}
