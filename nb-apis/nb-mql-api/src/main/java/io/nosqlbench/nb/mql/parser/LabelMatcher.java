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

import java.util.regex.Pattern;

/**
 * Represents a label matching condition in MetricsQL.
 *
 * <p>Supports four types of label matching:</p>
 * <ul>
 *   <li>EQUAL: label="value" - Exact string match</li>
 *   <li>NOT_EQUAL: label!="value" - Not equal to value</li>
 *   <li>REGEX: label=~"pattern" - Regex match</li>
 *   <li>NOT_REGEX: label!~"pattern" - Negative regex match</li>
 * </ul>
 *
 * <p>Label names are validated to ensure they contain only valid characters.</p>
 */
public class LabelMatcher {

    /**
     * Label name validation pattern: must start with letter or underscore,
     * followed by alphanumeric characters or underscores.
     */
    private static final Pattern VALID_LABEL_NAME = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    public enum MatchType {
        EQUAL,
        NOT_EQUAL,
        REGEX,
        NOT_REGEX
    }

    private final String labelName;
    private final String value;
    private final MatchType matchType;
    private final Pattern compiledPattern; // For regex matching

    /**
     * Creates a label matcher with validation
     *
     * @param labelName The label name (validated against security pattern)
     * @param value The value or regex pattern
     * @param matchType The type of matching
     * @throws IllegalArgumentException if label name is invalid
     */
    public LabelMatcher(String labelName, String value, MatchType matchType) {
        if (labelName == null || labelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Label name cannot be null or empty");
        }

        // Security: Validate label name to prevent SQL injection
        if (!VALID_LABEL_NAME.matcher(labelName).matches()) {
            throw new IllegalArgumentException(
                "Invalid label name: '" + labelName + "'. " +
                "Label names must start with letter/underscore and contain only alphanumeric/underscore characters."
            );
        }

        if (value == null) {
            throw new IllegalArgumentException("Label value cannot be null");
        }

        this.labelName = labelName;
        this.value = value;
        this.matchType = matchType;

        // Pre-compile regex patterns for validation
        if (matchType == MatchType.REGEX || matchType == MatchType.NOT_REGEX) {
            try {
                this.compiledPattern = Pattern.compile(value);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "Invalid regex pattern for label '" + labelName + "': " + e.getMessage(), e
                );
            }
        } else {
            this.compiledPattern = null;
        }
    }

    public String getLabelName() {
        return labelName;
    }

    public String getValue() {
        return value;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public Pattern getCompiledPattern() {
        return compiledPattern;
    }

    /**
     * Checks if this matcher would match the given label value
     */
    public boolean matches(String labelValue) {
        if (labelValue == null) {
            return matchType == MatchType.NOT_EQUAL || matchType == MatchType.NOT_REGEX;
        }

        return switch (matchType) {
            case EQUAL -> labelValue.equals(value);
            case NOT_EQUAL -> !labelValue.equals(value);
            case REGEX -> compiledPattern.matcher(labelValue).matches();
            case NOT_REGEX -> !compiledPattern.matcher(labelValue).matches();
        };
    }

    /**
     * Generates SQL fragment for this label matcher.
     * Returns a subquery that filters metric instances by label.
     *
     * @return SQLFragment with parameterized query
     */
    public SQLFragment toSQL() {
        // For SQLite, we'll use LIKE for simple patterns and regex for complex ones
        // Note: SQLite doesn't have built-in regex, so we'll need to handle that at runtime

        return switch (matchType) {
            case EQUAL ->
                new SQLFragment(
                    "mi.label_set_id IN (\n" +
                    "  SELECT lsm.label_set_id FROM label_set_membership lsm\n" +
                    "  JOIN label_key lk ON lk.id = lsm.label_key_id\n" +
                    "  JOIN label_value lv ON lv.id = lsm.label_value_id\n" +
                    "  WHERE lk.name = ? AND lv.value = ?\n" +
                    ")",
                    java.util.List.of(labelName, value)
                );

            case NOT_EQUAL ->
                new SQLFragment(
                    "mi.label_set_id NOT IN (\n" +
                    "  SELECT lsm.label_set_id FROM label_set_membership lsm\n" +
                    "  JOIN label_key lk ON lk.id = lsm.label_key_id\n" +
                    "  JOIN label_value lv ON lv.id = lsm.label_value_id\n" +
                    "  WHERE lk.name = ? AND lv.value = ?\n" +
                    ")",
                    java.util.List.of(labelName, value)
                );

            case REGEX ->
                // Use REGEXP function (requires RegexHelper.enableRegex() on connection)
                new SQLFragment(
                    "mi.label_set_id IN (\n" +
                    "  SELECT lsm.label_set_id FROM label_set_membership lsm\n" +
                    "  JOIN label_key lk ON lk.id = lsm.label_key_id\n" +
                    "  JOIN label_value lv ON lv.id = lsm.label_value_id\n" +
                    "  WHERE lk.name = ? AND lv.value REGEXP ?\n" +
                    ")",
                    java.util.List.of(labelName, value)
                );

            case NOT_REGEX ->
                // Negative regex matching
                new SQLFragment(
                    "mi.label_set_id NOT IN (\n" +
                    "  SELECT lsm.label_set_id FROM label_set_membership lsm\n" +
                    "  JOIN label_key lk ON lk.id = lsm.label_key_id\n" +
                    "  JOIN label_value lv ON lv.id = lsm.label_value_id\n" +
                    "  WHERE lk.name = ? AND lv.value REGEXP ?\n" +
                    ")",
                    java.util.List.of(labelName, value)
                );
        };
    }

    @Override
    public String toString() {
        String op = switch (matchType) {
            case EQUAL -> "=";
            case NOT_EQUAL -> "!=";
            case REGEX -> "=~";
            case NOT_REGEX -> "!~";
        };
        return labelName + op + "\"" + value + "\"";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelMatcher that = (LabelMatcher) o;
        return labelName.equals(that.labelName) &&
               value.equals(that.value) &&
               matchType == that.matchType;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * labelName.hashCode() + value.hashCode()) + matchType.hashCode();
    }
}
