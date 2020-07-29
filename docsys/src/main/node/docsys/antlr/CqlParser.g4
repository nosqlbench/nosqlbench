/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 by Domagoj Kovačević
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Project : cql-parser; an ANTLR4 grammar for Apache Cassandra CQL  https://github.com/kdcro101cql-parser
 */

parser grammar CqlParser;

options { tokenVocab=CqlLexer; }

root
    : cqls? MINUSMINUS? eof
    ;

cqls
    : (cql MINUSMINUS? statementSeparator | empty)* (cql (MINUSMINUS? statementSeparator)? | empty)
    ;
statementSeparator
    : SEMI
    ;

empty
    : statementSeparator
    ;

cql
    : beginBatch
    | alterKeyspace
    | alterMaterializedView
    | alterRole
    | alterTable
    | alterType
    | alterUser
    | applyBatch
    | createAggregate
    | createFunction
    | createIndex
    | createKeyspace
    | createMaterializedView
    | createRole
    | createTable
    | createTrigger
    | createType
    | createUser
    | delete
    | dropAggregate
    | dropFunction
    | dropIndex
    | dropKeyspace
    | dropMaterializedView
    | dropRole
    | dropTable
    | dropTrigger
    | dropType
    | dropUser
    | grant
    | insert
    | listPermissions
    | listRoles
    | revoke
    | select
    | truncate
    | update
    | use
    ;

revoke
    : kwRevoke priviledge kwOn resource kwFrom role
    ;

listUsers
    : kwListUsers
    ;

listRoles
    : kwListRoles (kwOf role)? kwNorecursive?
    ;

listPermissions
    : kwList priviledge (kwOn resource)? (kwOf role)?
    ;

grant
    : kwGrant priviledge kwOn resource kwTo role
    ;

priviledge
    :(kwAll | kwAllPermissions)
    |kwAlter
    |kwAuthorize
    |kwDescibe
    |kwExecute
    |kwCreate
    |kwDrop
    |kwModify
    |kwSelect
    ;

resource
    : kwAllFunctions
    | kwAllFunctions kwIn kwKeyspace keyspace
    | kwFunction (keyspace DOT)? function
    | kwAllKeyspaces
    | kwKeyspace keyspace
    | (kwTable)? tableSpec
    | kwAllRoles
    | kwRole role
    ;

createUser
    : kwCreate kwUser ifNotExist? user kwWith kwPassword constantString (kwSuperuser|kwNosuperuser)?
    ;

createRole
    : kwCreate kwRole ifNotExist? role roleWith?
    ;

createType
    : kwCreate kwType ifNotExist? objectUnknownSpec
      syntaxBracketLr typeMemberColumnList syntaxBracketRr
    ;
typeMemberColumnList
    : columnSpec dataType (syntaxComma columnSpec dataType)*
    ;

createTrigger
    : kwCreate kwTrigger ifNotExist? objectUnknownSpec kwUsing triggerClass
    ;

createMaterializedView
    : kwCreate kwMaterializedView ifNotExist? objectUnknownSpec
      kwAs
      kwSelect baseColumnList kwFrom baseTableSpec
      materializedViewWhere
      primaryKeyElement
      (kwWith materializedViewOptions)?

    ;
materializedViewWhere
    : kwWhere columnNotNullList (kwAnd relationElements)?
    ;
columnNotNullList
    : columnNotNull (kwAnd columnNotNull)*
    ;
columnNotNull
    : columnSpec kwIs kwNot kwNull
    ;

materializedViewOptions
    : tableOptions
    | tableOptions kwAnd clusteringOrder
    | clusteringOrder
    | clusteringOrder kwAnd tableOptions
    ;

createKeyspace
    : kwCreate kwKeyspace ifNotExist? objectUnknown
      kwWith kwReplication OPERATOR_EQ syntaxBracketLc replicationList syntaxBracketRc
      (kwAnd durableWrites)?
    ;

createFunction
    : kwCreate orReplace? kwFunction  ifNotExist?
      objectUnknownSpec syntaxBracketLr paramList? syntaxBracketRr
      returnMode
      kwReturns dataType
      kwLanguage language kwAs
      codeBlock

    ;
codeBlock
    : CODE_BLOCK
    ;
paramList
    : param (syntaxComma param)*
    ;

returnMode
    : (kwCalled | kwReturns kwNull) kwOn kwNull kwInput
    ;
