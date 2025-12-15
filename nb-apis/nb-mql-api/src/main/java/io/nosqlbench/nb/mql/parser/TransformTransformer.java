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
 * Transforms MetricsQL mathematical transform functions to SQL.
 *
 * <p>Handles functions that transform values without changing cardinality:</p>
 * <ul>
 *   <li>abs() - Absolute value</li>
 *   <li>ceil() - Ceiling (round up)</li>
 *   <li>floor() - Floor (round down)</li>
 *   <li>round() - Round to nearest integer</li>
 *   <li>ln() - Natural logarithm</li>
 *   <li>log2() - Log base 2</li>
 *   <li>log10() - Log base 10</li>
 *   <li>sqrt() - Square root</li>
 *   <li>exp() - Exponential (e^x)</li>
 * </ul>
 *
 * <p>Phase 5 Implementation: Mathematical transforms pushed to SQLite</p>
 */
public class TransformTransformer {
    private static final Logger logger = LoggerFactory.getLogger(TransformTransformer.class);

    /**
     * Supported transform function types.
     */
    public enum TransformType {
        ABS,     // Absolute value
        CEIL,    // Ceiling
        FLOOR,   // Floor
        ROUND,   // Round
        LN,      // Natural logarithm
        LOG2,    // Log base 2
        LOG10,   // Log base 10
        SQRT,    // Square root
        EXP      // Exponential
    }

    /**
     * Transforms a mathematical transform function to SQL.
     * Wraps the input expression's value column in a SQL function.
     *
     * @param transformType The type of transform function
     * @param inputSQL The SQL fragment for the input expression
     * @return SQL fragment with transform applied
     */
    public SQLFragment transformMathFunction(TransformType transformType, SQLFragment inputSQL) {
        logger.debug("Transforming math function: {}", transformType);

        String sqlFunc = getSQLFunction(transformType);

        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>(inputSQL.getParameters());

        // Wrap the input query in a CTE and apply the transform
        sql.append("WITH input_data AS (\n");
        sql.append("  ").append(inputSQL.getSql().replace("\n", "\n  ")).append("\n");
        sql.append(")\n");
        sql.append("SELECT\n");
        sql.append("  timestamp,\n");
        sql.append("  ").append(sqlFunc).append("(value) AS value,\n");
        sql.append("  labels\n");
        sql.append("FROM input_data\n");
        sql.append("ORDER BY timestamp, labels");

        return new SQLFragment(sql.toString(), parameters);
    }

    /**
     * Returns the SQL function name for the given transform type.
     * All these functions are built into SQLite.
     */
    private String getSQLFunction(TransformType transformType) {
        return switch (transformType) {
            case ABS -> "ABS";
            case CEIL -> "CEIL";
            case FLOOR -> "FLOOR";
            case ROUND -> "ROUND";
            case LN -> "LN";
            case LOG2 -> "LOG2";
            case LOG10 -> "LOG10";
            case SQRT -> "SQRT";
            case EXP -> "EXP";
        };
    }
}
