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

import io.nosqlbench.nb.mql.generated.MetricsQLParser;
import io.nosqlbench.nb.mql.generated.MetricsQLParserBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms MetricsQL parsed AST into SQL queries for SQLite.
 * This visitor walks the parse tree and generates appropriate SQL fragments.
 *
 * <p>Phase 2 Implementation: Basic selectors with SQL generation</p>
 */
public class MetricsQLTransformer extends MetricsQLParserBaseVisitor<SQLFragment> {
    private static final Logger logger = LoggerFactory.getLogger(MetricsQLTransformer.class);

    private final SelectorTransformer selectorTransformer = new SelectorTransformer();

    /**
     * Visit the top-level query node
     */
    @Override
    public SQLFragment visitQuery(MetricsQLParser.QueryContext ctx) {
        logger.debug("Transforming query: {}", ctx.getText());
        return visit(ctx.expression());
    }

    /**
     * Visit a metric selector: metric_name{label="value"}[5m]
     */
    @Override
    public SQLFragment visitMetricSelector(MetricsQLParser.MetricSelectorContext ctx) {
        String metricName = ctx.selector().metricName().getText();
        logger.debug("Processing selector for metric: {}", metricName);

        // Parse label matchers
        List<LabelMatcher> labelMatchers = new ArrayList<>();
        if (ctx.selector().labelMatchers() != null) {
            var matchers = ctx.selector().labelMatchers().labelMatcher();
            for (var matcher : matchers) {
                String labelName = matcher.IDENTIFIER().getText();
                String value = unquoteString(matcher.STRING().getText());
                LabelMatcher.MatchType matchType = getMatchType(matcher.matchOp());

                labelMatchers.add(new LabelMatcher(labelName, value, matchType));
            }
        }

        // Parse time range if present
        String timeRange = null;
        if (ctx.selector().timeRange() != null) {
            timeRange = ctx.selector().timeRange().DURATION().getText();
        }

        // Transform to SQL
        return selectorTransformer.transformSelector(metricName, labelMatchers, timeRange);
    }

    /**
     * Determines the match type from a matchOp context
     */
    private LabelMatcher.MatchType getMatchType(MetricsQLParser.MatchOpContext matchOp) {
        if (matchOp instanceof MetricsQLParser.MatchEqualContext) {
            return LabelMatcher.MatchType.EQUAL;
        } else if (matchOp instanceof MetricsQLParser.MatchNotEqualContext) {
            return LabelMatcher.MatchType.NOT_EQUAL;
        } else if (matchOp instanceof MetricsQLParser.MatchRegexContext) {
            return LabelMatcher.MatchType.REGEX;
        } else if (matchOp instanceof MetricsQLParser.MatchNotRegexContext) {
            return LabelMatcher.MatchType.NOT_REGEX;
        }
        throw new IllegalArgumentException("Unknown match operator type: " + matchOp.getClass().getName());
    }

    /**
     * Removes quotes from a string literal
     */
    private String unquoteString(String quotedString) {
        if (quotedString.length() < 2) {
            return quotedString;
        }
        // Remove first and last character (quotes)
        return quotedString.substring(1, quotedString.length() - 1);
    }

    private final RollupTransformer rollupTransformer = new RollupTransformer();
    private final AggregationTransformer aggregationTransformer = new AggregationTransformer();
    private final TransformTransformer transformTransformer = new TransformTransformer();
    private final BinaryOpTransformer binaryOpTransformer = new BinaryOpTransformer();
    private final LabelManipulationTransformer labelManipulationTransformer = new LabelManipulationTransformer();