createAggregate
    : kwCreate orReplace? kwAggregate ifNotExist?
      objectUnknownSpec syntaxBracketLr dataType syntaxBracketRr
      kwSfunc function
      kwStype dataType
      kwFinalfunc function
      kwInitcond initCondDefinition
    ;

initCondDefinition
    : constant
    | initCondList
    | initCondListNested
    | initCondHash
    ;
initCondHash
    : syntaxBracketLc initCondHashItem (syntaxComma initCondHashItem )*   syntaxBracketRc
    ;
initCondHashItem
    : hashKey COLON initCondDefinition
    ;
initCondListNested
    : syntaxBracketLr initCondList (syntaxComma constant | initCondList)* syntaxBracketRr
    ;

initCondList
    : syntaxBracketLr constant (syntaxComma constant)* syntaxBracketRr
    ;

orReplace
    : kwOr kwReplace
    ;

alterUser
    : kwAlter kwUser user kwWith userPassword userSuperUser?
    ;

userPassword
    : kwPassword constantString
    ;

userSuperUser
    : kwSuperuser
    | kwNosuperuser
    ;

alterType
    : kwAlter kwType typeSpec alterTypeOperation
    ;
alterTypeOperation
    : alterTypeAlterType? alterTypeAdd? alterTypeRename?
    ;

alterTypeRename
    : kwRename alterTypeRenameList
    ;
alterTypeRenameList
    : alterTypeRenameItem (kwAnd alterTypeRenameItem)*
    ;

alterTypeRenameItem
    : columnSpec kwTo columnUnknownSpec
    ;

alterTypeAdd
    : kwAdd columnUnknownSpec dataType (syntaxComma columnUnknownSpec dataType)*
    ;

alterTypeAlterType
    : kwAlter columnSpec kwType dataType
    ;

alterTable
    : kwAlter kwTable tableSpec alterTableOperation
    ;
alterTableOperation
    : alterTableAdd
    | alterTableDropColumns
    | alterTableDropColumns
    | alterTableDropCompactStorage
    | alterTableRename
    | alterTableWith
    | { this.notifyErrorListeners("rule.alterTableOperation"); }
    ;
alterTableWith
    : kwWith tableOptions
    ;

alterTableRename
    :kwRename columnSpec kwTo columnUnknownSpec
    ;
alterTableDropCompactStorage
    : kwDrop kwCompact kwStorage
    ;
alterTableDropColumns
    : kwDrop alterTableDropColumnList
    ;

alterTableDropColumnList
    : columnSpec (syntaxComma columnSpec)*
    ;

alterTableAdd
    : kwAdd alterTableColumnDefinition
    ;

alterTableColumnDefinition
    : columnUnknownSpec dataType (syntaxComma columnUnknownSpec dataType)*
    ;

alterRole
    : kwAlter kwRole role roleWith?
    ;
roleWith
    : kwWith (roleWithOptions (kwAnd roleWithOptions)*)
    ;
roleWithOptions
    : kwPassword OPERATOR_EQ constantString
    | kwLogin OPERATOR_EQ constantBoolean
    | kwSuperuser OPERATOR_EQ constantBoolean
    | kwOptions OPERATOR_EQ optionHash
    ;

alterMaterializedView
    : kwAlter kwMaterializedView materializedViewSpec
     (kwWith tableOptions)?
    ;

dropUser
    : kwDrop kwUser ifExist? user
    ;

dropType
    : kwDrop kwType ifExist? typeSpec
    ;
dropMaterializedView
    : kwDrop kwMaterializedView ifExist? materializedViewSpec

    ;
dropAggregate
    : kwDrop kwAggregate ifExist? aggregateSpec
    ;
dropFunction
    : kwDrop kwFunction ifExist? functionSpec
    ;
dropTrigger
    : kwDrop kwTrigger ifExist? trigger kwOn tableSpec
    ;
dropRole
    : kwDrop kwRole ifExist? role
    ;

dropTable
    : kwDrop kwTable ifExist? tableSpec
    ;
dropKeyspace
    : kwDrop kwKeyspace ifExist? keyspace
    ;
dropIndex
    : kwDrop kwIndex ifExist? indexSpec
    ;
createTable
    : kwCreate kwTable ifNotExist? objectUnknownSpec createTableDef withElement?
    ;

createTableDef
    : syntaxBracketLr columnDefinitionList syntaxBracketRr
    | { this.notifyErrorListeners("rule.createTableDef"); }
    ;

