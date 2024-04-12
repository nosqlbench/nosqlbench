grammar OpTemplate;
// https://www.youtube.com/watch?v=eW4WFgRtFeY

opTemplate : (modifiers)? template;

modifiers : '(' modifier? (',' modifier )* ')';

modifier : mname (':'|'=') mval;

mname: ID;

template: ( ~('\n'|'\r')+ ) NEWLINE;

ID: IDPART ('.' IDPART)* ;
IDPART:  ( ( [a-zA-Z] [0-9a-zA-Z_]* )
 | ( [a-zA-Z] [0-9a-zA-Z_]* '-' [0-9a-zA-Z_]) )
 ;

NEWLINE   : '\r' '\n' | '\n' | '\r';

mval : ( floatValue | doubleValue | integerValue | longValue | stringValue | booleanValue);

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

SSTRING_LITERAL : '\'' (~('\'' | '\\' | '\r' | '\n') | '\\' ('\'' | '\\' | . ))* '\'';
DSTRING_LITERAL : '"' (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\' | .))* '"';

WS : [\u000C \t\n]+ -> channel(HIDDEN);