    /**
     * Visit a function call: rate(metric[5m]) or abs(metric)
     * Phase 3-5: Implements rollup and transform functions
     */
    @Override
    public SQLFragment visitFunction(MetricsQLParser.FunctionContext ctx) {
        String functionName = ctx.functionCall().IDENTIFIER().getText().toLowerCase();
        logger.debug("Processing function: {}", functionName);

        // Get function arguments
        if (ctx.functionCall().argumentList() == null ||
            ctx.functionCall().argumentList().expression().isEmpty()) {
            throw new IllegalArgumentException(
                "Function " + functionName + " requires arguments");
        }

        // Check if this is a label manipulation function
        if (isLabelManipulationFunction(functionName)) {
            return handleLabelManipulation(functionName, ctx);
        }

        // Check if this is a transform function
        TransformTransformer.TransformType transformType = getTransformType(functionName);
        if (transformType != null) {
            // Transform function: abs(expr), ceil(expr), etc.
            var argCtx = ctx.functionCall().argumentList().expression(0);
            SQLFragment inputSQL = visit(argCtx);
            return transformTransformer.transformMathFunction(transformType, inputSQL);
        }

        // Otherwise, it's a rollup function
        // For rollup functions, argument must be a metric selector with time range
        var argCtx = ctx.functionCall().argumentList().expression(0);

        if (!(argCtx instanceof MetricsQLParser.MetricSelectorContext)) {
            throw new IllegalArgumentException(
                "Function " + functionName + " requires a metric selector as argument. " +
                "Example: " + functionName + "(metric_name[5m])");
        }

        MetricsQLParser.MetricSelectorContext selectorCtx =
            (MetricsQLParser.MetricSelectorContext) argCtx;

        // Extract selector components
        String metricName = selectorCtx.selector().metricName().getText();

        // Parse label matchers
        List<LabelMatcher> labelMatchers = new ArrayList<>();
        if (selectorCtx.selector().labelMatchers() != null) {
            var matchers = selectorCtx.selector().labelMatchers().labelMatcher();
            for (var matcher : matchers) {
                String labelName = matcher.IDENTIFIER().getText();
                String value = unquoteString(matcher.STRING().getText());
                LabelMatcher.MatchType matchType = getMatchType(matcher.matchOp());
                labelMatchers.add(new LabelMatcher(labelName, value, matchType));
            }
        }

        // Extract time range
        String timeRange = null;
        if (selectorCtx.selector().timeRange() != null) {
            timeRange = selectorCtx.selector().timeRange().DURATION().getText();
        }

        // Handle quantile_over_time separately as it takes 2 arguments
        if (functionName.equals("quantile_over_time")) {
            // quantile_over_time(0.95, metric[5m]) - first arg is quantile value
            if (ctx.functionCall().argumentList().expression().size() < 2) {
                throw new IllegalArgumentException(
                    "quantile_over_time requires 2 arguments: quantile value and metric selector. " +
                    "Example: quantile_over_time(0.95, metric[5m])");
            }

            // First argument should be a number (quantile value)
            var quantileArgCtx = ctx.functionCall().argumentList().expression(0);
            if (!(quantileArgCtx instanceof MetricsQLParser.LiteralExprContext)) {
                throw new IllegalArgumentException(
                    "First argument to quantile_over_time must be a number between 0.0 and 1.0");
            }

            var literalCtx = (MetricsQLParser.LiteralExprContext) quantileArgCtx;
            if (!(literalCtx.literal() instanceof MetricsQLParser.NumberLiteralContext)) {
                throw new IllegalArgumentException(
                    "First argument to quantile_over_time must be a number between 0.0 and 1.0");
            }

            double quantileValue = Double.parseDouble(
                ((MetricsQLParser.NumberLiteralContext) literalCtx.literal()).NUMBER().getText());

            // Second argument is the metric selector
            var metricArgCtx = ctx.functionCall().argumentList().expression(1);
            if (!(metricArgCtx instanceof MetricsQLParser.MetricSelectorContext)) {
                throw new IllegalArgumentException(
                    "Second argument to quantile_over_time must be a metric selector with time range");
            }

            MetricsQLParser.MetricSelectorContext quantileSelectorCtx =
                (MetricsQLParser.MetricSelectorContext) metricArgCtx;

            String quantileMetricName = quantileSelectorCtx.selector().metricName().getText();

            List<LabelMatcher> quantileLabelMatchers = new ArrayList<>();
            if (quantileSelectorCtx.selector().labelMatchers() != null) {
                var matchers = quantileSelectorCtx.selector().labelMatchers().labelMatcher();
                for (var matcher : matchers) {
                    String labelName = matcher.IDENTIFIER().getText();
                    String value = unquoteString(matcher.STRING().getText());
                    LabelMatcher.MatchType matchType = getMatchType(matcher.matchOp());
                    quantileLabelMatchers.add(new LabelMatcher(labelName, value, matchType));
                }
            }

            String quantileTimeRange = null;
            if (quantileSelectorCtx.selector().timeRange() != null) {
                quantileTimeRange = quantileSelectorCtx.selector().timeRange().DURATION().getText();
            }

            return rollupTransformer.transformRollup(
                RollupTransformer.RollupType.QUANTILE_OVER_TIME,
                quantileMetricName, quantileLabelMatchers, quantileTimeRange, null, quantileValue);
        }

        // Map function name to rollup type
        RollupTransformer.RollupType rollupType = switch (functionName) {
            case "rate" -> RollupTransformer.RollupType.RATE;
            case "increase" -> RollupTransformer.RollupType.INCREASE;
            case "avg_over_time" -> RollupTransformer.RollupType.AVG_OVER_TIME;
            case "sum_over_time" -> RollupTransformer.RollupType.SUM_OVER_TIME;
            case "min_over_time" -> RollupTransformer.RollupType.MIN_OVER_TIME;
            case "max_over_time" -> RollupTransformer.RollupType.MAX_OVER_TIME;
            case "count_over_time" -> RollupTransformer.RollupType.COUNT_OVER_TIME;
            default -> throw new IllegalArgumentException(
                "Unsupported function: " + functionName + ". " +
                "Supported rollup functions: rate, increase, avg_over_time, sum_over_time, " +
                "min_over_time, max_over_time, count_over_time, quantile_over_time. " +
                "Supported transform functions: abs, ceil, floor, round, ln, log2, log10, sqrt, exp");
        };

        // Transform using RollupTransformer
        return rollupTransformer.transformRollup(
            rollupType, metricName, labelMatchers, timeRange, null);
    }

