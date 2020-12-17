grammar Config;
// https://www.youtube.com/watch?v=eW4WFgRtFeY

config : OPEN_BRACE? assignments CLOSE_BRACE?;
scope : OPEN_BRACE assignments CLOSE_BRACE;
assignments : assignment? ( COMMA assignment )* ;
assignment :
  (ID OPEN_BRACE cval CLOSE_BRACE)
  | (ID ASSIGN cval)
  | ( ID ASSIGN? scope );

ASSIGN : '=' | ':' ;
OPEN_BRACE : '{';
CLOSE_BRACE : '}';
COMMA : ',';

ID: IDPART ('.' IDPART)* ;
IDPART:  ( ( [a-zA-Z] [0-9a-zA-Z_]* )
 | ( [a-zA-Z] [0-9a-zA-Z_]* '-' [0-9a-zA-Z_]) )
 ;

NEWLINE   : '\r' '\n' | '\n' | '\r';

cval : ( realNumber | wholeNumber | booleanValue | stringValue | hexValue);

realNumber: FLOAT;
wholeNumber: INTEGER;
booleanValue: BOOLEAN;
stringValue : SSTRING_LITERAL | DSTRING_LITERAL | RAW_SCOPE_LITERAL;
hexValue : HEX_LITERAL;

LONG : '-'? INT ('l'|'L') ;
DOUBLE    :   ('-'? INT '.' '0'* INT EXP? | '-'? INT EXP | '-'? INT ) ('d'|'D') ;
INTEGER : '-'? INT ;
FLOAT
    :    '-'? INT '.' INT EXP?   // 1.35, 1.35E-9, 0.3, -4.5
    |   '-'? INT EXP            // 1e10 -3e4
    |   '-'? INT    // -3, 45
    ;
BOOLEAN : 'true' | 'false';
HEX_LITERAL: '0' ('x'|'X') HEX_CHAR+ ;
fragment HEX_CHAR: [0123456789ABCDEFabcdef_];

fragment INT :   '0' | [1-9] [0-9]* ; // no leading zeros
fragment ZINT : [0-9]* ; // leading zeroes
fragment EXP :   [Ee] [+\-]? INT ;

SSTRING_LITERAL : '\'' (~('\'' | '\\' | '\r' | '\n') | '\\' ('\'' | '\\' | . ))* '\'';
DSTRING_LITERAL :
  '"'
  (
    ~('"' | '\\' | '\r' | '\n')
    | '\\' ('"' | '\\' | .)
  )*
  '"';

fragment RAW_SCOPE_LITERAL: ~('}' | '\r' | '\n')+;
fragment RAW_BASIC_LITERAL : ~('"' | '\\' | '\r' | '\n')+;

WS : [\u000C \t\n]+ -> channel(HIDDEN);
