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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms MetricsQL binary operations to SQL.
 *
 * <p>Handles three types of binary operations:</p>
 * <ul>
 *   <li>Arithmetic: +, -, *, /, % (modulo)</li>
 *   <li>Comparison: ==, !=, <, >, <=, >=</li>
 *   <li>Set operations: and, or, unless</li>
 * </ul>
 *
 * <p>Supports both metric-to-metric and metric-to-scalar operations.</p>
 *
 * <p>Phase 7 Implementation: Binary operations with metric joining</p>
 */
public class BinaryOpTransformer {
    private static final Logger logger = LoggerFactory.getLogger(BinaryOpTransformer.class);

    /**
     * Binary operation types.
     */
    public enum BinaryOpType {
        // Arithmetic
        ADD,       // +
        SUBTRACT,  // -
        MULTIPLY,  // *
        DIVIDE,    // /
        MODULO,    // %

        // Comparison
        EQUAL,     // ==
        NOT_EQUAL, // !=
        LESS_THAN, // <
        GREATER_THAN, // >
        LESS_OR_EQUAL, // <=
        GREATER_OR_EQUAL, // >=

        // Set operations
        AND,       // and
        OR,        // or
        UNLESS     // unless
    }

    /**
     * Transforms a binary operation between two expressions.
     *
     * @param opType The operation type
     * @param leftSQL SQL for left operand
     * @param rightSQL SQL for right operand
     * @param isRightScalar True if right operand is a scalar constant
     * @return SQL fragment with binary operation
     */
    public SQLFragment transformBinaryOp(BinaryOpType opType,
                                        SQLFragment leftSQL,
                                        SQLFragment rightSQL,
                                        boolean isRightScalar) {
        logger.debug("Transforming binary operation: {}, rightScalar={}", opType, isRightScalar);

        if (isRightScalar) {
            return transformScalarOp(opType, leftSQL, rightSQL);
        } else {
            return transformMetricOp(opType, leftSQL, rightSQL);
        }
    }

    /**
     * Transforms scalar operations: metric + 100
     */
    private SQLFragment transformScalarOp(BinaryOpType opType,
                                         SQLFragment metricSQL,
                                         SQLFragment scalarSQL) {
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        parameters.addAll(metricSQL.getParameters());
        parameters.addAll(scalarSQL.getParameters());

        String operator = getSQLOperator(opType);

        // Wrap metric query in CTE
        sql.append("WITH metric_data AS (\n");
        sql.append("  ").append(metricSQL.getSql().replace("\n", "\n  ")).append("\n");
        sql.append("),\n");

        // Get scalar value
        sql.append("scalar_value AS (\n");
        sql.append("  ").append(scalarSQL.getSql().replace("\n", "\n  ")).append("\n");
        sql.append(")\n");

        // Apply operation
        sql.append("SELECT\n");
        sql.append("  metric_data.timestamp,\n");
        sql.append("  metric_data.value ").append(operator).append(" scalar_value.value AS value,\n");
        sql.append("  metric_data.labels\n");
        sql.append("FROM metric_data, scalar_value\n");
        sql.append("ORDER BY timestamp, labels");

        return new SQLFragment(sql.toString(), parameters);
    }

    /**
     * Transforms metric-to-metric operations: metric1 + metric2
     * Joins two metric queries on timestamp and labels.
     */
    private SQLFragment transformMetricOp(BinaryOpType opType,
                                         SQLFragment leftSQL,
                                         SQLFragment rightSQL) {
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        parameters.addAll(leftSQL.getParameters());
        parameters.addAll(rightSQL.getParameters());

        String operator = getSQLOperator(opType);

        // Wrap both queries in CTEs
        sql.append("WITH left_data AS (\n");
        sql.append("  ").append(leftSQL.getSql().replace("\n", "\n  ")).append("\n");
        sql.append("),\n");
        sql.append("right_data AS (\n");
        sql.append("  ").append(rightSQL.getSql().replace("\n", "\n  ")).append("\n");
        sql.append(")\n");

        // Join and apply operation
        if (opType == BinaryOpType.AND || opType == BinaryOpType.OR || opType == BinaryOpType.UNLESS) {
            // Set operations
            sql.append(buildSetOperation(opType));
        } else {
            // Arithmetic or comparison
            sql.append("SELECT\n");
            sql.append("  left_data.timestamp,\n");
            sql.append("  left_data.value ").append(operator).append(" right_data.value AS value,\n");
            sql.append("  left_data.labels\n");
            sql.append("FROM left_data\n");
            sql.append("JOIN right_data ON\n");
            sql.append("  left_data.timestamp = right_data.timestamp\n");
            sql.append("  AND left_data.labels = right_data.labels\n");
            sql.append("ORDER BY timestamp, labels");
        }

        return new SQLFragment(sql.toString(), parameters);
    }

    /**
     * Builds SQL for set operations (and, or, unless).
     */
    private String buildSetOperation(BinaryOpType opType) {
        StringBuilder sql = new StringBuilder();

        switch (opType) {
            case AND -> {
                // Return series that exist in both sets
                sql.append("SELECT\n");
                sql.append("  left_data.timestamp,\n");
                sql.append("  left_data.value,\n");
                sql.append("  left_data.labels\n");
                sql.append("FROM left_data\n");
                sql.append("WHERE EXISTS (\n");
                sql.append("  SELECT 1 FROM right_data\n");
                sql.append("  WHERE right_data.labels = left_data.labels\n");
                sql.append(")\n");
                sql.append("ORDER BY timestamp, labels");
            }
            case OR -> {
                // Return series from both sets (UNION)
                sql.append("SELECT timestamp, value, labels FROM left_data\n");
                sql.append("UNION\n");
                sql.append("SELECT timestamp, value, labels FROM right_data\n");
                sql.append("ORDER BY timestamp, labels");
            }
            case UNLESS -> {
                // Return series from left that don't exist in right
                sql.append("SELECT\n");
                sql.append("  left_data.timestamp,\n");
                sql.append("  left_data.value,\n");
                sql.append("  left_data.labels\n");
                sql.append("FROM left_data\n");
                sql.append("WHERE NOT EXISTS (\n");
                sql.append("  SELECT 1 FROM right_data\n");
                sql.append("  WHERE right_data.labels = left_data.labels\n");
                sql.append(")\n");
                sql.append("ORDER BY timestamp, labels");
            }
        }

        return sql.toString();
    }

    /**
     * Maps binary operation type to SQL operator.
     */
    private String getSQLOperator(BinaryOpType opType) {
        return switch (opType) {
            case ADD -> "+";
            case SUBTRACT -> "-";
            case MULTIPLY -> "*";
            case DIVIDE -> "/";
            case MODULO -> "%";
            case EQUAL -> "=";
            case NOT_EQUAL -> "!=";
            case LESS_THAN -> "<";
            case GREATER_THAN -> ">";
            case LESS_OR_EQUAL -> "<=";
            case GREATER_OR_EQUAL -> ">=";
            default -> throw new IllegalArgumentException(
                "Set operations (AND, OR, UNLESS) don't use simple operators");
        };
    }
}
