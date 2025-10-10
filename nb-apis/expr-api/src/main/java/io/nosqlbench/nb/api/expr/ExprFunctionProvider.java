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

/**
 * Service Provider contract for contributing functions and variables into the
 * expression evaluation environment that is prepared for each workload file.
 *
 * <p>Implementations should be annotated with
 * {@code @io.nosqlbench.nb.annotations.Service(value = ExprFunctionProvider.class, selector = "...")}
 * so that the {@code expr-api} annotation processor can generate the required
 * {@code META-INF/services} descriptors automatically. Functions can further declare
 * standalone {@link io.nosqlbench.nb.api.expr.annotations.ExprExample} annotations so the runtime
 * can validate documentation examples automatically.</p>
 */
public interface ExprFunctionProvider {
}
