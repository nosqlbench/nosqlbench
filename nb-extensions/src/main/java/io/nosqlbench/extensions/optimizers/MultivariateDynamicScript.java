package io.nosqlbench.extensions.optimizers;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.slf4j.Logger;

import java.security.InvalidParameterException;
import java.util.Arrays;

public class MultivariateDynamicScript implements MultivariateFunction {
    private final ScriptObjectMirror script;
    private final int varcount;
    private Logger logger;

    public MultivariateDynamicScript(Logger logger, int varcount, ScriptObjectMirror script) {
        this.logger = logger;
        this.script = script;
        this.varcount = varcount;
    }

    @Override
    public double value(double[] doubles) {
        logger.info("invoking function with " + Arrays.toString(doubles));
        if (doubles.length != varcount) {
            throw new InvalidParameterException("Expected " + varcount + " doubles, not " + doubles.length);
        }
        Object result = null;
        if (doubles.length == 1) {
            result = this.script.call(script,
                    doubles[0]
            );
        } else if (doubles.length == 2) {
            result = this.script.call(script,
                    doubles[0], doubles[1]
            );
        } else if (doubles.length == 3) {
            result = this.script.call(script,
                    doubles[0], doubles[1], doubles[2]
            );
        } else if (doubles.length == 4) {
            result = this.script.call(script,
                    doubles[0], doubles[1], doubles[2], doubles[3]
            );
        } else if (doubles.length == 5) {
            result = this.script.call(script,
                    doubles[0], doubles[1], doubles[2], doubles[3], doubles[4]);
        }
        return Double.valueOf(result.toString());
    }
}
