package io.nosqlbench.engine.extensions.optimizers;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.slf4j.Logger;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public class NashornMultivariateObjectScript implements MultivariateFunction {
    private final ScriptObjectMirror script;
    private final MVParams params;
    private Logger logger;

    public NashornMultivariateObjectScript(Logger logger, MVParams params, ScriptObjectMirror script) {
        this.logger = logger;
        this.script = script;
        this.params = params;
    }

    @Override
    public double value(double[] doubles) {
        if (doubles.length != params.size()) {
            throw new InvalidParameterException("Expected " + params.size() + " doubles, not " + doubles.length);
        }

        Map<String,Double> map = new HashMap<>();

        for (int i = 0; i < doubles.length; i++) {
            map.put(params.get(i).name, doubles[i]);
        }

        Object object = ScriptObjectMirror.wrap(map, null);
        Object result = null;
        result = this.script.call(script,object);
        return Double.valueOf(result.toString());
    }
}
