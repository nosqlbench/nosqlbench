// Generated from CqlParser.g4 by ANTLR 4.5
// jshint ignore: start
var antlr4 = require('antlr4/index');

// This class defines a complete generic visitor for a parse tree produced by CqlParser.

function CqlParserVisitor() {
	antlr4.tree.ParseTreeVisitor.call(this);
	return this;
}

CqlParserVisitor.prototype = Object.create(antlr4.tree.ParseTreeVisitor.prototype);
CqlParserVisitor.prototype.constructor = CqlParserVisitor;

// Visit a parse tree produced by CqlParser#root.
CqlParserVisitor.prototype.visitRoot = function(ctx) {
};


// Visit a parse tree produced by CqlParser#cqls.
CqlParserVisitor.prototype.visitCqls = function(ctx) {
};


// Visit a parse tree produced by CqlParser#statementSeparator.
CqlParserVisitor.prototype.visitStatementSeparator = function(ctx) {
};


// Visit a parse tree produced by CqlParser#empty.
CqlParserVisitor.prototype.visitEmpty = function(ctx) {
};


// Visit a parse tree produced by CqlParser#cql.
CqlParserVisitor.prototype.visitCql = function(ctx) {
};


// Visit a parse tree produced by CqlParser#revoke.
CqlParserVisitor.prototype.visitRevoke = function(ctx) {
};


// Visit a parse tree produced by CqlParser#listUsers.
CqlParserVisitor.prototype.visitListUsers = function(ctx) {
};


// Visit a parse tree produced by CqlParser#listRoles.
CqlParserVisitor.prototype.visitListRoles = function(ctx) {
};


// Visit a parse tree produced by CqlParser#listPermissions.
CqlParserVisitor.prototype.visitListPermissions = function(ctx) {
};


// Visit a parse tree produced by CqlParser#grant.
CqlParserVisitor.prototype.visitGrant = function(ctx) {
};


// Visit a parse tree produced by CqlParser#priviledge.
CqlParserVisitor.prototype.visitPriviledge = function(ctx) {
};


// Visit a parse tree produced by CqlParser#resource.
CqlParserVisitor.prototype.visitResource = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createUser.
CqlParserVisitor.prototype.visitCreateUser = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createRole.
CqlParserVisitor.prototype.visitCreateRole = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createType.
CqlParserVisitor.prototype.visitCreateType = function(ctx) {
};


// Visit a parse tree produced by CqlParser#typeMemberColumnList.
CqlParserVisitor.prototype.visitTypeMemberColumnList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createTrigger.
CqlParserVisitor.prototype.visitCreateTrigger = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createMaterializedView.
CqlParserVisitor.prototype.visitCreateMaterializedView = function(ctx) {
};


// Visit a parse tree produced by CqlParser#materializedViewWhere.
CqlParserVisitor.prototype.visitMaterializedViewWhere = function(ctx) {
};


// Visit a parse tree produced by CqlParser#columnNotNullList.
CqlParserVisitor.prototype.visitColumnNotNullList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#columnNotNull.
CqlParserVisitor.prototype.visitColumnNotNull = function(ctx) {
};


// Visit a parse tree produced by CqlParser#materializedViewOptions.
CqlParserVisitor.prototype.visitMaterializedViewOptions = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createKeyspace.
CqlParserVisitor.prototype.visitCreateKeyspace = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createFunction.
CqlParserVisitor.prototype.visitCreateFunction = function(ctx) {
};


// Visit a parse tree produced by CqlParser#codeBlock.
CqlParserVisitor.prototype.visitCodeBlock = function(ctx) {
};


// Visit a parse tree produced by CqlParser#paramList.
CqlParserVisitor.prototype.visitParamList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#returnMode.
CqlParserVisitor.prototype.visitReturnMode = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createAggregate.
CqlParserVisitor.prototype.visitCreateAggregate = function(ctx) {
};


// Visit a parse tree produced by CqlParser#initCondDefinition.
CqlParserVisitor.prototype.visitInitCondDefinition = function(ctx) {
};


// Visit a parse tree produced by CqlParser#initCondHash.
CqlParserVisitor.prototype.visitInitCondHash = function(ctx) {
};


// Visit a parse tree produced by CqlParser#initCondHashItem.
CqlParserVisitor.prototype.visitInitCondHashItem = function(ctx) {
};


// Visit a parse tree produced by CqlParser#initCondListNested.
CqlParserVisitor.prototype.visitInitCondListNested = function(ctx) {
};


