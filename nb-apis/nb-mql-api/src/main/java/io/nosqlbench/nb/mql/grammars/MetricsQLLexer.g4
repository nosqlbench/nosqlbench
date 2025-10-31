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

lexer grammar MetricsQLLexer;

// Keywords and operators (case-insensitive)
BY: [Bb][Yy];
WITHOUT: [Ww][Ii][Tt][Hh][Oo][Uu][Tt];
AND: [Aa][Nn][Dd];
OR: [Oo][Rr];
UNLESS: [Uu][Nn][Ll][Ee][Ss][Ss];
ON: [Oo][Nn];
IGNORING: [Ii][Gg][Nn][Oo][Rr][Ii][Nn][Gg];
GROUP_LEFT: [Gg][Rr][Oo][Uu][Pp]'_'[Ll][Ee][Ff][Tt];
GROUP_RIGHT: [Gg][Rr][Oo][Uu][Pp]'_'[Rr][Ii][Gg][Hh][Tt];
BOOL: [Bb][Oo][Oo][Ll];

// Aggregation functions (for syntax highlighting and parsing)
SUM: [Ss][Uu][Mm];
AVG: [Aa][Vv][Gg];
MIN: [Mm][Ii][Nn];
MAX: [Mm][Aa][Xx];
COUNT: [Cc][Oo][Uu][Nn][Tt];
STDDEV: [Ss][Tt][Dd][Dd][Ee][Vv];
STDVAR: [Ss][Tt][Dd][Vv][Aa][Rr];
QUANTILE: [Qq][Uu][Aa][Nn][Tt][Ii][Ll][Ee];

// Comparison operators
EQ: '==';
NE: '!=';
LTE: '<=';
GTE: '>=';
LT: '<';
GT: '>';

// Label matcher operators
LABEL_EQ: '=';
LABEL_REGEX: '=~';
LABEL_NOT_REGEX: '!~';

// Arithmetic operators
ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';
MOD: '%';
POW: '^';

// Delimiters
LPAREN: '(';
RPAREN: ')';
LBRACE: '{';
RBRACE: '}';
LBRACKET: '[';
RBRACKET: ']';
COMMA: ',';
COLON: ':';

// Literals
NUMBER
    : [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?
    ;

// Duration literal (e.g., 5m, 1h, 30s)
DURATION
    : [0-9]+ [a-zA-Z]+
    ;

// String literal (single or double quoted)
STRING
    : '"' ( '\\"' | ~[\\"\r\n] )* '"'
    | '\'' ( '\\\'' | ~[\\'\r\n] )* '\''
    ;

// Identifiers (metric names, label names, function names)
IDENTIFIER
    : [a-zA-Z_:] [a-zA-Z0-9_:]*
    ;

// Whitespace (skip)
WS
    : [ \t\r\n]+ -> skip
    ;

// Comments (skip)
LINE_COMMENT
    : '#' ~[\r\n]* -> skip
    ;
