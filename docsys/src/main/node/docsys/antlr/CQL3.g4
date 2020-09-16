grammar CQL3;


// workaround for:
//  https://github.com/antlr/antlr4/issues/118
//random_wrapper
//    : statements EOF
//   ;

statements
    : statement ( ';'+ statement )* ';'+
    ;

statement
    : drop_keyspace_stmt
    | create_keyspace_stmt
    | alter_keyspace_stmt
    | use_stmt
    | create_table_stmt
    | alter_table_stmt
    | drop_table_stmt
    | truncate_table_stmt
    | create_index_stmt
    | drop_index_stmt
    | insert_stmt
    | update_stmt
    | delete_stmt
    | batch_stmt
    ;

dml_statements
    : dml_statement (';'+ dml_statement)* ';'+
    ;

dml_statement
    : insert_stmt
    | update_stmt
    | delete_stmt
    ;

create_keyspace_stmt
    : K_CREATE K_KEYSPACE if_not_exists? keyspace_name K_WITH properties
    ;

alter_keyspace_stmt
    : K_ALTER K_KEYSPACE keyspace_name K_WITH properties
    ;

drop_keyspace_stmt
    : K_DROP K_KEYSPACE if_exists? keyspace_name
    ;

use_stmt
    : K_USE keyspace_name
    ;

create_table_stmt
    : K_CREATE (K_TABLE | K_COLUMNFAMILY) if_not_exists? table_name column_definitions (K_WITH table_options)?
    ;

alter_table_stmt
    : K_ALTER (K_TABLE | K_COLUMNFAMILY) table_name alter_table_instruction
    ;

alter_table_instruction
    : K_ALTER column_name K_TYPE column_type
    | K_ADD column_name column_type
    | K_DROP column_name
    | K_WITH table_options
    ;

drop_table_stmt
    : K_DROP K_TABLE if_exists? table_name
    ;

truncate_table_stmt
    : K_TRUNCATE table_name
    ;

create_index_stmt
    : K_CREATE (K_CUSTOM)? K_INDEX if_not_exists? index_name? K_ON table_name '(' column_name ')'
      (K_USING index_class (K_WITH index_options)?)?
    ;

drop_index_stmt
    : K_DROP K_INDEX if_exists? index_name
    ;

insert_stmt
    : K_INSERT K_INTO table_name column_names K_VALUES column_values if_not_exists? upsert_options?
    ;

column_names
    : '(' column_name (',' column_name)* ')'
    ;

column_values
    : '(' term (',' term)* ')'
    ;

upsert_options
    : K_USING upsert_option (K_AND upsert_option)*
    ;

upsert_option
    : K_TIMESTAMP INTEGER
    | K_TTL INTEGER
    ;

index_name
    : IDENTIFIER
    ;

index_class
    : STRING
    ;

index_options
    : K_OPTIONS '=' map
    ;

update_stmt
    : K_UPDATE table_name upsert_options? K_SET update_assignments K_WHERE where_clause update_conditions?
    ;

update_assignments
    : update_assignment (',' update_assignment)*
    ;

update_assignment
    : column_name '=' term
    | column_name '=' column_name ('+' | '-') (INTEGER | set | list)
    | column_name '=' column_name '+' map
    | column_name '[' term ']' '=' term
    ;

update_conditions
    : K_IF update_condition (K_AND update_condition)*
    ;

update_condition
    : IDENTIFIER '=' term
    | IDENTIFIER '[' term ']' '=' term
    ;

where_clause
    : relation (K_AND relation)*
    ;

relation
    : column_name '=' term
    | column_name K_IN '(' (term (',' term)*)? ')'
    | column_name K_IN '?'
    ;

delete_stmt
    : K_DELETE delete_selections? K_FROM table_name
                        (K_USING K_TIMESTAMP INTEGER)?
                        K_WHERE where_clause
                        delete_conditions?
    ;

delete_conditions
    : K_IF ( K_EXISTS
           | (delete_condition (K_AND delete_condition)*))
    ;

delete_condition
    : IDENTIFIER ('[' term ']')? '=' term
    ;

delete_selections
    : delete_selection (',' delete_selection)*
    ;

delete_selection
    : IDENTIFIER ('[' term ']')?
    ;

batch_stmt
    : K_BEGIN (K_UNLOGGED | K_COUNTER)? K_BATCH batch_options? dml_statements K_APPLY K_BATCH
    ;

batch_options
    : K_USING batch_option (K_AND batch_option)*
    ;

batch_option
    : K_TIMESTAMP INTEGER
    ;

table_name
    : (keyspace_name '.')? table_name_noks
    ;

table_name_noks
    : IDENTIFIER
    ;

column_name
    : IDENTIFIER
    ;

table_options
    : table_option (K_AND table_option)*
    ;

table_option
    : property
    | K_COMPACT K_STORAGE
    | K_CLUSTERING K_ORDER K_BY IDENTIFIER
    | K_CLUSTERING K_ORDER K_BY '('IDENTIFIER asc_or_desc')'
    ;

