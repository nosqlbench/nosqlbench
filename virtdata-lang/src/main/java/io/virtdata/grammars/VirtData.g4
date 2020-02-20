grammar VirtData;
// https://www.youtube.com/watch?v=eW4WFgRtFeY

virtdataRecipe : virtdataFlow (specend virtdataFlow?)* EOF ;

virtdataFlow : (COMPOSE)? expression (';' expression?)* ;

expression : (lvalue ASSIGN)? virtdataCall ;

virtdataCall :
 ( inputType TYPEARROW )?
 ( funcName '(' (arg (',' arg )* )? ')' )
 ( TYPEARROW outputType )?
 ;

lvalue : ID;
inputType : ID;
funcName: ID;
outputType : ID;

arg : ( value | virtdataCall | ref );
ref : ('$' ID );
value : ( floatValue | doubleValue | integerValue | longValue | stringValue | booleanValue);
stringValue : SSTRING_LITERAL | DSTRING_LITERAL ;
longValue: LONG;
doubleValue: DOUBLE;
integerValue: INTEGER;
floatValue: FLOAT;
booleanValue: BOOLEAN;

LONG : '-'? INT ('l'|'L') ;
DOUBLE    :   ('-'? INT '.' '0'* INT EXP? | '-'? INT EXP | '-'? INT ) ('d'|'D') ;
INTEGER : '-'? INT ;
FLOAT
    :    '-'? INT '.' ZINT EXP?   // 1.35, 1.35E-9, 0.3, -4.5
    |   '-'? INT EXP            // 1e10 -3e4
    |   '-'? INT    // -3, 45
    ;
BOOLEAN : 'true' | 'false';

fragment INT :   '0' | [1-9] [0-9]* ; // no leading zeros
fragment ZINT : [0-9]* ; // leading zeroes
fragment EXP :   [Ee] [+\-]? INT ;

specend: ( ';;' NEWLINE+ ) | ';;' | NEWLINE+ ;

NEWLINE   : '\r' '\n' | '\n' | '\r';

COMPOSE: 'compose' ;
TYPEARROW: '->' ;
ASSIGN: '=';
SSTRING_LITERAL : '\'' (~('\'' | '\\' | '\r' | '\n') | '\\' ('\'' | '\\' | . ))* '\'';
DSTRING_LITERAL : '"' (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\' | .))* '"';

// IDs can start with letters, contain numbers, dashes, and
// underscores, but not end with a dash
ID: IDPART ('.' IDPART)* ;
IDPART:  ( ( [a-zA-Z] [0-9a-zA-Z_]* )
 | ( [a-zA-Z] [0-9a-zA-Z_]* '-' [0-9a-zA-Z_]) )
 ;

// include form feed
WS : [\u000C \t\n]+ -> channel(HIDDEN);
// NL : [\r\nu000C]