// Visit a parse tree produced by CqlParser#initCondList.
CqlParserVisitor.prototype.visitInitCondList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#orReplace.
CqlParserVisitor.prototype.visitOrReplace = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterUser.
CqlParserVisitor.prototype.visitAlterUser = function(ctx) {
};


// Visit a parse tree produced by CqlParser#userPassword.
CqlParserVisitor.prototype.visitUserPassword = function(ctx) {
};


// Visit a parse tree produced by CqlParser#userSuperUser.
CqlParserVisitor.prototype.visitUserSuperUser = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterType.
CqlParserVisitor.prototype.visitAlterType = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTypeOperation.
CqlParserVisitor.prototype.visitAlterTypeOperation = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTypeRename.
CqlParserVisitor.prototype.visitAlterTypeRename = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTypeRenameList.
CqlParserVisitor.prototype.visitAlterTypeRenameList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTypeRenameItem.
CqlParserVisitor.prototype.visitAlterTypeRenameItem = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTypeAdd.
CqlParserVisitor.prototype.visitAlterTypeAdd = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTypeAlterType.
CqlParserVisitor.prototype.visitAlterTypeAlterType = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTable.
CqlParserVisitor.prototype.visitAlterTable = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTableOperation.
CqlParserVisitor.prototype.visitAlterTableOperation = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTableWith.
CqlParserVisitor.prototype.visitAlterTableWith = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTableRename.
CqlParserVisitor.prototype.visitAlterTableRename = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTableDropCompactStorage.
CqlParserVisitor.prototype.visitAlterTableDropCompactStorage = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTableDropColumns.
CqlParserVisitor.prototype.visitAlterTableDropColumns = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTableDropColumnList.
CqlParserVisitor.prototype.visitAlterTableDropColumnList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTableAdd.
CqlParserVisitor.prototype.visitAlterTableAdd = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterTableColumnDefinition.
CqlParserVisitor.prototype.visitAlterTableColumnDefinition = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterRole.
CqlParserVisitor.prototype.visitAlterRole = function(ctx) {
};


// Visit a parse tree produced by CqlParser#roleWith.
CqlParserVisitor.prototype.visitRoleWith = function(ctx) {
};


// Visit a parse tree produced by CqlParser#roleWithOptions.
CqlParserVisitor.prototype.visitRoleWithOptions = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterMaterializedView.
CqlParserVisitor.prototype.visitAlterMaterializedView = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dropUser.
CqlParserVisitor.prototype.visitDropUser = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dropType.
CqlParserVisitor.prototype.visitDropType = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dropMaterializedView.
CqlParserVisitor.prototype.visitDropMaterializedView = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dropAggregate.
CqlParserVisitor.prototype.visitDropAggregate = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dropFunction.
CqlParserVisitor.prototype.visitDropFunction = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dropTrigger.
CqlParserVisitor.prototype.visitDropTrigger = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dropRole.
CqlParserVisitor.prototype.visitDropRole = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dropTable.
CqlParserVisitor.prototype.visitDropTable = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dropKeyspace.
CqlParserVisitor.prototype.visitDropKeyspace = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dropIndex.
CqlParserVisitor.prototype.visitDropIndex = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createTable.
CqlParserVisitor.prototype.visitCreateTable = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createTableDef.
CqlParserVisitor.prototype.visitCreateTableDef = function(ctx) {
};


// Visit a parse tree produced by CqlParser#withElement.
CqlParserVisitor.prototype.visitWithElement = function(ctx) {
};


// Visit a parse tree produced by CqlParser#clusteringOrder.
CqlParserVisitor.prototype.visitClusteringOrder = function(ctx) {
};


// Visit a parse tree produced by CqlParser#clusteringOrderItem.
CqlParserVisitor.prototype.visitClusteringOrderItem = function(ctx) {
};


// Visit a parse tree produced by CqlParser#tableOptions.
CqlParserVisitor.prototype.visitTableOptions = function(ctx) {
};


// Visit a parse tree produced by CqlParser#tableOptionItem.
CqlParserVisitor.prototype.visitTableOptionItem = function(ctx) {
};


// Visit a parse tree produced by CqlParser#tableOptionName.
CqlParserVisitor.prototype.visitTableOptionName = function(ctx) {
};


// Visit a parse tree produced by CqlParser#tableOptionValue.
CqlParserVisitor.prototype.visitTableOptionValue = function(ctx) {
};