asc_or_desc
    : K_ASC
    | K_DESC
    ;

column_definitions
    : '(' column_definition (',' column_definition)* ')'
    ;

column_definition
    : column_name column_type (K_STATIC)? (K_PRIMARY K_KEY)?
    | K_PRIMARY K_KEY primary_key
    ;

column_type
    : data_type
    ;

primary_key
    : '(' partition_key (',' clustering_column)* ')'
    ;

partition_key
    : column_name
    | '(' column_name (',' column_name)* ')'
    ;

clustering_column
    : column_name
    ;

keyspace_name
    : IDENTIFIER
    ;

if_not_exists
    : K_IF K_NOT K_EXISTS
    ;

if_exists
    : K_IF K_EXISTS
    ;

constant
    : STRING
    | INTEGER
    | FLOAT
    | bool
    | UUID
    | BLOB
    ;

variable
    : '?'
    | ':' IDENTIFIER
    ;

term
    : constant
    | collection
    | variable
    | function
    ;

collection
    : map
    | set
    | list
    ;

map
    : '{' (term ':' term (',' term ':' term)*)? '}'
    ;

set
    : '{' (term (',' term)*)? '}'
    ;

list
    : '[' (term (',' term)*)? ']'
    ;

function
    : IDENTIFIER '(' (term (',' term)*)? ')'
    ;

properties
    : property (K_AND property)*
    ;

property
    : property_name '=' property_value
    ;

property_name
    : IDENTIFIER
    ;

property_value
    : IDENTIFIER
    | constant
    | map
    ;

data_type
    : native_type
    | collection_type
    | STRING
    ;

native_type
    : 'ascii'
    | 'bigint'
    | 'blob'
    | 'boolean'
    | 'counter'
    | 'decimal'
    | 'double'
    | 'float'
    | 'inet'
    | 'int'
    | 'text'
    | 'tinyint'
    | 'timestamp'
    | 'timeuuid'
    | 'uuid'
    | 'varchar'
    | 'varint'
    ;

collection_type
    : 'list' '<' native_type '>'
    | 'set' '<' native_type '>'
    | 'map' '<' native_type ',' native_type '>'
    ;


bool
    : K_TRUE
    | K_FALSE
    ;


K_ADD:          A D D;
K_ALTER:        A L T E R;
K_AND:          A N D;
K_APPLY:        A P P L Y;
K_BATCH:        B A T C H;
K_BEGIN:        B E G I N;
K_CLUSTERING:   C L U S T E R I N G;
K_ASC:          A S C;
K_DESC:         D E S C;
K_COLUMNFAMILY: C O L U M N F A M I L Y;
K_COMPACT:      C O M P A C T;
K_COUNTER:      C O U N T E R;
K_CREATE:       C R E A T E;
K_CUSTOM:       C U S T O M;
K_DELETE:       D E L E T E;
K_DROP:         D R O P;
K_EXISTS:       E X I S T S;
K_FALSE:        F A L S E;
K_FROM:         F R O M;
K_IF:           I F;
K_IN:           I N;
K_INDEX:        I N D E X;
K_INSERT:       I N S E R T;
K_INTO:         I N T O;
K_KEY:          K E Y;
K_KEYSPACE:     K E Y S P A C E;
K_NOT:          N O T;
K_ON:           O N;
K_OPTIONS:      O P T I O N S;
K_ORDER:        O R D E R;
K_BY:           B Y;
K_PRIMARY:      P R I M A R Y;
K_SELECT:       S E L E C T;
K_SET:          S E T;
K_STATIC:       S T A T I C;
K_STORAGE:      S T O R A G E;
K_TABLE:        T A B L E;
K_TIMESTAMP:    T I M E S T A M P;
K_TRUE:         T R U E;
K_TRUNCATE:     T R U N C A T E;
K_TTL:          T T L;
K_TYPE:         T Y P E;
K_UNLOGGED:     U N L O G G E D;
K_UPDATE:       U P D A T E;
K_USE:          U S E;
K_USING:        U S I N G;
K_VALUES:       V A L U E S;
K_WHERE:        W H E R E;
K_WITH:         W I T H;




IDENTIFIER
    : [a-zA-Z0-9_] [a-zA-Z0-9_]*
    ;


STRING
    : '\'' IDENTIFIER  '\''
    ;

INTEGER
    : '-'? DIGIT+
    ;

FLOAT
    : '-'? DIGIT+ ( '.' DIGIT* )? ( E [+-]? DIGIT+ )?
    | 'NaN'
    | 'Infinity'
    ;

UUID
    : HEX HEX HEX HEX HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX
    ;


BLOB
    : '0' X (HEX)+
    ;


fragment HEX : [0-9a-fA-F];
fragment DIGIT : [0-9];
fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];

SINGLE_LINE_COMMENT
    : ('--'|'//') ~[\r\n]* -> channel(HIDDEN)
    ;

MULTILINE_COMMENT
    : '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN)
    ;

WS
    : [ \t\r\n] -> channel(HIDDEN)
    ;
