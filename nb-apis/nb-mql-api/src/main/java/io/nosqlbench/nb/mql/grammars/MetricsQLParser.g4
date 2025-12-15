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

parser grammar MetricsQLParser;

options {
    tokenVocab=MetricsQLLexer;
}

// Entry point
query
    : expression EOF
    ;

// Expressions with precedence (lowest to highest)
expression
    : expression (OR | UNLESS) expression                          # binaryLogical
    | expression AND expression                                    # binaryAnd
    | expression (EQ | NE | LT | LTE | GT | GTE) expression       # binaryComparison
    | expression (ADD | SUB) expression                            # binaryAddSub
    | expression (MUL | DIV | MOD) expression                      # binaryMulDiv
    | expression POW expression                                    # binaryPow
    | aggregationExpr                                              # aggregation
    | functionCall                                                 # function
    | selector                                                     # metricSelector
    | literal                                                      # literalExpr
    | LPAREN expression RPAREN                                     # parenExpr
    ;

// Aggregation expressions: sum(metric) by (label)
aggregationExpr
    : aggregationFunc LPAREN expression RPAREN (aggregationModifier)?
    ;

aggregationFunc
    : SUM | AVG | MIN | MAX | COUNT | STDDEV | STDVAR | QUANTILE
    ;

aggregationModifier
    : BY LPAREN labelList RPAREN
    | WITHOUT LPAREN labelList RPAREN
    ;

// Function calls: rate(metric[5m]), quantile(0.95, metric)
functionCall
    : IDENTIFIER LPAREN (argumentList)? RPAREN
    ;

argumentList
    : expression (COMMA expression)*
    ;

// Metric selector: metric_name{label="value"}[5m]
selector
    : metricName (labelMatchers)? (timeRange)?
    ;

metricName
    : IDENTIFIER
    ;

// Label matchers: {job="api", status="200"}
labelMatchers
    : LBRACE (labelMatcher (COMMA labelMatcher)*)? RBRACE
    ;

labelMatcher
    : IDENTIFIER matchOp STRING
    ;

matchOp
    : LABEL_EQ         # matchEqual
    | NE               # matchNotEqual
    | LABEL_REGEX      # matchRegex
    | LABEL_NOT_REGEX  # matchNotRegex
    ;

// Time range: [5m]
timeRange
    : LBRACKET DURATION RBRACKET
    ;

// Label list for grouping: (label1, label2)
labelList
    : IDENTIFIER (COMMA IDENTIFIER)*
    ;

// Literals
literal
    : NUMBER    # numberLiteral
    | STRING    # stringLiteral
    ;