withElement
    : kwWith tableOptions (kwAnd clusteringOrder)?
    | kwWith clusteringOrder? (kwAnd tableOptions)?
    ;

clusteringOrder
    : kwClustering kwOrder kwBy syntaxBracketLr clusteringOrderItem (syntaxComma clusteringOrderItem)* syntaxBracketRr
    ;

clusteringOrderItem
    : columnSpec orderDirection?
    ;


tableOptions
    :  tableOptionItem (kwAnd tableOptionItem)*
    ;
tableOptionItem
    : tableOptionName OPERATOR_EQ tableOptionValue
    | tableOptionName OPERATOR_EQ optionHash
    ;
tableOptionName
    : OBJECT_NAME
    ;

tableOptionValue
    : constantString
    | constantFloat
    ;
optionHash
    : syntaxBracketLc optionHashItem (syntaxComma optionHashItem)*  syntaxBracketRc
    ;
optionHashItem
    : optionHashKey COLON optionHashValue
    ;
optionHashKey
    : constantString
    ;
optionHashValue
    : constantString
    | constantFloat
    ;

columnDefinitionList
    : columnDefinition (syntaxComma columnDefinition)* (syntaxComma primaryKeyElement)?
    ;

columnDefinition
    : columnUnknown dataType primaryKeyModifier
    | columnUnknown dataType kwStatic
    | columnUnknown dataType
    | { this.notifyErrorListeners("rule.columnDefinition"); }
    ;

primaryKeyModifier
    : kwPrimary kwKey
    | kwPrimary { this.notifyErrorListeners("rule.primaryKeyModifier"); }
    ;
primaryKeyElement
    : kwPrimary kwKey syntaxBracketLr primaryKeyDefinition syntaxBracketRr
    ;

primaryKeyDefinition
    : primaryKeySimple
    | primaryKeyComposite
    | compoundKey
    ;

primaryKeySimple
    : columnSpec
    ;
primaryKeyComposite
    : syntaxBracketLr partitionKeyList syntaxBracketRr
    ;

compoundKey
    : columnSpec (syntaxComma clusteringKeyList)
    |  syntaxBracketLr partitionKeyList syntaxBracketRr (syntaxComma clusteringKeyList)
    ;

partitionKeyList
    : ( columnSpec ) (syntaxComma columnSpec)*
    ;
clusteringKeyList
    : ( columnSpec ) (syntaxComma columnSpec)*
    ;

applyBatch
    : kwApply kwBatch
    ;

beginBatch
    : beginBatchSpec delete
    | beginBatchSpec insert
    | beginBatchSpec update
    ;

beginBatchSpec
    : kwBegin batchType? kwBatch usingTimestampSpec?
    ;
batchType
    : kwLogged
    | kwUnlogged
    ;

alterKeyspace
    : kwAlter kwKeyspace keyspace
      kwWith kwReplication OPERATOR_EQ syntaxBracketLc replicationList syntaxBracketRc
      (kwAnd durableWrites)?
    ;

replicationList
    : replicationListItem (syntaxComma replicationListItem)*
    ;
// replicationList
//     : ( replicationListItem ) (syntaxComma replicationListItem)*
//     ;

replicationListItem
    : STRING_LITERAL COLON STRING_LITERAL
    | STRING_LITERAL COLON DECIMAL_LITERAL
    ;
durableWrites
    : kwDurableWrites OPERATOR_EQ constantBoolean
    ;

use
    : kwUse keyspace
    ;

truncate
    : kwTruncate (kwTable)? tableSpec
    ;

createIndex
    : kwCreate kwIndex ifNotExist? objectUnknown? createIndexSubject createIndexDef
    ;

createIndexSubject
    : kwOn tableSpec
    | { this.notifyErrorListeners("rule.createIndexSubject"); }
    ;

index
    : OBJECT_NAME
    | constantString
    ;
createIndexDef
    : syntaxBracketLr createIndexTarget syntaxBracketRr
    | { this.notifyErrorListeners("rule.createIndexDef"); }
    ;
createIndexTarget
    : columnSpec
    | indexKeysSpec
    | indexEntriesSSpec
    | indexFullSpec
    | { this.notifyErrorListeners("rule.createIndexTarget"); }
    ;

indexKeysSpec
    : kwKeys syntaxBracketLr (columnSpec| { this.notifyErrorListeners("rule.indexKeysSpec"); }) syntaxBracketRr
    ;
