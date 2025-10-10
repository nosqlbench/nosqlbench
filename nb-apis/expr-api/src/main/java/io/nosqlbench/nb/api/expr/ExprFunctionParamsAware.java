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


import io.nosqlbench.nb.api.config.params.Element;

/**
 * Optional mix-in for {@link ExprFunctionProvider} implementations that want direct access to the
 * workload parameter set for the current evaluation. When present, the runtime supplies the
 * parameter view before any annotated functions are registered.
 */
public interface ExprFunctionParamsAware {

    /**
     * Provide the workload parameters associated with the current expression evaluation.
     *
     * @param params normalized parameter view; never {@code null}
     */
    void setParams(Element params);
}
