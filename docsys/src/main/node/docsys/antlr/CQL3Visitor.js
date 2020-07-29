// Generated from CQL3.g4 by ANTLR 4.5
// jshint ignore: start
var antlr4 = require('antlr4/index');

// This class defines a complete generic visitor for a parse tree produced by CQL3Parser.

function CQL3Visitor() {
	antlr4.tree.ParseTreeVisitor.call(this);
	return this;
}

CQL3Visitor.prototype = Object.create(antlr4.tree.ParseTreeVisitor.prototype);
CQL3Visitor.prototype.constructor = CQL3Visitor;

// Visit a parse tree produced by CQL3Parser#statements.
CQL3Visitor.prototype.visitStatements = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#statement.
CQL3Visitor.prototype.visitStatement = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#dml_statements.
CQL3Visitor.prototype.visitDml_statements = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#dml_statement.
CQL3Visitor.prototype.visitDml_statement = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#create_keyspace_stmt.
CQL3Visitor.prototype.visitCreate_keyspace_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#alter_keyspace_stmt.
CQL3Visitor.prototype.visitAlter_keyspace_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#drop_keyspace_stmt.
CQL3Visitor.prototype.visitDrop_keyspace_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#use_stmt.
CQL3Visitor.prototype.visitUse_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#create_table_stmt.
CQL3Visitor.prototype.visitCreate_table_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#alter_table_stmt.
CQL3Visitor.prototype.visitAlter_table_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#alter_table_instruction.
CQL3Visitor.prototype.visitAlter_table_instruction = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#drop_table_stmt.
CQL3Visitor.prototype.visitDrop_table_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#truncate_table_stmt.
CQL3Visitor.prototype.visitTruncate_table_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#create_index_stmt.
CQL3Visitor.prototype.visitCreate_index_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#drop_index_stmt.
CQL3Visitor.prototype.visitDrop_index_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#insert_stmt.
CQL3Visitor.prototype.visitInsert_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#column_names.
CQL3Visitor.prototype.visitColumn_names = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#column_values.
CQL3Visitor.prototype.visitColumn_values = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#upsert_options.
CQL3Visitor.prototype.visitUpsert_options = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#upsert_option.
CQL3Visitor.prototype.visitUpsert_option = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#index_name.
CQL3Visitor.prototype.visitIndex_name = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#index_class.
CQL3Visitor.prototype.visitIndex_class = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#index_options.
CQL3Visitor.prototype.visitIndex_options = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#update_stmt.
CQL3Visitor.prototype.visitUpdate_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#update_assignments.
CQL3Visitor.prototype.visitUpdate_assignments = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#update_assignment.
CQL3Visitor.prototype.visitUpdate_assignment = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#update_conditions.
CQL3Visitor.prototype.visitUpdate_conditions = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#update_condition.
CQL3Visitor.prototype.visitUpdate_condition = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#where_clause.
CQL3Visitor.prototype.visitWhere_clause = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#relation.
CQL3Visitor.prototype.visitRelation = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#delete_stmt.
CQL3Visitor.prototype.visitDelete_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#delete_conditions.
CQL3Visitor.prototype.visitDelete_conditions = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#delete_condition.
CQL3Visitor.prototype.visitDelete_condition = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#delete_selections.
CQL3Visitor.prototype.visitDelete_selections = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#delete_selection.
CQL3Visitor.prototype.visitDelete_selection = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#batch_stmt.
CQL3Visitor.prototype.visitBatch_stmt = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#batch_options.
CQL3Visitor.prototype.visitBatch_options = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#batch_option.
CQL3Visitor.prototype.visitBatch_option = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#table_name.
CQL3Visitor.prototype.visitTable_name = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#table_name_noks.
CQL3Visitor.prototype.visitTable_name_noks = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#column_name.
CQL3Visitor.prototype.visitColumn_name = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#table_options.
CQL3Visitor.prototype.visitTable_options = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#table_option.
CQL3Visitor.prototype.visitTable_option = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#asc_or_desc.
CQL3Visitor.prototype.visitAsc_or_desc = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#column_definitions.
CQL3Visitor.prototype.visitColumn_definitions = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#column_definition.
CQL3Visitor.prototype.visitColumn_definition = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#column_type.
CQL3Visitor.prototype.visitColumn_type = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#primary_key.
CQL3Visitor.prototype.visitPrimary_key = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#partition_key.
CQL3Visitor.prototype.visitPartition_key = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#clustering_column.
CQL3Visitor.prototype.visitClustering_column = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#keyspace_name.
CQL3Visitor.prototype.visitKeyspace_name = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#if_not_exists.
CQL3Visitor.prototype.visitIf_not_exists = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#if_exists.
CQL3Visitor.prototype.visitIf_exists = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#constant.
CQL3Visitor.prototype.visitConstant = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#variable.
CQL3Visitor.prototype.visitVariable = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#term.
CQL3Visitor.prototype.visitTerm = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#collection.
CQL3Visitor.prototype.visitCollection = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#map.
CQL3Visitor.prototype.visitMap = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#set.
CQL3Visitor.prototype.visitSet = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#list.
CQL3Visitor.prototype.visitList = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#function.
CQL3Visitor.prototype.visitFunction = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#properties.
CQL3Visitor.prototype.visitProperties = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#property.
CQL3Visitor.prototype.visitProperty = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#property_name.
CQL3Visitor.prototype.visitProperty_name = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#property_value.
CQL3Visitor.prototype.visitProperty_value = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#data_type.
CQL3Visitor.prototype.visitData_type = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#native_type.
CQL3Visitor.prototype.visitNative_type = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#collection_type.
CQL3Visitor.prototype.visitCollection_type = function(ctx) {
};


// Visit a parse tree produced by CQL3Parser#bool.
CQL3Visitor.prototype.visitBool = function(ctx) {
};



exports.CQL3Visitor = CQL3Visitor;