indexEntriesSSpec
    : kwEntries syntaxBracketLr (columnSpec| { this.notifyErrorListeners("rule.indexEntriesSSpec"); }) syntaxBracketRr
    ;
indexFullSpec
    : kwFull syntaxBracketLr (columnSpec| { this.notifyErrorListeners("rule.indexFullSpec"); }) syntaxBracketRr
    ;

delete
    : kwDelete deleteColumnList? fromSpec usingTimestampSpec?
      ( whereSpec | { this.notifyErrorListeners("rule.whereSpec"); } ) (ifExist | ifSpec)?
    ;

deleteColumnList
    : ( deleteColumnItem ) (syntaxComma deleteColumnItem)*
    ;

deleteColumnItem
    : columnSpec
    | columnSpec syntaxBracketLs (constantString|constantDecimal)  syntaxBracketRs
    ;


update
    : kwUpdate tableOrMaterializedViewSpec  usingTtlTimestamp?  updateAssignments
      (whereSpec | { this.notifyErrorListeners("rule.whereSpec"); }) (ifExist | ifSpec)?
    ;

ifSpec
    : kwIf ifConditionList
    ;
ifConditionList
    : ( ifCondition ) (kwAnd ifCondition)*
    ;
ifCondition
    : OBJECT_NAME OPERATOR_EQ constant
    ;

updateAssignments
    : kwSet ( updateAssignmentElement ) (syntaxComma updateAssignmentElement)*
    | { this.notifyErrorListeners("rule.updateAssignments"); }
    ;

updateAssignmentElement
    // : column syntaxOperatorEq (constant | assignmentMap | assignmentSet | assignmentList)
    : columnSpec syntaxOperatorEq (constant | constantCollection)
    | columnSpec syntaxOperatorEq columnSpec (syntaxPlus | syntaxMinus) constantDecimal
    | columnSpec syntaxOperatorEq columnSpec (syntaxPlus | syntaxMinus) assignmentMap
    | columnSpec syntaxOperatorEq columnSpec (syntaxPlus | syntaxMinus) assignmentSet
    | columnSpec syntaxOperatorEq columnSpec (syntaxPlus | syntaxMinus) assignmentList
    | columnSpec syntaxOperatorEq assignmentSet (syntaxPlus | syntaxMinus) OBJECT_NAME
    | columnSpec syntaxOperatorEq assignmentMap (syntaxPlus | syntaxMinus) OBJECT_NAME
    | columnSpec syntaxOperatorEq assignmentList (syntaxPlus | syntaxMinus) OBJECT_NAME
    | columnSpec syntaxBracketLs constantDecimal syntaxBracketRs syntaxOperatorEq constant
    | { this.notifyErrorListeners("rule.updateAssignmentElement"); }
    ;

assignmentSet
    : syntaxBracketLc constant (syntaxComma constant)* syntaxBracketRc
    ;

assignmentMap
    : syntaxBracketLc (constant syntaxColon constant ) (constant syntaxColon constant )* syntaxBracketRc
    ;

assignmentList
    : syntaxBracketLs constant (syntaxColon constant )* syntaxBracketRs
    ;


insert
    : kwInsertInto tableOrMaterializedViewSpec insertColumnSpec insertValuesSpec (ifNotExist|) usingTtlTimestamp?
    ;
usingTtlTimestamp
    : kwUsing ttl
    | kwUsing ttl kwAnd timestamp
    | kwUsing timestamp
    | kwUsing timestamp kwAnd ttl
    ;

timestamp
    : kwTimestamp constantDecimal
    ;

ttl
    : kwTtl constantDecimal
    ;

usingTimestampSpec
    : kwUsing timestamp
    ;

ifNotExist
    : kwIf kwNot kwExists
    ;
ifExist
    : kwIf kwExists
    ;

insertValuesSpec
    : kwValues syntaxBracketLr expressionList (syntaxBracketRr| { this.notifyErrorListeners("rule.syntaxBracketRr"); })
    | { this.notifyErrorListeners("rule.insertValuesSpec"); }
    ;

insertColumnSpec
    : syntaxBracketLr columnList syntaxBracketRr
    | { this.notifyErrorListeners("rule.insertColumnSpec"); }
    ;

columnList
    : columnSpec (syntaxComma (columnSpec | { this.notifyErrorListeners("rule.column"); }))*
    | { this.notifyErrorListeners("rule.columnList"); }
    ;
