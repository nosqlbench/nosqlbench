/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.cqld4.opmappers;

public enum CqlD4OpType {

    /**
     * uses {@link com.datastax.oss.driver.api.core.cql.SimpleStatement}
     * <em>does not</em> parameterize values via the SimpleStatement API.
     * Pre-renderes the statement string with values included. This is not
     * efficient nor recommended for production use, although it is useful
     * for certain testing scenarios in which you need to create a lot
     * of DDL or other statements which require non-parameterizable fields
     * to be present in binding values.
     */
    raw,

    /**
     * uses {@link com.datastax.oss.driver.api.core.cql.SimpleStatement}
     * This parameterizes values and applies them as positional fields,
     * where the binding points are aligned by binding name.
     */
    simple,

    /**
     * uses {@link com.datastax.oss.driver.api.core.cql.SimpleStatement}
     * This type does everything that the {@link #simple} mode does, and
     * additionally uses prepared statements.
     */
    prepared,

    /**
     * Allows for a statement template to be used to create a batch statement.
     * The fields 'op_template', and 'repeat' are required, and all fields below
     * the op_template field are a nested version of the other op types here, but
     * supports only the simple and prepared forms for historic compatibility reasons.
     */
    batch,

    /**
     * uses {@link com.datastax.dse.driver.api.core.graph.ScriptGraphStatement}
     * This is the "raw" mode of using gremlin. It is not as efficient, and thus
     * is only recommended for testing or legacy apps.
     */
    gremlin,

    /**
     * uses {@link com.datastax.dse.driver.api.core.graph.FluentGraphStatement}
     * This mode is the recommended mode for gremlin execution. It uses the fluent
     * API on the client side. The fluent syntax is compiled and cached as bytecode
     * within a per-thread execution environment (for each op template). For each
     * cycle, the bindings are rendered, injected into that execution environment,
     * and then the bytecode is executed to render the current operation, which is
     * then sent to the server. Although this is arguably more involved, the result
     * is quite efficient and provides the closes idiomatic experience <em>AND</em>
     * the best performance.
     *
     * <p>This is the mode that is recommended for all graph usage.</p>
     */
    fluent,

    /**
     * reserved for future use
     */
    rainbow,
//    /**
//     * reserved for future use
//     */
//    sst
}