// Visit a parse tree produced by CqlParser#optionHash.
CqlParserVisitor.prototype.visitOptionHash = function(ctx) {
};


// Visit a parse tree produced by CqlParser#optionHashItem.
CqlParserVisitor.prototype.visitOptionHashItem = function(ctx) {
};


// Visit a parse tree produced by CqlParser#optionHashKey.
CqlParserVisitor.prototype.visitOptionHashKey = function(ctx) {
};


// Visit a parse tree produced by CqlParser#optionHashValue.
CqlParserVisitor.prototype.visitOptionHashValue = function(ctx) {
};


// Visit a parse tree produced by CqlParser#columnDefinitionList.
CqlParserVisitor.prototype.visitColumnDefinitionList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#columnDefinition.
CqlParserVisitor.prototype.visitColumnDefinition = function(ctx) {
};


// Visit a parse tree produced by CqlParser#primaryKeyModifier.
CqlParserVisitor.prototype.visitPrimaryKeyModifier = function(ctx) {
};


// Visit a parse tree produced by CqlParser#primaryKeyElement.
CqlParserVisitor.prototype.visitPrimaryKeyElement = function(ctx) {
};


// Visit a parse tree produced by CqlParser#primaryKeyDefinition.
CqlParserVisitor.prototype.visitPrimaryKeyDefinition = function(ctx) {
};


// Visit a parse tree produced by CqlParser#primaryKeySimple.
CqlParserVisitor.prototype.visitPrimaryKeySimple = function(ctx) {
};


// Visit a parse tree produced by CqlParser#primaryKeyComposite.
CqlParserVisitor.prototype.visitPrimaryKeyComposite = function(ctx) {
};


// Visit a parse tree produced by CqlParser#compoundKey.
CqlParserVisitor.prototype.visitCompoundKey = function(ctx) {
};


// Visit a parse tree produced by CqlParser#partitionKeyList.
CqlParserVisitor.prototype.visitPartitionKeyList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#clusteringKeyList.
CqlParserVisitor.prototype.visitClusteringKeyList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#applyBatch.
CqlParserVisitor.prototype.visitApplyBatch = function(ctx) {
};


// Visit a parse tree produced by CqlParser#beginBatch.
CqlParserVisitor.prototype.visitBeginBatch = function(ctx) {
};


// Visit a parse tree produced by CqlParser#beginBatchSpec.
CqlParserVisitor.prototype.visitBeginBatchSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#batchType.
CqlParserVisitor.prototype.visitBatchType = function(ctx) {
};


// Visit a parse tree produced by CqlParser#alterKeyspace.
CqlParserVisitor.prototype.visitAlterKeyspace = function(ctx) {
};


// Visit a parse tree produced by CqlParser#replicationList.
CqlParserVisitor.prototype.visitReplicationList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#replicationListItem.
CqlParserVisitor.prototype.visitReplicationListItem = function(ctx) {
};


// Visit a parse tree produced by CqlParser#durableWrites.
CqlParserVisitor.prototype.visitDurableWrites = function(ctx) {
};


// Visit a parse tree produced by CqlParser#use.
CqlParserVisitor.prototype.visitUse = function(ctx) {
};


// Visit a parse tree produced by CqlParser#truncate.
CqlParserVisitor.prototype.visitTruncate = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createIndex.
CqlParserVisitor.prototype.visitCreateIndex = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createIndexSubject.
CqlParserVisitor.prototype.visitCreateIndexSubject = function(ctx) {
};


// Visit a parse tree produced by CqlParser#index.
CqlParserVisitor.prototype.visitIndex = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createIndexDef.
CqlParserVisitor.prototype.visitCreateIndexDef = function(ctx) {
};


// Visit a parse tree produced by CqlParser#createIndexTarget.
CqlParserVisitor.prototype.visitCreateIndexTarget = function(ctx) {
};


// Visit a parse tree produced by CqlParser#indexKeysSpec.
CqlParserVisitor.prototype.visitIndexKeysSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#indexEntriesSSpec.
CqlParserVisitor.prototype.visitIndexEntriesSSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#indexFullSpec.
CqlParserVisitor.prototype.visitIndexFullSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#delete.
CqlParserVisitor.prototype.visitDelete = function(ctx) {
};


// Visit a parse tree produced by CqlParser#deleteColumnList.
CqlParserVisitor.prototype.visitDeleteColumnList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#deleteColumnItem.
CqlParserVisitor.prototype.visitDeleteColumnItem = function(ctx) {
};


