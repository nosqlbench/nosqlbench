package io.nosqlbench.nb.api.expr.annotations;

/*
 * Copyright (c) nosqlbench
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


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative metadata for Groovy expression helper functions. Methods annotated with this
 * will be discovered and registered automatically when {@link io.nosqlbench.nb.api.expr.ExprFunctionProvider}
 * implementations opt in to annotation-driven registration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExprFunctionSpec {
    /**
     * Name that callers will use from Groovy expressions. Defaults to the method name when left blank.
     */
    String name() default "";

    /**
     * Short call-style synopsis, e.g. {@code upper(value)}.
     */
    String synopsis() default "";

    /**
     * One-line description of the helper function.
     */
    String description() default "";
}