    /**
     * Maps function name to transform type, or returns null if not a transform function.
     */
    private TransformTransformer.TransformType getTransformType(String functionName) {
        return switch (functionName) {
            case "abs" -> TransformTransformer.TransformType.ABS;
            case "ceil" -> TransformTransformer.TransformType.CEIL;
            case "floor" -> TransformTransformer.TransformType.FLOOR;
            case "round" -> TransformTransformer.TransformType.ROUND;
            case "ln" -> TransformTransformer.TransformType.LN;
            case "log2" -> TransformTransformer.TransformType.LOG2;
            case "log10" -> TransformTransformer.TransformType.LOG10;
            case "sqrt" -> TransformTransformer.TransformType.SQRT;
            case "exp" -> TransformTransformer.TransformType.EXP;
            default -> null;  // Not a transform function
        };
    }

    /**
     * Checks if function name is a label manipulation function.
     */
    private boolean isLabelManipulationFunction(String functionName) {
        return switch (functionName) {
            case "label_set", "label_del", "label_keep",
                 "label_copy", "label_move", "label_replace" -> true;
            default -> false;
        };
    }

    /**
     * Handles label manipulation functions.
     */
    private SQLFragment handleLabelManipulation(String functionName,
                                               MetricsQLParser.FunctionContext ctx) {
        var args = ctx.functionCall().argumentList().expression();

        // First argument is always the metric expression
        SQLFragment inputSQL = visit(args.get(0));

        return switch (functionName) {
            case "label_set" -> {
                // label_set(metric, "key", "value")
                if (args.size() != 3) {
                    throw new IllegalArgumentException(
                        "label_set requires 3 arguments: metric, label_name, label_value");
                }
                String key = extractStringLiteral(args.get(1));
                String value = extractStringLiteral(args.get(2));
                yield labelManipulationTransformer.labelSet(inputSQL, key, value);
            }
            case "label_del" -> {
                // label_del(metric, "label1", "label2", ...)
                if (args.size() < 2) {
                    throw new IllegalArgumentException(
                        "label_del requires at least 2 arguments: metric and label names to delete");
                }
                List<String> labelsToDelete = new ArrayList<>();
                for (int i = 1; i < args.size(); i++) {
                    labelsToDelete.add(extractStringLiteral(args.get(i)));
                }
                yield labelManipulationTransformer.labelDel(inputSQL, labelsToDelete);
            }
            case "label_keep" -> {
                // label_keep(metric, "label1", "label2", ...)
                if (args.size() < 2) {
                    throw new IllegalArgumentException(
                        "label_keep requires at least 2 arguments: metric and label names to keep");
                }
                List<String> labelsToKeep = new ArrayList<>();
                for (int i = 1; i < args.size(); i++) {
                    labelsToKeep.add(extractStringLiteral(args.get(i)));
                }
                yield labelManipulationTransformer.labelKeep(inputSQL, labelsToKeep);
            }
            case "label_copy" -> {
                // label_copy(metric, "src", "dst")
                if (args.size() != 3) {
                    throw new IllegalArgumentException(
                        "label_copy requires 3 arguments: metric, source_label, destination_label");
                }
                String src = extractStringLiteral(args.get(1));
                String dst = extractStringLiteral(args.get(2));
                yield labelManipulationTransformer.labelCopy(inputSQL, src, dst);
            }
            case "label_move" -> {
                // label_move(metric, "src", "dst")
                if (args.size() != 3) {
                    throw new IllegalArgumentException(
                        "label_move requires 3 arguments: metric, source_label, destination_label");
                }
                String src = extractStringLiteral(args.get(1));
                String dst = extractStringLiteral(args.get(2));
                yield labelManipulationTransformer.labelMove(inputSQL, src, dst);
            }
            case "label_replace" -> {
                // label_replace(metric, "dst", "replacement", "src", "regex")
                if (args.size() != 5) {
                    throw new IllegalArgumentException(
                        "label_replace requires 5 arguments: metric, dst_label, replacement, src_label, regex");
                }
                String dst = extractStringLiteral(args.get(1));
                String replacement = extractStringLiteral(args.get(2));
                String src = extractStringLiteral(args.get(3));
                String regex = extractStringLiteral(args.get(4));
                yield labelManipulationTransformer.labelReplace(inputSQL, dst, replacement, src, regex);
            }
            default -> throw new IllegalArgumentException("Unknown label manipulation function: " + functionName);
        };
    }