// Visit a parse tree produced by CqlParser#update.
CqlParserVisitor.prototype.visitUpdate = function(ctx) {
};


// Visit a parse tree produced by CqlParser#ifSpec.
CqlParserVisitor.prototype.visitIfSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#ifConditionList.
CqlParserVisitor.prototype.visitIfConditionList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#ifCondition.
CqlParserVisitor.prototype.visitIfCondition = function(ctx) {
};


// Visit a parse tree produced by CqlParser#updateAssignments.
CqlParserVisitor.prototype.visitUpdateAssignments = function(ctx) {
};


// Visit a parse tree produced by CqlParser#updateAssignmentElement.
CqlParserVisitor.prototype.visitUpdateAssignmentElement = function(ctx) {
};


// Visit a parse tree produced by CqlParser#assignmentSet.
CqlParserVisitor.prototype.visitAssignmentSet = function(ctx) {
};


// Visit a parse tree produced by CqlParser#assignmentMap.
CqlParserVisitor.prototype.visitAssignmentMap = function(ctx) {
};


// Visit a parse tree produced by CqlParser#assignmentList.
CqlParserVisitor.prototype.visitAssignmentList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#insert.
CqlParserVisitor.prototype.visitInsert = function(ctx) {
};


// Visit a parse tree produced by CqlParser#usingTtlTimestamp.
CqlParserVisitor.prototype.visitUsingTtlTimestamp = function(ctx) {
};


// Visit a parse tree produced by CqlParser#timestamp.
CqlParserVisitor.prototype.visitTimestamp = function(ctx) {
};


// Visit a parse tree produced by CqlParser#ttl.
CqlParserVisitor.prototype.visitTtl = function(ctx) {
};


// Visit a parse tree produced by CqlParser#usingTimestampSpec.
CqlParserVisitor.prototype.visitUsingTimestampSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#ifNotExist.
CqlParserVisitor.prototype.visitIfNotExist = function(ctx) {
};


// Visit a parse tree produced by CqlParser#ifExist.
CqlParserVisitor.prototype.visitIfExist = function(ctx) {
};


// Visit a parse tree produced by CqlParser#insertValuesSpec.
CqlParserVisitor.prototype.visitInsertValuesSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#insertColumnSpec.
CqlParserVisitor.prototype.visitInsertColumnSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#columnList.
CqlParserVisitor.prototype.visitColumnList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#baseColumnList.
CqlParserVisitor.prototype.visitBaseColumnList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#expressionList.
CqlParserVisitor.prototype.visitExpressionList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#expression.
CqlParserVisitor.prototype.visitExpression = function(ctx) {
};


// Visit a parse tree produced by CqlParser#select.
CqlParserVisitor.prototype.visitSelect = function(ctx) {
};


// Visit a parse tree produced by CqlParser#limitSpec.
CqlParserVisitor.prototype.visitLimitSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#fromSpec.
CqlParserVisitor.prototype.visitFromSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#orderSpec.
CqlParserVisitor.prototype.visitOrderSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#orderSpecElement.
CqlParserVisitor.prototype.visitOrderSpecElement = function(ctx) {
};


// Visit a parse tree produced by CqlParser#whereSpec.
CqlParserVisitor.prototype.visitWhereSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#selectElements.
CqlParserVisitor.prototype.visitSelectElements = function(ctx) {
};


// Visit a parse tree produced by CqlParser#selectElement.
CqlParserVisitor.prototype.visitSelectElement = function(ctx) {
};


// Visit a parse tree produced by CqlParser#relationElements.
CqlParserVisitor.prototype.visitRelationElements = function(ctx) {
};


// Visit a parse tree produced by CqlParser#relationElement.
CqlParserVisitor.prototype.visitRelationElement = function(ctx) {
};


// Visit a parse tree produced by CqlParser#relationElementConstant.
CqlParserVisitor.prototype.visitRelationElementConstant = function(ctx) {
};


// Visit a parse tree produced by CqlParser#relationElementIn.
CqlParserVisitor.prototype.visitRelationElementIn = function(ctx) {
};


// Visit a parse tree produced by CqlParser#relationElementToken.
CqlParserVisitor.prototype.visitRelationElementToken = function(ctx) {
};


// Visit a parse tree produced by CqlParser#relationElementTokenSpec.
CqlParserVisitor.prototype.visitRelationElementTokenSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#relationOperator.
CqlParserVisitor.prototype.visitRelationOperator = function(ctx) {
};