baseColumnList
    : columnSpec (syntaxComma (columnSpec | { this.notifyErrorListeners("rule.column"); }))*
    | { this.notifyErrorListeners("rule.baseColumnList"); }
    ;


expressionList
    : expression (syntaxComma expression)*
    ;

expression
    : ( constant| constantCollection )
    | { this.notifyErrorListeners("rule.expression"); }
    ;

select
    : kwSelect kwDistinct? selectElements fromSpec whereSpec? orderSpec? limitSpec? kwAllowFiltering?
    ;

limitSpec
    : kwLimit (constantDecimal| { this.notifyErrorListeners("rule.constantDecimal"); })
    ;

fromSpec
    : kwFrom tableOrMaterializedViewSpec
    | { this.notifyErrorListeners("rule.fromSpec"); }
    ;

orderSpec
    : kwOrderBy orderSpecElement
    ;
orderSpecElement
    : columnSpec (kwAsc|kwDesc)?
    | { this.notifyErrorListeners("rule.orderSpecElement"); }
    ;

whereSpec
    : kwWhere relationElements
    ;

selectElements
    : specialStar
    | selectElement (syntaxComma selectElement)*
    | { this.notifyErrorListeners("rule.selectElements"); }
    ;

selectElement
    : columnSpec
    | columnSpec (kwAs OBJECT_NAME)?
    | functionCall (kwAs OBJECT_NAME)?
    | { this.notifyErrorListeners("rule.selectElement"); }
    ;

relationElements
    : (relationElement ) (kwAnd relationElement)*
    ;

relationElement
    : relationElementConstant
    | relationElementIn
    | relationElementToken
    | OBJECT_NAME { this.notifyErrorListeners("rule.relationElement"); }
    | { this.notifyErrorListeners("rule.relationElement"); }
    ;

relationElementConstant
    : columnSpec relationOperator (constant | { this.notifyErrorListeners("rule.constant"); } )
    ;

relationElementIn
    : columnSpec kwIn syntaxBracketLr functionArgs? syntaxBracketRr
    ;

relationElementToken
    : relationElementTokenSpec
      (relationOperator | { this.notifyErrorListeners("rule.relationOperator"); })
      relationElementTokenSpec
    ;
relationElementTokenSpec
    : kwToken
      (syntaxBracketLr  | { this.notifyErrorListeners("rule.syntaxBracketLr"); })
      (columnSpec           | { this.notifyErrorListeners("rule.column"); })
      (syntaxBracketRr  | { this.notifyErrorListeners("rule.syntaxBracketRr"); })
    | { this.notifyErrorListeners("rule.relationElementTokenSpec"); }
    ;

relationOperator
    : syntaxOperatorEq
    | syntaxOperatorLt
    | syntaxOperatorGt
    | syntaxOperatorLte
    | syntaxOperatorGte
    | kwContains
    | kwContainsKey
    | { this.notifyErrorListeners("rule.relationOperator"); }
    ;



functionCall
    : OBJECT_NAME '(' STAR ')'
    | OBJECT_NAME '(' functionArgs? ')'
    ;

functionArgs
    : (constant | OBJECT_NAME | functionCall )
    (
      syntaxComma
      (constant | OBJECT_NAME | functionCall )
    )*
    ;


constant
    : constantUuid
    | constantString
    | constantDecimal
    | constantFloat
    | constantHexadecimal
    | constantBoolean
    | kwNull
    ;

collectionElement
    : constant | constantMap | constantSet | constantList | constantTuple
    ;

collectionMapElement
    : collectionElement syntaxColon collectionElement
    ;

constantCollection
    : constantMap | constantTuple | constantList | constantSet
    ;

constantMap
    : syntaxBracketLc collectionMapElement (syntaxComma collectionMapElement)* syntaxBracketRc
    ;

constantSet
    : syntaxBracketLc collectionElement (syntaxComma collectionElement)* syntaxBracketRc
    ;

constantList
    : syntaxBracketLs collectionElement (syntaxComma collectionElement)* syntaxBracketRs
    ;

constantTuple
    : syntaxBracketLr collectionElement (syntaxComma collectionElement)* syntaxBracketRr
    ;

constantUuid
    : UUID
    ;

constantDecimal
    : DECIMAL_LITERAL
    ;

constantFloat
    : DECIMAL_LITERAL
    | FLOAT_LITERAL
    ;

constantString
    : STRING_LITERAL
    ;

constantBoolean
    : K_TRUE | K_FALSE;

