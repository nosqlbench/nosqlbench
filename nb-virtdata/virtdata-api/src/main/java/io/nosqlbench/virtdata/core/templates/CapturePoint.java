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

package io.nosqlbench.virtdata.core.templates;

import java.util.Objects;

/**
 * A capture point is a named variable which should be extracted from a payload or result type
 * using a native driver API. The result is meant to be provided to the NoSQLBench runtime
 * during cycle execution, and stored in a scoped context of variables which can be re-used within
 * other operations.
 * <hr/>
 * <H2>Format</H2>
 *
 * <pre>{@code
 * select [username as u1] from users where userid={userid};
 * }</pre>
 *
 * In the example above, the span <em>[username as u1]</em> is recognized as a capture point.
 * The name of the variable to be captured is <em>username</em>. It is to be captured under
 * a different variable name <em>u1</em>.
 *
 * If the name is the same in both cases, i.e. the variable is named in the result as it
 * should be known after extraction, then you can elide the <em>as u1</em> clause as in this example:
 *
 * <pre>{@code
 * select [username] from users where userid={userid};
 * }</pre>
 *
 * During op mapping, any capture points are condensed down to the native driver vernacular by
 * removing the square brackets from the op template. Thus, the result of parsing the above would
 * yield a form compatible with a native driver. For example, converting to prepared statement form
 * would yield:
 *
 * <pre>{@code
 * select username from users where userid=:userid
 * }</pre>
 *
 * For details on the <em>{userid}</em> form, see {@link BindPoint}
 */
public class CapturePoint {

    private final String name;
    private final String asName;

    public CapturePoint(String name, String asName) {
        this.name = name;
        this.asName = asName;
    }

    public String getName() {
        return name;
    }

    /**
     * Create a CapturePoint with the specified anchorName, and an optional aliasName.
     * If aliasName is null, then the anchorName is used as the alias.
     *
     * @param anchorName The name of the capture variable in the native form
     * @param aliasName  The name of the captured value as seen by consumers
     * @return A new CapturePoint
     */
    public static CapturePoint of(String anchorName, String aliasName) {
        Objects.requireNonNull(anchorName);
        return new CapturePoint(anchorName, aliasName == null ? anchorName : aliasName);
    }

    /**
     * Create a CapturePoint with the specified anchorName, and the same aliasName.
     *
     * @param anchorName The name of the capture variable in the native form and as seen by consumers.
     * @return A new CapturePoint
     */
    public static CapturePoint of(String anchorName) {
        Objects.requireNonNull(anchorName);
        return new CapturePoint(anchorName, anchorName);
    }

    @Override
    public String toString() {
        return "[" + getName() + (name.equals(asName) ? "" : " as " + asName) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CapturePoint that = (CapturePoint) o;
        return Objects.equals(name, that.name) && Objects.equals(asName, that.asName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, asName);
    }
}
