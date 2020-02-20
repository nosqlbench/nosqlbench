grammar MVv2;
// https://www.youtube.com/watch?v=eW4WFgRtFeY

mgRecipe : mgFlow (specend mgFlow?)* EOF ;

mgFlow : expression (';' expression?)* ;

expression : (lvalue ASSIGN)? mgCall ;

mgCall :
 ( inputType INPUTTYPE )?
 ( funcName '(' (arg (',' arg )* )? ')' )
 ( OUTPUTTYPE outputType )?
 ;

lvalue : ID;
inputType : ID;
funcName: ID;
outputType : ID;

arg : ( ref | mgCall | value );
ref : ('$' ID );
value : ( floatValue | integerValue | stringValue);
stringValue : SSTRING_LITERAL | DSTRING_LITERAL ;
floatValue: FLOAT;
integerValue: INTEGER;

FLOAT
    :   '-'? INT '.' INT EXP?   // 1.35, 1.35E-9, 0.3, -4.5
    |   '-'? INT EXP            // 1e10 -3e4
    |   '-'? INT                // -3, 45
    ;
INTEGER : INT ;

fragment INT :   '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP :   [Ee] [+\-]? INT ;

specend: ( ';;' NEWLINE+ ) | ';;' | NEWLINE+ ;

NEWLINE   : '\r' '\n' | '\n' | '\r';

OUTPUTTYPE: '->' ;
INPUTTYPE: '>-' ;
ASSIGN: '=';
SSTRING_LITERAL : '\'' (~('\'' | '\\' | '\r' | '\n') | '\\' ('\'' | '\\'))* '\'';
DSTRING_LITERAL : '"' (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\'))* '"';

// IDs can start with letters, contain numbers, dashes, and
// underscores, but not end with a dash
ID: IDPART ('.' IDPART)* ;
IDPART:  ( ( [a-zA-Z] [0-9a-zA-Z_]* )
 | ( [a-zA-Z] [0-9a-zA-Z_]* '-' [0-9a-zA-Z_]) )
 ;

WS : [\e\u000C ]+ -> channel(HIDDEN);
// NL : [\r\nu000C]
