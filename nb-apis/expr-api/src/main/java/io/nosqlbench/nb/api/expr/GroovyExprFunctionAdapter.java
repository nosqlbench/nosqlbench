package io.nosqlbench.nb.api.expr;

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


import java.util.Objects;

/**
 * Wraps an {@link ExprFunction} with a Groovy friendly adapter that exposes a
 * {@code call} method. Groovy treats any object with a {@code call} method as a
 * first-class function reference, allowing expressions like {@code f(args...)}.
 */
final class GroovyExprFunctionAdapter {

    private final ExprFunction delegate;

    GroovyExprFunctionAdapter(ExprFunction delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    public Object call(Object... args) {
        return delegate.apply(args);
    }
}
