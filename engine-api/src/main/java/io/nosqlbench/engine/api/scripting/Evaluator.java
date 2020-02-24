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

/**
 * <p>
 * An evaluator is a scripting helper that knows what its return type will be at runtime.
 * You can create an Evaluator from an environment and a desired return type, then reuse it.
 * Primitive types that can be mapped from the script to the return type should, including
 * </p>
 * <UL>
 * <LI>Double -&gt; Long</LI>
 * <LI>Double -&gt; Integer</LI>
 * <LI>Double -&gt; Float</LI>
 * <LI>Long -&gt; Integer</LI>
 * </UL>
 *
 * <p>An evaluator is not threadsafe by default. If you need threadsafe evaluators with similar
 * semantics, wrap it in a ThreadLocal.</p>
 *
 * @param <T> The return type that is needed by the caller
 */
public interface Evaluator<T> {

    /**
     * Evaluate the provided script, returning the value that it yields
     *
     * @return An object of type T
     */
    T eval();

    /**
     * @param scriptText Nashorn compatible script text
     * @return this NahornEvaluator, for method chaining
     */
    NashornEvaluator<T> script(String scriptText);

    /**
     * Set the variable environment of the evaluator
     *
     * @param varName the variable name to add to the environment
     * @param var     the object to bind to the varname
     * @return this NashornEvaluator, for method chaining
     */
    NashornEvaluator<T> put(String varName, Object var);
}
