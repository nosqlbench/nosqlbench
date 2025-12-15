/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.mql.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a SQL fragment with parameterized values.
 *
 * <p>This class ensures that all SQL queries use parameterized queries
 * to prevent SQL injection attacks. User input is never concatenated
 * directly into SQL strings.</p>
 *
 * <p>Example:</p>
 * <pre>
 * SQLFragment fragment = new SQLFragment(
 *     "SELECT * FROM metrics WHERE name = ?",
 *     List.of("http_requests_total")
 * );
 * </pre>
 */
public class SQLFragment {
    private final String sql;
    private final List<Object> parameters;

    /**
     * Creates a SQL fragment with no parameters
     */
    public SQLFragment(String sql) {
        this(sql, Collections.emptyList());
    }

    /**
     * Creates a SQL fragment with parameters
     *
     * @param sql The SQL string with ? placeholders
     * @param parameters The parameter values in order
     */
    public SQLFragment(String sql, List<Object> parameters) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }
        this.sql = sql;
        this.parameters = new ArrayList<>(parameters);
    }

    /**
     * Gets the SQL string with ? placeholders
     */
    public String getSql() {
        return sql;
    }

    /**
     * Gets the parameter values in order
     */
    public List<Object> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Combines multiple SQL fragments with a separator
     *
     * @param fragments List of fragments to combine
     * @param separator Separator between fragments (e.g., " AND ", ", ")
     * @return Combined fragment
     */
    public static SQLFragment combine(List<SQLFragment> fragments, String separator) {
        if (fragments == null || fragments.isEmpty()) {
            return new SQLFragment("");
        }

        if (fragments.size() == 1) {
            return fragments.get(0);
        }

        StringBuilder sql = new StringBuilder();
        List<Object> allParams = new ArrayList<>();

        for (int i = 0; i < fragments.size(); i++) {
            if (i > 0) {
                sql.append(separator);
            }
            SQLFragment fragment = fragments.get(i);
            sql.append(fragment.getSql());
            allParams.addAll(fragment.getParameters());
        }

        return new SQLFragment(sql.toString(), allParams);
    }

    /**
     * Wraps this fragment in parentheses
     */
    public SQLFragment parenthesize() {
        return new SQLFragment("(" + sql + ")", parameters);
    }

    /**
     * Appends another fragment to this one
     */
    public SQLFragment append(String sqlText) {
        return new SQLFragment(this.sql + sqlText, this.parameters);
    }

    /**
     * Appends another fragment with its parameters
     */
    public SQLFragment append(SQLFragment other) {
        List<Object> combined = new ArrayList<>(this.parameters);
        combined.addAll(other.parameters);
        return new SQLFragment(this.sql + other.sql, combined);
    }

    /**
     * Creates a fragment with a parameter placeholder
     *
     * @param sql SQL with one ? placeholder
     * @param parameter The parameter value
     */
    public static SQLFragment withParam(String sql, Object parameter) {
        return new SQLFragment(sql, List.of(parameter));
    }

    @Override
    public String toString() {
        return "SQLFragment{sql='" + sql + "', params=" + parameters + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLFragment that = (SQLFragment) o;
        return sql.equals(that.sql) && parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        return 31 * sql.hashCode() + parameters.hashCode();
    }
}
