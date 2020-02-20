grammar MgRedux;
// https://www.youtube.com/watch?v=eW4WFgRtFeY

gencontext:
    genblock*
    ;

genblock :
    id
    blockparams
    (STORELEFT blockfunction (STORELEFT blockinput)* )?
    (LBRACE genblock* RBRACE)?
    ;

blockname: id;

blockparams: blockparam* ;
blockparam: id EQUALS value;
blockfunction: funccall+ ;
blockinput: ID;
funccall: id LPAREN (argument (COMMA argument)* )* RPAREN SEMI;
argument: (id EQUALS)? ( id | value );

id: ID;
idref : LANGLE id RANGLE ;
value: (
 id
 | idref
 | intvalue
 | realvalue
 | quotedstring
 | nulllist
 | intlist
 | reallist
 | stringlist
 | listoflist
 | tuple );
intvalue: WHOLENUMBER;
realvalue: REALNUMBER;
quotedstring:
    SQUOTE stringvalue SQUOTE
    | SQUOTE SQUOTE;
nulllist: LBRACKET RBRACKET;
intlist: LBRACKET (intvalue (COMMA intvalue)* )* RBRACKET ;
reallist: LBRACKET (realvalue (COMMA realvalue)* )* RBRACKET ;
tuple: LBRACKET ( value (COMMA value)* )* RBRACKET;
stringlist: LBRACKET ( quotedstring (COMMA quotedstring)* )* RBRACKET;
listoflist: LBRACKET ( (intlist | reallist | stringlist | tuple) (COMMA (intlist | reallist | stringlist | tuple) )* )* RBRACKET ;
stringvalue: .+? ;

LINE_COMMENT: '//' (~'\n')* NEWLINE -> skip ;
COMMENT: '/*' .*? '*/' -> skip;
WS : [ \e\n]+ -> skip ;
NEWLINE: '\r' ? '\n';

ID: IDPART ('.' IDPART)* ;
IDPART:  [a-zA-Z:] [0-9a-zA-Z_-]* ;

REALNUMBER: ('-'|'+')? [0-9]+ ( '.' [0-9]+ )+ ;
WHOLENUMBER: ('-'|'+')? [0-9]+ ;

EQUALS: '=';
STORELEFT: '<-';
LBRACE: '{';
RBRACE: '}';
LBRACKET: '[';
RBRACKET: ']';
LPAREN: '(';
RPAREN: ')';
COMMA: ',';
SQUOTE: '\'';
SEMI: ';';
LANGLE: '<' ;
RANGLE: '>' ;