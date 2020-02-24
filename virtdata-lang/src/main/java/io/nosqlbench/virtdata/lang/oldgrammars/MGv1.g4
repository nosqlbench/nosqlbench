grammar MGv1;
// https://www.youtube.com/watch?v=eW4WFgRtFeY

gencontextdef : ( entitydef | samplerdef)* ;

entitydef : 'entity' entityName
    ( 'pop' '=' popSize )?
    (fielddef | funcdef)*;
entityName : id;
popSize : NUMBER;
fielddef: 'field' fieldName COLON fieldType (STORELEFT chainedFuncSpec)? ;
fieldType : id;
fieldName : id ;

funcdef: 'func' funcName STORELEFT chainedFuncSpec;
funcName : id;

samplerdef : 'sampler' samplerName (':' samplerEntity)? (STORELEFT samplerFunc)? ;
samplerEntity: id;
samplerName : id;
samplerFunc : chainedFuncSpec ;

chainedFuncSpec : chainedFuncPart (';' chainedFuncPart)* ';'? ;
chainedFuncPart :  assignment? functionCall ;
functionCall : functionName LPAREN funcArgs RPAREN ;
assignment : assignTo EQUALS ;
assignTo : id;
funcArgs : (funcArg (',' funcArg)* )* ;
funcArg : assignment? value ;
functionName : id ;
parameter : id ;
expression: functionCall | value ;
value : stringValue | stringTemplate | numericValue | nonCommaOrParen ;
numericValue : NUMBER ;
stringValue : SQUOTESTRING ;
//stringTemplate : DOUBLE_QUOTED ;
stringTemplate : '"' templateSection+ '"' ;
templateSection : templateVarname | templatePreamble templateVarname | templatePreamble ;
templatePreamble : ~('${')+ ;
templateVarname : LSUBST id RSUBST;
nonComma : ((~(','))|('.'|'-'|'/'))+? ;
nonCommaOrParen : (~(','|')'))+;
//nonCommaOrParen : ((~(','|')'))|('.'|'-'|'/'|'%'))+ ;
//nonComma : .+? ;
id : 'entity' | 'sampler' | ID;

LINE_COMMENT: '//' (~'\n')* NEWLINE -> skip ;
COMMENT: '/*' .*? '*/' -> skip;
WS : [ \e\n]+ -> skip ;
NEWLINE: '\r' ? '\n';
ID: IDPART ('.' IDPART)* ;
IDPART:  [a-zA-Z] [0-9a-zA-Z_-]* ;
NUMBER: [0-9]+ ( '.' [0-9]+ )? ;
STORELEFT: '<-';
COLON: ':';
SQUOTE: '\'';
SQUOTESTRING: '\'' .*? '\'' ;
DQUOTE: '"';
EQUALS: '=';
LSUBST: '${';
RSUBST: '}';
LPAREN: '(';
RPAREN: ')';