constantHexadecimal
    : HEXADECIMAL_LITERAL
    ;

keyspace
    : OBJECT_NAME
    // | DQUOTE OBJECT_NAME DQUOTE
    | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES
    ;

baseKeyspace
    : OBJECT_NAME
    // | DQUOTE OBJECT_NAME DQUOTE
    | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES
    ;

table
    : OBJECT_NAME
    // | DQUOTE OBJECT_NAME DQUOTE
    | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES
    ;

baseTable
    : OBJECT_NAME
    | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES
    ;

materializedView
    : OBJECT_NAME
    | DQUOTE OBJECT_NAME DQUOTE
    | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES
    ;

keyspaceObject
    : OBJECT_NAME
    | DQUOTE OBJECT_NAME DQUOTE
    | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES
    ;
objectUnknown
    : OBJECT_NAME
    | DQUOTE OBJECT_NAME DQUOTE
    | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES | K_INPUT
    ;

aggregateSpec
    : aggregate
    | keyspace specialDot aggregate
    | keyspace specialDot { this.notifyErrorListeners("rule.aggregate"); }
    | { this.notifyErrorListeners("rule.aggregateSpec"); }
    ;

typeSpec
    : type
    | keyspace specialDot type
    | keyspace specialDot { this.notifyErrorListeners("rule.type"); }
    | { this.notifyErrorListeners("rule.typeSpec"); }
    ;

functionSpec
    : function
    | keyspace specialDot function
    | keyspace specialDot { this.notifyErrorListeners("rule.function"); }
    | { this.notifyErrorListeners("rule.functionSpec"); }
    ;

tableSpec
    : table
    | syntaxDquote table syntaxDquote
    | ((syntaxDquote keyspace syntaxDquote)|(keyspace)) specialDot table
    | ((syntaxDquote keyspace syntaxDquote)|(keyspace)) specialDot syntaxDquote table syntaxDquote
    | ((syntaxDquote keyspace syntaxDquote)|(keyspace)) specialDot { this.notifyErrorListeners("rule.table"); }
    | { this.notifyErrorListeners("rule.tableSpec"); }
    ;

baseTableSpec
    : baseTable
    | syntaxDquote baseTable syntaxDquote
    | ((syntaxDquote baseKeyspace syntaxDquote)|(baseKeyspace)) specialDot baseTable
    | ((syntaxDquote baseKeyspace syntaxDquote)|(baseKeyspace)) specialDot syntaxDquote baseTable syntaxDquote
    | ((syntaxDquote baseKeyspace syntaxDquote)|(baseKeyspace)) specialDot { this.notifyErrorListeners("rule.baseTable"); }
    | { this.notifyErrorListeners("rule.baseTableSpec"); }
    ;

indexSpec
    : index
    | keyspace specialDot index
    | keyspace specialDot { this.notifyErrorListeners("rule.index"); }
    | { this.notifyErrorListeners("rule.indexSpec"); }
    ;

materializedViewSpec
    : materializedView
    | keyspace specialDot materializedView
    | keyspace specialDot { this.notifyErrorListeners("rule.materializedView"); }
    | { this.notifyErrorListeners("rule.materializedViewSpec"); }
    ;

tableOrMaterializedViewSpec
    : tableSpec
    | materializedViewSpec
    | { this.notifyErrorListeners("rule.tableOrMaterializedViewSpec"); }
    ;



objectUnknownSpec
    : objectUnknown
    | syntaxDquote objectUnknown syntaxDquote
    | ((syntaxDquote keyspace syntaxDquote)|(keyspace)) specialDot objectUnknown
    | ((syntaxDquote keyspace syntaxDquote)|(keyspace)) specialDot syntaxDquote objectUnknown syntaxDquote
    | ((syntaxDquote keyspace syntaxDquote)|(keyspace)) specialDot { this.notifyErrorListeners("rule.objectUnknownSpec"); }
    | { this.notifyErrorListeners("rule.objectUnknownSpec"); }
    ;

columnSpec
    : column
    | syntaxDquote column syntaxDquote
    ;

column
    : OBJECT_NAME
    | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES | K_INPUT
    ;

columnUnknownSpec
    : columnUnknown
    | syntaxDquote columnUnknown syntaxDquote
    ;

columnUnknown
    : OBJECT_NAME | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES  | K_INPUT
    | syntaxDquote (OBJECT_NAME | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES  | K_INPUT) syntaxDquote
    ;

