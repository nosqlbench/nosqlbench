package io.nosqlbench.engine.extensions.optimizers;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.apache.commons.math3.analysis.MultivariateFunction;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PolyglotMultivariateObjectScript implements MultivariateFunction {

    private final MVParams params;
    private final Object function;
    private final Logger logger;

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
        } else if (result instanceof Integer) {
            return (double) ((Integer) result);
        } else {
            throw new RuntimeException(
                "Unable to case result of polyglot function return value as a double:" +
                    result.getClass().getCanonicalName() + ", toString=" + result.toString());
        }
    }
}
