/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.scripting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;

/**
 * public void setBindings(Bindings bindings, int scope);
 *
 * @param <T> generic parameter for return types from this evaluator
 */
public class NashornEvaluator<T> implements Evaluator<T> {
    private final static Logger logger = LoggerFactory.getLogger(NashornEvaluator.class);

    private static final ScriptEngineManager engineManager = new ScriptEngineManager();
    private final ScriptEngine scriptEngine;
    private final SimpleBindings bindings = new SimpleBindings();
    private String script = "";
    private final Class<? extends T> resultType;
    private CompiledScript compiled;

    /**
     * Create a new NashornEvaluator.
     *
     * @param resultType The required class of the result type, which must extend generic parameter type t.
     * @param vars Optional pairs of names and values. vars[0] is a name, vars[1] is a value, ...
     */
    public NashornEvaluator(Class<? extends T> resultType, Object... vars) {
        this.scriptEngine = engineManager.getEngineByName("nashorn");
        this.scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        this.resultType = resultType;
        for (int i = 0; i < vars.length; i += 2) {
            this.put(vars[i].toString(), vars[i + 1]);
        }
    }

    /**
     * Set the script that will be evaluated.
     * @param scriptText Nashorn compatible script text
     * @return this NashornEvaluator, for method chaining
     */
    @Override
    public NashornEvaluator<T> script(String scriptText) {
        this.script = scriptText;
        try {
            Object result = null;
            if (scriptEngine instanceof Compilable) {
                logger.info("Using direct script compilation");
                Compilable compilableEngine = (Compilable) scriptEngine;
                compiled = compilableEngine.compile(script);
                logger.trace("Compiled script:" + script);
            } else {
                logger.trace("Did not compile script: " + script);
            }
        } catch (ScriptException e) {
            throw new RuntimeException("Script compilation error for " + scriptText + ": ", e);
        }
        return this;
    }


    /**
     * Evaluate the compiled script if it is compiled, or the raw script text otherwise.
     * It is not an error to call this without setting the script to something other than the default of "",
     * but it not very useful in most cases.
     * @return The value produced by the script, compiled or not
     */
    @Override
    public T eval() {
        T result = null;
        try {
            Object evaled = null;
            if (compiled != null) {
                evaled = compiled.eval();
            } else {
                evaled = scriptEngine.eval(script);
            }
            result = convert(resultType, evaled);
        } catch (ScriptException e) {
            throw new RuntimeException("Script error while evaluating result for '" + script + "':", e);
        } catch (Exception o) {
            throw new RuntimeException("Non-Script error while evaluating result for '" + script + "':", o);
        }
        return result;
    }

    /**
     * Put a varianble into the script environment
     * @param varName the variable name to add to the environment
     * @param var     the object to bind to the varname
     * @return this NashornEvaluator, for method chaining
     */
    @Override
    public NashornEvaluator<T> put(String varName, Object var) {
        bindings.put(varName, var);
        return this;
    }

    /**
     * Convert some basic types from the script to the requested type. This makes it easier
     * to deal with issues across the type systems, with the risk of unintended conversions.
     * Be choosy about what you support here. Less is more.
     * @param expectedType the wanted type to return
     * @param result the result produced by the script
     * @return the converted value
     */
    private T convert(Class<? extends T> expectedType, Object result) {
        if (expectedType.isAssignableFrom(result.getClass())) {
            return expectedType.cast(result);
        }
        String desiredClass = expectedType.getSimpleName();
        Class<?> resultClass = result.getClass();

        if (resultClass == Double.class) {
            switch (desiredClass) {
                case "Long":
                    return expectedType.cast(((Double) result).longValue());
                case "Integer":
                    return expectedType.cast(((Double) result).intValue());
                case "Float":
                    return expectedType.cast(((Double) result).floatValue());
                default:
                    throw new RuntimeException("Incompatible result type requested for conversion from " + resultClass + " to " + desiredClass);
            }
        }

        if (resultClass == Long.class) {
            switch (desiredClass) {
                case "Integer":
                    return expectedType.cast(((Long) result).intValue());
                default:
                    throw new RuntimeException("Incompatible result type requested for conversion from " + resultClass + " to " + desiredClass);
            }
        }
        throw new RuntimeException(
                "Incompatible input type for conversion from evaluator:" + result.getClass() + ", " +
                        "when type " + expectedType.getSimpleName() + " was needed.");

    }

}