// Visit a parse tree produced by CqlParser#functionCall.
CqlParserVisitor.prototype.visitFunctionCall = function(ctx) {
};


// Visit a parse tree produced by CqlParser#functionArgs.
CqlParserVisitor.prototype.visitFunctionArgs = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constant.
CqlParserVisitor.prototype.visitConstant = function(ctx) {
};


// Visit a parse tree produced by CqlParser#collectionElement.
CqlParserVisitor.prototype.visitCollectionElement = function(ctx) {
};


// Visit a parse tree produced by CqlParser#collectionMapElement.
CqlParserVisitor.prototype.visitCollectionMapElement = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantCollection.
CqlParserVisitor.prototype.visitConstantCollection = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantMap.
CqlParserVisitor.prototype.visitConstantMap = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantSet.
CqlParserVisitor.prototype.visitConstantSet = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantList.
CqlParserVisitor.prototype.visitConstantList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantTuple.
CqlParserVisitor.prototype.visitConstantTuple = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantUuid.
CqlParserVisitor.prototype.visitConstantUuid = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantDecimal.
CqlParserVisitor.prototype.visitConstantDecimal = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantFloat.
CqlParserVisitor.prototype.visitConstantFloat = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantString.
CqlParserVisitor.prototype.visitConstantString = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantBoolean.
CqlParserVisitor.prototype.visitConstantBoolean = function(ctx) {
};


// Visit a parse tree produced by CqlParser#constantHexadecimal.
CqlParserVisitor.prototype.visitConstantHexadecimal = function(ctx) {
};


// Visit a parse tree produced by CqlParser#keyspace.
CqlParserVisitor.prototype.visitKeyspace = function(ctx) {
};


// Visit a parse tree produced by CqlParser#baseKeyspace.
CqlParserVisitor.prototype.visitBaseKeyspace = function(ctx) {
};


// Visit a parse tree produced by CqlParser#table.
CqlParserVisitor.prototype.visitTable = function(ctx) {
};


// Visit a parse tree produced by CqlParser#baseTable.
CqlParserVisitor.prototype.visitBaseTable = function(ctx) {
};


// Visit a parse tree produced by CqlParser#materializedView.
CqlParserVisitor.prototype.visitMaterializedView = function(ctx) {
};


// Visit a parse tree produced by CqlParser#keyspaceObject.
CqlParserVisitor.prototype.visitKeyspaceObject = function(ctx) {
};


// Visit a parse tree produced by CqlParser#objectUnknown.
CqlParserVisitor.prototype.visitObjectUnknown = function(ctx) {
};


// Visit a parse tree produced by CqlParser#aggregateSpec.
CqlParserVisitor.prototype.visitAggregateSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#typeSpec.
CqlParserVisitor.prototype.visitTypeSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#functionSpec.
CqlParserVisitor.prototype.visitFunctionSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#tableSpec.
CqlParserVisitor.prototype.visitTableSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#baseTableSpec.
CqlParserVisitor.prototype.visitBaseTableSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#indexSpec.
CqlParserVisitor.prototype.visitIndexSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#materializedViewSpec.
CqlParserVisitor.prototype.visitMaterializedViewSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#tableOrMaterializedViewSpec.
CqlParserVisitor.prototype.visitTableOrMaterializedViewSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#objectUnknownSpec.
CqlParserVisitor.prototype.visitObjectUnknownSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#columnSpec.
CqlParserVisitor.prototype.visitColumnSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#column.
CqlParserVisitor.prototype.visitColumn = function(ctx) {
};


// Visit a parse tree produced by CqlParser#columnUnknownSpec.
CqlParserVisitor.prototype.visitColumnUnknownSpec = function(ctx) {
};


// Visit a parse tree produced by CqlParser#columnUnknown.
CqlParserVisitor.prototype.visitColumnUnknown = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataType.
CqlParserVisitor.prototype.visitDataType = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeCollection.
CqlParserVisitor.prototype.visitDataTypeCollection = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeFundamental.
CqlParserVisitor.prototype.visitDataTypeFundamental = function(ctx) {
};


// Visit a parse tree produced by CqlParser#orderDirection.
CqlParserVisitor.prototype.visitOrderDirection = function(ctx) {
};


// Visit a parse tree produced by CqlParser#role.
CqlParserVisitor.prototype.visitRole = function(ctx) {
};


// Visit a parse tree produced by CqlParser#trigger.
CqlParserVisitor.prototype.visitTrigger = function(ctx) {
};