    /**
     * Extracts string value from a string literal expression.
     */
    private String extractStringLiteral(MetricsQLParser.ExpressionContext exprCtx) {
        if (!(exprCtx instanceof MetricsQLParser.LiteralExprContext)) {
            throw new IllegalArgumentException("Expected string literal argument");
        }

        var literalCtx = (MetricsQLParser.LiteralExprContext) exprCtx;
        if (!(literalCtx.literal() instanceof MetricsQLParser.StringLiteralContext)) {
            throw new IllegalArgumentException("Expected string literal argument");
        }

        String quotedString = ((MetricsQLParser.StringLiteralContext) literalCtx.literal())
            .STRING().getText();
        return unquoteString(quotedString);
    }

    /**
     * Visit an aggregation expression: sum(metric) by (label)
     * Phase 4: Implements aggregations with GROUP BY
     */
    @Override
    public SQLFragment visitAggregation(MetricsQLParser.AggregationContext ctx) {
        String aggFuncStr = ctx.aggregationExpr().aggregationFunc().getText().toUpperCase();
        logger.debug("Processing aggregation: {}", aggFuncStr);

        // Map aggregation function name to type
        AggregationTransformer.AggregationType aggType = switch (aggFuncStr) {
            case "SUM" -> AggregationTransformer.AggregationType.SUM;
            case "AVG" -> AggregationTransformer.AggregationType.AVG;
            case "MIN" -> AggregationTransformer.AggregationType.MIN;
            case "MAX" -> AggregationTransformer.AggregationType.MAX;
            case "COUNT" -> AggregationTransformer.AggregationType.COUNT;
            case "STDDEV", "STDVAR" -> AggregationTransformer.AggregationType.STDDEV;
            default -> throw new IllegalArgumentException(
                "Unsupported aggregation function: " + aggFuncStr + ". " +
                "Supported functions: SUM, AVG, MIN, MAX, COUNT, STDDEV, STDVAR");
        };

        // Get the expression to aggregate (must be a metric selector)
        var exprCtx = ctx.aggregationExpr().expression();
        if (!(exprCtx instanceof MetricsQLParser.MetricSelectorContext)) {
            throw new IllegalArgumentException(
                "Aggregation function " + aggFuncStr + " requires a metric selector as argument. " +
                "Example: " + aggFuncStr.toLowerCase() + "(metric_name)");
        }

        MetricsQLParser.MetricSelectorContext selectorCtx =
            (MetricsQLParser.MetricSelectorContext) exprCtx;

        // Extract metric name
        String metricName = selectorCtx.selector().metricName().getText();

        // Parse label matchers
        List<LabelMatcher> labelMatchers = new ArrayList<>();
        if (selectorCtx.selector().labelMatchers() != null) {
            var matchers = selectorCtx.selector().labelMatchers().labelMatcher();
            for (var matcher : matchers) {
                String labelName = matcher.IDENTIFIER().getText();
                String value = unquoteString(matcher.STRING().getText());
                LabelMatcher.MatchType matchType = getMatchType(matcher.matchOp());
                labelMatchers.add(new LabelMatcher(labelName, value, matchType));
            }
        }

        // Parse aggregation modifier (by/without)
        AggregationTransformer.ModifierType modifierType = AggregationTransformer.ModifierType.NONE;
        List<String> groupingLabels = new ArrayList<>();

        if (ctx.aggregationExpr().aggregationModifier() != null) {
            var modifier = ctx.aggregationExpr().aggregationModifier();
            if (modifier.BY() != null) {
                modifierType = AggregationTransformer.ModifierType.BY;
            } else if (modifier.WITHOUT() != null) {
                modifierType = AggregationTransformer.ModifierType.WITHOUT;
            }

            // Extract label list
            if (modifier.labelList() != null) {
                for (var labelToken : modifier.labelList().IDENTIFIER()) {
                    groupingLabels.add(labelToken.getText());
                }
            }
        }

        // Transform using AggregationTransformer
        return aggregationTransformer.transformAggregation(
            aggType, metricName, labelMatchers, modifierType, groupingLabels);
    }