dataType
    : dataTypeFundamental
    | dataTypeCollection
    ;

dataTypeCollection
    : dataTypeFrozen (dataTypeStructure | { this.notifyErrorListeners("rule.dataTypeStructure"); })
    | dataTypeSet (dataTypeStructure | { this.notifyErrorListeners("rule.dataTypeStructure"); })
    | dataTypeList (dataTypeStructure | { this.notifyErrorListeners("rule.dataTypeStructure"); })
    | dataTypeMap (dataTypeStructure | { this.notifyErrorListeners("rule.dataTypeStructure"); })
    | dataTypeTuple (dataTypeStructure | { this.notifyErrorListeners("rule.dataTypeStructure"); })
    ;

dataTypeFundamental
    : dataTypeAscii
    | dataTypeBigint
    | dataTypeBlob
    | dataTypeBoolean
    | dataTypeCounter
    | dataTypeDate
    | dataTypeDecimal
    | dataTypeDouble
    | dataTypeFloat
    | dataTypeInet
    | dataTypeInt
    | dataTypeSmallInt
    | dataTypeText
    | dataTypeTime
    | dataTypeTimeUuid
    | dataTypeTimestamp
    | dataTypeTinyInt
    | dataTypeUuid
    | dataTypeVarChar
    | dataTypeVarInt
    | dataTypeUserDefined
    ;

orderDirection
    : kwAsc
    | kwDesc
    ;

role
    : OBJECT_NAME
    ;

trigger
    : OBJECT_NAME
    ;
triggerClass
    : constantString
    ;

type
    : OBJECT_NAME
    ;
aggregate
    : OBJECT_NAME
    | DQUOTE OBJECT_NAME DQUOTE
    | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES | K_INPUT
    ;

function
    : OBJECT_NAME
    ;
language
    : OBJECT_NAME
    ;
user
    : OBJECT_NAME
    ;
password
    : constantString
    ;
hashKey: OBJECT_NAME;

param
    : paramName dataType
    ;
paramName
    : OBJECT_NAME
    | K_ROLE | K_PERMISSIONS | K_OPTIONS | K_DURABLE_WRITES | K_LANGUAGE | K_TYPE | K_INITCOND |
      K_REPLICATION | K_TTL | K_PARTITION | K_KEY | K_LEVEL | K_USERS | K_USER | K_ROLE | K_ROLES | K_INPUT
    ;

kwAdd: K_ADD;
kwAggregate: K_AGGREGATE;
kwAll: K_ALL;
kwAllFunctions: K_ALL_FUNCTIONS;
kwAllKeyspaces: K_ALL_KEYSPACES;
kwAllRoles: K_ALL_ROLES;
kwAllPermissions: K_ALL K_PERMISSIONS;
kwAllow: K_ALLOW;
kwAllowFiltering: K_ALLOW K_FILTERING;
kwAlter: K_ALTER;
kwAnd: K_AND;
kwApply: K_APPLY;
kwAs: K_AS;
kwAsc: K_ASC;
kwAuthorize: K_AUTHORIZE;
kwBatch: K_BATCH;
kwBegin: K_BEGIN;
kwBy: K_BY;
kwCalled: K_CALLED;
kwClustering: K_CLUSTERING;
kwCompact: K_COMPACT;
kwContains: K_CONTAINS;
kwContainsKey: K_CONTAINS K_KEY;
kwCreate: K_CREATE;
kwDelete: K_DELETE;
kwDesc: K_DESC;
kwDescibe: K_DESCRIBE;
kwDistinct: K_DISTINCT;
kwDrop: K_DROP;
kwDurableWrites: K_DURABLE_WRITES;
kwEntries: K_ENTRIES;
kwExecute: K_EXECUTE;
kwExists: K_EXISTS;
kwFiltering: K_FILTERING;
kwFinalfunc: K_FINALFUNC;
kwFrom: K_FROM;
kwFull: K_FULL;
kwFunction: K_FUNCTION;
kwGrant: K_GRANT;
kwIf: K_IF;
kwIn: K_IN;
kwIndex: K_INDEX;
kwInitcond: K_INITCOND;
kwInput: K_INPUT;
// kwInsert: K_INSERT;
kwInsertInto: K_INSERT K_INTO;
// kwInto: K_INTO;
kwIs: K_IS;
kwKey: K_KEY;
kwKeys: K_KEYS;
kwKeyspace: K_KEYSPACE;
kwLanguage: K_LANGUAGE;
kwLimit: K_LIMIT;
kwList: K_LIST;