// Visit a parse tree produced by CqlParser#triggerClass.
CqlParserVisitor.prototype.visitTriggerClass = function(ctx) {
};


// Visit a parse tree produced by CqlParser#type.
CqlParserVisitor.prototype.visitType = function(ctx) {
};


// Visit a parse tree produced by CqlParser#aggregate.
CqlParserVisitor.prototype.visitAggregate = function(ctx) {
};


// Visit a parse tree produced by CqlParser#function.
CqlParserVisitor.prototype.visitFunction = function(ctx) {
};


// Visit a parse tree produced by CqlParser#language.
CqlParserVisitor.prototype.visitLanguage = function(ctx) {
};


// Visit a parse tree produced by CqlParser#user.
CqlParserVisitor.prototype.visitUser = function(ctx) {
};


// Visit a parse tree produced by CqlParser#password.
CqlParserVisitor.prototype.visitPassword = function(ctx) {
};


// Visit a parse tree produced by CqlParser#hashKey.
CqlParserVisitor.prototype.visitHashKey = function(ctx) {
};


// Visit a parse tree produced by CqlParser#param.
CqlParserVisitor.prototype.visitParam = function(ctx) {
};


// Visit a parse tree produced by CqlParser#paramName.
CqlParserVisitor.prototype.visitParamName = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAdd.
CqlParserVisitor.prototype.visitKwAdd = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAggregate.
CqlParserVisitor.prototype.visitKwAggregate = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAll.
CqlParserVisitor.prototype.visitKwAll = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAllFunctions.
CqlParserVisitor.prototype.visitKwAllFunctions = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAllKeyspaces.
CqlParserVisitor.prototype.visitKwAllKeyspaces = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAllRoles.
CqlParserVisitor.prototype.visitKwAllRoles = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAllPermissions.
CqlParserVisitor.prototype.visitKwAllPermissions = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAllow.
CqlParserVisitor.prototype.visitKwAllow = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAllowFiltering.
CqlParserVisitor.prototype.visitKwAllowFiltering = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAlter.
CqlParserVisitor.prototype.visitKwAlter = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAnd.
CqlParserVisitor.prototype.visitKwAnd = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwApply.
CqlParserVisitor.prototype.visitKwApply = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAs.
CqlParserVisitor.prototype.visitKwAs = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAsc.
CqlParserVisitor.prototype.visitKwAsc = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwAuthorize.
CqlParserVisitor.prototype.visitKwAuthorize = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwBatch.
CqlParserVisitor.prototype.visitKwBatch = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwBegin.
CqlParserVisitor.prototype.visitKwBegin = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwBy.
CqlParserVisitor.prototype.visitKwBy = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwCalled.
CqlParserVisitor.prototype.visitKwCalled = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwClustering.
CqlParserVisitor.prototype.visitKwClustering = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwCompact.
CqlParserVisitor.prototype.visitKwCompact = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwContains.
CqlParserVisitor.prototype.visitKwContains = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwContainsKey.
CqlParserVisitor.prototype.visitKwContainsKey = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwCreate.
CqlParserVisitor.prototype.visitKwCreate = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwDelete.
CqlParserVisitor.prototype.visitKwDelete = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwDesc.
CqlParserVisitor.prototype.visitKwDesc = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwDescibe.
CqlParserVisitor.prototype.visitKwDescibe = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwDistinct.
CqlParserVisitor.prototype.visitKwDistinct = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwDrop.
CqlParserVisitor.prototype.visitKwDrop = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwDurableWrites.
CqlParserVisitor.prototype.visitKwDurableWrites = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwEntries.
CqlParserVisitor.prototype.visitKwEntries = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwExecute.
CqlParserVisitor.prototype.visitKwExecute = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwExists.
CqlParserVisitor.prototype.visitKwExists = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwFiltering.
CqlParserVisitor.prototype.visitKwFiltering = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwFinalfunc.
CqlParserVisitor.prototype.visitKwFinalfunc = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwFrom.
CqlParserVisitor.prototype.visitKwFrom = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwFull.
CqlParserVisitor.prototype.visitKwFull = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwFunction.
CqlParserVisitor.prototype.visitKwFunction = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwGrant.
CqlParserVisitor.prototype.visitKwGrant = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwIf.
CqlParserVisitor.prototype.visitKwIf = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwIn.
CqlParserVisitor.prototype.visitKwIn = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwIndex.
CqlParserVisitor.prototype.visitKwIndex = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwInitcond.
CqlParserVisitor.prototype.visitKwInitcond = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwInput.
CqlParserVisitor.prototype.visitKwInput = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwInsertInto.
CqlParserVisitor.prototype.visitKwInsertInto = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwIs.
CqlParserVisitor.prototype.visitKwIs = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwKey.
CqlParserVisitor.prototype.visitKwKey = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwKeys.
CqlParserVisitor.prototype.visitKwKeys = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwKeyspace.
CqlParserVisitor.prototype.visitKwKeyspace = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwLanguage.
CqlParserVisitor.prototype.visitKwLanguage = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwLimit.
CqlParserVisitor.prototype.visitKwLimit = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwList.
CqlParserVisitor.prototype.visitKwList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwListRoles.
CqlParserVisitor.prototype.visitKwListRoles = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwListUsers.
CqlParserVisitor.prototype.visitKwListUsers = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwLogged.
CqlParserVisitor.prototype.visitKwLogged = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwLogin.
CqlParserVisitor.prototype.visitKwLogin = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwMaterializedView.
CqlParserVisitor.prototype.visitKwMaterializedView = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwModify.
CqlParserVisitor.prototype.visitKwModify = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwNosuperuser.
CqlParserVisitor.prototype.visitKwNosuperuser = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwNorecursive.
CqlParserVisitor.prototype.visitKwNorecursive = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwNot.
CqlParserVisitor.prototype.visitKwNot = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwNull.
CqlParserVisitor.prototype.visitKwNull = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwOf.
CqlParserVisitor.prototype.visitKwOf = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwOn.
CqlParserVisitor.prototype.visitKwOn = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwOptions.
CqlParserVisitor.prototype.visitKwOptions = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwOr.
CqlParserVisitor.prototype.visitKwOr = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwOrder.
CqlParserVisitor.prototype.visitKwOrder = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwOrderBy.
CqlParserVisitor.prototype.visitKwOrderBy = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwPartition.
CqlParserVisitor.prototype.visitKwPartition = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwPassword.
CqlParserVisitor.prototype.visitKwPassword = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwPermissions.
CqlParserVisitor.prototype.visitKwPermissions = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwPrimary.
CqlParserVisitor.prototype.visitKwPrimary = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwRename.
CqlParserVisitor.prototype.visitKwRename = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwReplace.
CqlParserVisitor.prototype.visitKwReplace = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwReplication.
CqlParserVisitor.prototype.visitKwReplication = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwReturns.
CqlParserVisitor.prototype.visitKwReturns = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwRole.
CqlParserVisitor.prototype.visitKwRole = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwSelect.
CqlParserVisitor.prototype.visitKwSelect = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwSet.
CqlParserVisitor.prototype.visitKwSet = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwSfunc.
CqlParserVisitor.prototype.visitKwSfunc = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwStatic.
CqlParserVisitor.prototype.visitKwStatic = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwStorage.
CqlParserVisitor.prototype.visitKwStorage = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwStype.
CqlParserVisitor.prototype.visitKwStype = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwSuperuser.
CqlParserVisitor.prototype.visitKwSuperuser = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwTable.
CqlParserVisitor.prototype.visitKwTable = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwTimestamp.
CqlParserVisitor.prototype.visitKwTimestamp = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwTo.
CqlParserVisitor.prototype.visitKwTo = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwToken.
CqlParserVisitor.prototype.visitKwToken = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwTrigger.
CqlParserVisitor.prototype.visitKwTrigger = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwTruncate.
CqlParserVisitor.prototype.visitKwTruncate = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwTtl.
CqlParserVisitor.prototype.visitKwTtl = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwType.
CqlParserVisitor.prototype.visitKwType = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwUnlogged.
CqlParserVisitor.prototype.visitKwUnlogged = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwUpdate.
CqlParserVisitor.prototype.visitKwUpdate = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwUse.
CqlParserVisitor.prototype.visitKwUse = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwUser.
CqlParserVisitor.prototype.visitKwUser = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwUsers.
CqlParserVisitor.prototype.visitKwUsers = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwUsing.
CqlParserVisitor.prototype.visitKwUsing = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwValues.
CqlParserVisitor.prototype.visitKwValues = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwWhere.
CqlParserVisitor.prototype.visitKwWhere = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwWith.
CqlParserVisitor.prototype.visitKwWith = function(ctx) {
};