    /**
     * Visit binary arithmetic operations: expr + expr, expr - expr
     * Phase 7: Implements arithmetic binary operations
     */
    @Override
    public SQLFragment visitBinaryAddSub(MetricsQLParser.BinaryAddSubContext ctx) {
        BinaryOpTransformer.BinaryOpType opType = ctx.ADD() != null ?
            BinaryOpTransformer.BinaryOpType.ADD :
            BinaryOpTransformer.BinaryOpType.SUBTRACT;

        return transformBinaryOperation(opType, ctx.expression(0), ctx.expression(1));
    }

    /**
     * Visit binary arithmetic operations: expr * expr, expr / expr, expr % expr
     * Phase 7: Implements arithmetic binary operations
     */
    @Override
    public SQLFragment visitBinaryMulDiv(MetricsQLParser.BinaryMulDivContext ctx) {
        BinaryOpTransformer.BinaryOpType opType;
        if (ctx.MUL() != null) {
            opType = BinaryOpTransformer.BinaryOpType.MULTIPLY;
        } else if (ctx.DIV() != null) {
            opType = BinaryOpTransformer.BinaryOpType.DIVIDE;
        } else {
            opType = BinaryOpTransformer.BinaryOpType.MODULO;
        }

        return transformBinaryOperation(opType, ctx.expression(0), ctx.expression(1));
    }

