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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Example metadata for Groovy expression helper functions. Examples provide concrete, executable
 * samples that can be validated automatically to guard against regressions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(ExprExamples.class)
public @interface ExprExample {

    /**
     * Optional short description for the example.
     */
    String description() default "";

    /**
     * Groovy expressions that should be used as arguments when invoking the function. The
     * invocation harness will evaluate each entry in the context of the example.
     */
    String[] args() default {};

    /**
     * Groovy expression representing the expected result. When left blank, other expectation
     * flags ({@link #matches()}, {@link #expectNull()}, {@link #expectNotNull()}) are used instead.
     */
    String expect() default "";

    /**
     * Regular expression the rendered result (via {@code String.valueOf(result)}) must satisfy.
     */
    String matches() default "";

    /**
     * Indicates that the result should be {@code null}.
     */
    boolean expectNull() default false;

    /**
     * Indicates that the result should be non-null.
     */
    boolean expectNotNull() default false;

    /**
     * Optional set of system properties to apply before the function is invoked. Each entry
     * should be formatted as {@code key=value}. When the value portion is empty, the property will
     * be cleared for the scope of the example.
     */
    String[] systemProperties() default {};

    /**
     * Which evaluation context should be used when invoking this example.
     */
    ExprExampleContext context() default ExprExampleContext.DEFAULT;
}