// Visit a parse tree produced by CqlParser#kwRevoke.
CqlParserVisitor.prototype.visitKwRevoke = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeAscii.
CqlParserVisitor.prototype.visitDataTypeAscii = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeBigint.
CqlParserVisitor.prototype.visitDataTypeBigint = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeBlob.
CqlParserVisitor.prototype.visitDataTypeBlob = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeBoolean.
CqlParserVisitor.prototype.visitDataTypeBoolean = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeCounter.
CqlParserVisitor.prototype.visitDataTypeCounter = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeDate.
CqlParserVisitor.prototype.visitDataTypeDate = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeDecimal.
CqlParserVisitor.prototype.visitDataTypeDecimal = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeDouble.
CqlParserVisitor.prototype.visitDataTypeDouble = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeFloat.
CqlParserVisitor.prototype.visitDataTypeFloat = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeFrozen.
CqlParserVisitor.prototype.visitDataTypeFrozen = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeInet.
CqlParserVisitor.prototype.visitDataTypeInet = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeInt.
CqlParserVisitor.prototype.visitDataTypeInt = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeList.
CqlParserVisitor.prototype.visitDataTypeList = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeMap.
CqlParserVisitor.prototype.visitDataTypeMap = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeSmallInt.
CqlParserVisitor.prototype.visitDataTypeSmallInt = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeSet.
CqlParserVisitor.prototype.visitDataTypeSet = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeText.
CqlParserVisitor.prototype.visitDataTypeText = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeTime.
CqlParserVisitor.prototype.visitDataTypeTime = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeTimeUuid.
CqlParserVisitor.prototype.visitDataTypeTimeUuid = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeTimestamp.
CqlParserVisitor.prototype.visitDataTypeTimestamp = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeTinyInt.
CqlParserVisitor.prototype.visitDataTypeTinyInt = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeTuple.
CqlParserVisitor.prototype.visitDataTypeTuple = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeUserDefined.
CqlParserVisitor.prototype.visitDataTypeUserDefined = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeUuid.
CqlParserVisitor.prototype.visitDataTypeUuid = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeVarChar.
CqlParserVisitor.prototype.visitDataTypeVarChar = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeVarInt.
CqlParserVisitor.prototype.visitDataTypeVarInt = function(ctx) {
};


