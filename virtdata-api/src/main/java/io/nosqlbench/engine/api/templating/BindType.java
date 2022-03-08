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

package io.nosqlbench.engine.api.templating;

/**
 * The type of a parsed template depends on the structure of the bindings provided.
 */
public enum BindType {

    /**
     * A literal template is one which has no bindings that need to be provided to render a specific statement.
     * These templates are basically static statements.
     * Example: <em>{@code truncate testks.testtable;}</em>
     */
    literal,

    /**
     * A bindref template is one which has only a single bind point and no leading or trailing text.
     * It represents a single value which is to be injected, with no clear indication as to whether the
     * value should be in string form or not. These are used when referencing objects by bind point name.
     * Callers which use rawbind templates where Strings are needed should convert them with {@link Object#toString()}}
     * Example: <em>{@code {myvalue}}</em>
     */
    bindref,

    /**
     * A string template is one which is neither a literal template nor a bindref template. This includes
     * any template which has any amount of literal text and any template with more than one bind point.
     */
    concat

}