    /**
     * Visit comparison operations: expr == expr, expr != expr, etc.
     * Phase 7: Implements comparison binary operations
     */
    @Override
    public SQLFragment visitBinaryComparison(MetricsQLParser.BinaryComparisonContext ctx) {
        BinaryOpTransformer.BinaryOpType opType;
        if (ctx.EQ() != null) {
            opType = BinaryOpTransformer.BinaryOpType.EQUAL;
        } else if (ctx.NE() != null) {
            opType = BinaryOpTransformer.BinaryOpType.NOT_EQUAL;
        } else if (ctx.LT() != null) {
            opType = BinaryOpTransformer.BinaryOpType.LESS_THAN;
        } else if (ctx.GT() != null) {
            opType = BinaryOpTransformer.BinaryOpType.GREATER_THAN;
        } else if (ctx.LTE() != null) {
            opType = BinaryOpTransformer.BinaryOpType.LESS_OR_EQUAL;
        } else {
            opType = BinaryOpTransformer.BinaryOpType.GREATER_OR_EQUAL;
        }

        return transformBinaryOperation(opType, ctx.expression(0), ctx.expression(1));
    }

    /**
     * Visit logical operations: expr and expr, expr or expr
     * Phase 7: Implements set operations
     */
    @Override
    public SQLFragment visitBinaryLogical(MetricsQLParser.BinaryLogicalContext ctx) {
        BinaryOpTransformer.BinaryOpType opType;
        if (ctx.OR() != null) {
            opType = BinaryOpTransformer.BinaryOpType.OR;
        } else {
            opType = BinaryOpTransformer.BinaryOpType.UNLESS;
        }

        return transformBinaryOperation(opType, ctx.expression(0), ctx.expression(1));
    }

    /**
     * Visit AND operations: expr and expr
     * Phase 7: Implements set operations
     */
    @Override
    public SQLFragment visitBinaryAnd(MetricsQLParser.BinaryAndContext ctx) {
        return transformBinaryOperation(
            BinaryOpTransformer.BinaryOpType.AND,
            ctx.expression(0),
            ctx.expression(1));
    }

    /**
     * Helper method to transform any binary operation.
     */
    private SQLFragment transformBinaryOperation(BinaryOpTransformer.BinaryOpType opType,
                                                 MetricsQLParser.ExpressionContext leftCtx,
                                                 MetricsQLParser.ExpressionContext rightCtx) {
        logger.debug("Transforming binary operation: {}", opType);

        // Visit left and right expressions
        SQLFragment leftSQL = visit(leftCtx);
        SQLFragment rightSQL = visit(rightCtx);

        // Check if right side is a scalar (number literal)
        boolean isRightScalar = rightCtx instanceof MetricsQLParser.LiteralExprContext;

        return binaryOpTransformer.transformBinaryOp(opType, leftSQL, rightSQL, isRightScalar);
    }

    /**
     * Visit literal values
     * Phase 2: Placeholder, literals will be used in later phases
     */
    @Override
    public SQLFragment visitNumberLiteral(MetricsQLParser.NumberLiteralContext ctx) {
        return new SQLFragment("SELECT " + ctx.NUMBER().getText() + " AS value");
    }

    @Override
    public SQLFragment visitStringLiteral(MetricsQLParser.StringLiteralContext ctx) {
        String value = unquoteString(ctx.STRING().getText());
        return SQLFragment.withParam("SELECT ? AS value", value);
    }

    /**
     * Visit parenthesized expressions
     */
    @Override
    public SQLFragment visitParenExpr(MetricsQLParser.ParenExprContext ctx) {
        return visit(ctx.expression()).parenthesize();
    }

    /**
     * Default behavior: visit all children and return first non-null result
     */
    @Override
    protected SQLFragment aggregateResult(SQLFragment aggregate, SQLFragment nextResult) {
        if (aggregate == null) {
            return nextResult;
        }
        if (nextResult == null) {
            return aggregate;
        }
        return aggregate.append(nextResult);
    }
}
