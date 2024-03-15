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
 * <p>A capture point is a named variable which should be extracted from a payload or result type
 * using a native driver API. The result is meant to be provided to the NoSQLBench runtime
 * during cycle execution, and stored in a scoped context of variables which can be re-used within
 * other operations.
 * </p>
 * <p>
 * <hr/>
 *
 * <H2>Format</H2>
 *
 * <pre>{@code
 * select [username as u1] from users where userid={userid};
 * }</pre>
 * <p>
 * In the example above, the span <em>[username as u1]</em> is recognized as a capture point.
 * The name of the variable to be captured is <em>username</em>. It is to be captured under
 * a different variable name <em>u1</em>.
 * </p>
 *
 * <p>
 * If the name is the same in both cases, i.e. the variable is named in the result as it
 * should be known after extraction, then you can elide the <em>as u1</em> clause as in this example:
 *
 * <pre>{@code
 * select [username] from users where userid={userid};
 * }</pre>
 * </p>
 *
 * <p>
 * The scope of the captured
 * </p>
 *
 * <p>
 * During op mapping, any capture points are condensed down to the native driver vernacular by
 * removing the square brackets from the op template. Thus, the result of parsing the above would
 * yield a form compatible with a native driver. For example, converting to prepared statement form
 * would yield:
 *
 * <pre>{@code
 * select username from users where userid=:userid
 * }</pre>
 * <p>
 * For details on the <em>{userid}</em> form, see {@link BindPoint}
 */
public class CapturePoint {

    /**
     * The name of the capture point as known by the original protocol or driver API
     */
    private final String capturedName;

    /**
     * The name that the captured value should be stored as
     */
    private final String storedName;

    private final Scope storedScope;

    private final Class<?> storedType;
    private final Class<?> elementType;

    protected CapturePoint(
        String capturedName,
        String storedName,
        Scope storedScope,
        Class<?> storedType,
        Class<?> elementType
    ) {
        this.capturedName = capturedName;
        this.storedName = storedName;
        this.storedScope = storedScope;
        this.storedType = storedType;
        this.elementType = elementType;
    }

    public String getCapturedName() {
        return capturedName;
    }

    public String getStoredName() {
        return storedName;
    }

    public Scope getStoredScope() {
        return this.storedScope;
    }


    /**
     * Create a CapturePoint with the specified anchorName, and an optional aliasName.
     * If aliasName is null, then the anchorName is used as the alias.
     *
     * @param capturedName
     *     The name of the capture variable in the native form
     * @param storedName
     *     The name of the captured value as seen by consumers
     * @return A new CapturePoint
     */
    public static CapturePoint of(String capturedName, String storedName, Scope scope, Class<?> storedType,
                                  Class<?> elementType) {
        Objects.requireNonNull(capturedName);
        return new CapturePoint(capturedName, storedName == null ? capturedName : storedName, scope, storedType,
            elementType);
    }

    /**
     * Create a CapturePoint with the specified anchorName, and the same aliasName.
     *
     * @param anchorName
     *     The name of the capture variable in the native form and as seen by consumers.
     * @return A new CapturePoint
     */
    public static CapturePoint of(String anchorName) {
        Objects.requireNonNull(anchorName);
        return new CapturePoint(anchorName, anchorName, Scope.stanza, Object.class, null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(capturedName);
        if (!capturedName.equals(storedName)) {
            sb.append(" as:").append(storedName);
        }
        sb.append(" scope:").append(storedScope.name());
        if (storedType != Object.class) {
            sb.append(" type:").append(storedType.getCanonicalName());
        }
        sb.append("]");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CapturePoint that = (CapturePoint) o;

        if (!capturedName.equals(that.capturedName)) return false;
        if (!storedName.equals(that.storedName)) return false;
        if (storedScope != that.storedScope) return false;
        return storedType.equals(that.storedType);
    }

    @Override
    public int hashCode() {
        int result = capturedName.hashCode();
        result = 31 * result + storedName.hashCode();
        result = 31 * result + storedScope.hashCode();
        result = 31 * result + storedType.hashCode();
        return result;
    }

    public Class<?> getStoredType() {
        return this.storedType;
    }

    public static enum Scope {
        /**
         * The stanza scope includes the op sequence, but not the next iteration of an op sequence
         */
        stanza,
        /**
         * Thread scope is equivalent to thread-local, although it may be implemented using a different mechanism with
         * virtual threads
         */
        thread,
        /**
         * Container scope is limited to the container within which an op is executed
         */
        container,

        /**
         * Session scope includes all containers, threads, and stanzas within a session
         */
        session
    }
}
