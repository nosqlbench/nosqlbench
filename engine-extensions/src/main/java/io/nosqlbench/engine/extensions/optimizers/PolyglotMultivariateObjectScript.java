package io.nosqlbench.engine.extensions.optimizers;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.slf4j.Logger;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PolyglotMultivariateObjectScript implements MultivariateFunction {

    private final MVParams params;
    private final Object function;
    private Logger logger;

    public PolyglotMultivariateObjectScript(Logger logger, MVParams params, Object function) {
        this.logger = logger;
        this.function = function;
        this.params = params;
    }

    @Override
    public double value(double[] doubles) {
        if (doubles.length != params.size()) {
            throw new InvalidParameterException("Expected " + params.size() + " doubles, not " + doubles.length);
        }

        Map<String,Object> fparams = new HashMap<>();
        for (int i = 0; i < params.size(); i++) {
            fparams.put(params.get(i).name, doubles[i]);
        }
        Object[] args = new Object[]{ProxyObject.fromMap(fparams)};

        Object result = ((Function<Object[], Object>) function).apply(args);

        if (result instanceof Double) {
            return (Double) result;
        } else {
            throw new RuntimeException(
                "Unable to case result of polyglot function return value as a double:" +
                    result.getClass().getCanonicalName()+", toString=" + result.toString());
        }
    }
}