kwListRoles: K_LIST K_ROLES;
kwListUsers: K_LIST K_USERS;

kwLogged: K_LOGGED;
kwLogin: K_LOGIN;
// kwMaterialized: K_MATERIALIZED;
kwMaterializedView: K_MATERIALIZED K_VIEW;
kwModify: K_MODIFY;
kwNosuperuser: K_NOSUPERUSER;
kwNorecursive: K_NORECURSIVE;
kwNot: K_NOT;
kwNull: K_NULL;
kwOf: K_OF;
kwOn: K_ON;
kwOptions: K_OPTIONS;
kwOr: K_OR;
kwOrder: K_ORDER;
kwOrderBy: K_ORDER K_BY;
kwPartition: K_PARTITION;
kwPassword:K_PASSWORD;
kwPermissions: K_PERMISSIONS;
kwPrimary: K_PRIMARY;
kwRename: K_RENAME;
kwReplace: K_REPLACE;
kwReplication: K_REPLICATION;
kwReturns: K_RETURNS;
kwRole: K_ROLE;
kwSelect: K_SELECT;
kwSet: K_SET;
kwSfunc: K_SFUNC;
kwStatic: K_STATIC;
kwStorage: K_STORAGE;
kwStype: K_STYPE;
kwSuperuser : K_SUPERUSER;
kwTable: K_TABLE;
kwTimestamp: K_TIMESTAMP;
kwTo: K_TO;
kwToken: K_TOKEN;
kwTrigger: K_TRIGGER;
kwTruncate: K_TRUNCATE;
kwTtl: K_TTL;
kwType: K_TYPE;
kwUnlogged: K_UNLOGGED;
kwUpdate: K_UPDATE;
kwUse: K_USE;
kwUser: K_USER;
kwUsers: K_USERS;
kwUsing: K_USING;
kwValues: K_VALUES;
// kwView: K_VIEW;
kwWhere: K_WHERE;
kwWith: K_WITH;
kwRevoke: K_REVOKE;

dataTypeAscii: K_ASCII;
dataTypeBigint: K_BIGINT;
dataTypeBlob:K_BLOB;
dataTypeBoolean: K_BOOLEAN;
dataTypeCounter:  K_COUNTER;
dataTypeDate: K_DATE;
dataTypeDecimal:K_DECIMAL;
dataTypeDouble: K_DOUBLE;
dataTypeFloat: K_FLOAT;
dataTypeFrozen: K_FROZEN;
dataTypeInet: K_INET;
dataTypeInt: K_INT;
dataTypeList: K_LIST;
dataTypeMap: K_MAP;
dataTypeSmallInt:  K_SMALLINT;
dataTypeSet: K_SET;
dataTypeText: K_TEXT;
dataTypeTime: K_TIME;
dataTypeTimeUuid:K_TIMEUUID;
dataTypeTimestamp: K_TIMESTAMP;
dataTypeTinyInt: K_TINYINT;
dataTypeTuple: K_TUPLE;
dataTypeUserDefined: OBJECT_NAME;
dataTypeUuid:  K_UUID;
dataTypeVarChar: K_VARCHAR;
dataTypeVarInt: K_VARINT;

dataTypeStructure
    : syntaxBracketLa dataType (syntaxComma dataType)* syntaxBracketRa
    ;

specialStar: STAR;
specialDot: DOT;

eof
: EOF
;

// BRACKETS
// L - left
// R - right
// a - angle
// c - curly
// r - rounded

syntaxBracketLr : LR_BRACKET;
syntaxBracketRr : RR_BRACKET;
syntaxBracketLc : LC_BRACKET;
syntaxBracketRc : RC_BRACKET;
syntaxBracketLa : OPERATOR_LT;
syntaxBracketRa : OPERATOR_GT;
syntaxBracketLs : LS_BRACKET;
syntaxBracketRs : RS_BRACKET;

syntaxComma: COMMA;
syntaxColon: COLON;
syntaxPlus: PLUS;
syntaxMinus: MINUS;

syntaxSquote: SQUOTE;
syntaxDquote: DQUOTE;

syntaxOperatorEq: OPERATOR_EQ;
syntaxOperatorLt: OPERATOR_LT;
syntaxOperatorGt: OPERATOR_GT;
syntaxOperatorLte: OPERATOR_LTE;
syntaxOperatorGte: OPERATOR_GTE;