// Visit a parse tree produced by CqlParser#dataTypeStructure.
CqlParserVisitor.prototype.visitDataTypeStructure = function(ctx) {
};


// Visit a parse tree produced by CqlParser#specialStar.
CqlParserVisitor.prototype.visitSpecialStar = function(ctx) {
};


// Visit a parse tree produced by CqlParser#specialDot.
CqlParserVisitor.prototype.visitSpecialDot = function(ctx) {
};


// Visit a parse tree produced by CqlParser#eof.
CqlParserVisitor.prototype.visitEof = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxBracketLr.
CqlParserVisitor.prototype.visitSyntaxBracketLr = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxBracketRr.
CqlParserVisitor.prototype.visitSyntaxBracketRr = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxBracketLc.
CqlParserVisitor.prototype.visitSyntaxBracketLc = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxBracketRc.
CqlParserVisitor.prototype.visitSyntaxBracketRc = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxBracketLa.
CqlParserVisitor.prototype.visitSyntaxBracketLa = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxBracketRa.
CqlParserVisitor.prototype.visitSyntaxBracketRa = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxBracketLs.
CqlParserVisitor.prototype.visitSyntaxBracketLs = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxBracketRs.
CqlParserVisitor.prototype.visitSyntaxBracketRs = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxComma.
CqlParserVisitor.prototype.visitSyntaxComma = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxColon.
CqlParserVisitor.prototype.visitSyntaxColon = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxPlus.
CqlParserVisitor.prototype.visitSyntaxPlus = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxMinus.
CqlParserVisitor.prototype.visitSyntaxMinus = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxSquote.
CqlParserVisitor.prototype.visitSyntaxSquote = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxDquote.
CqlParserVisitor.prototype.visitSyntaxDquote = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxOperatorEq.
CqlParserVisitor.prototype.visitSyntaxOperatorEq = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxOperatorLt.
CqlParserVisitor.prototype.visitSyntaxOperatorLt = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxOperatorGt.
CqlParserVisitor.prototype.visitSyntaxOperatorGt = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxOperatorLte.
CqlParserVisitor.prototype.visitSyntaxOperatorLte = function(ctx) {
};


// Visit a parse tree produced by CqlParser#syntaxOperatorGte.
CqlParserVisitor.prototype.visitSyntaxOperatorGte = function(ctx) {
};



exports.CqlParserVisitor = CqlParserVisitor;