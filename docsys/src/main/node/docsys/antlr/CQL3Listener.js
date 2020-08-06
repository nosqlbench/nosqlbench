// Generated from CQL3.g4 by ANTLR 4.5
// jshint ignore: start
var antlr4 = require('antlr4/index');

// This class defines a complete listener for a parse tree produced by CQL3Parser.
function CQL3Listener() {
	antlr4.tree.ParseTreeListener.call(this);
	return this;
}

CQL3Listener.prototype = Object.create(antlr4.tree.ParseTreeListener.prototype);
CQL3Listener.prototype.constructor = CQL3Listener;

// Enter a parse tree produced by CQL3Parser#statements.
CQL3Listener.prototype.enterStatements = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#statements.
CQL3Listener.prototype.exitStatements = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#statement.
CQL3Listener.prototype.enterStatement = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#statement.
CQL3Listener.prototype.exitStatement = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#dml_statements.
CQL3Listener.prototype.enterDml_statements = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#dml_statements.
CQL3Listener.prototype.exitDml_statements = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#dml_statement.
CQL3Listener.prototype.enterDml_statement = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#dml_statement.
CQL3Listener.prototype.exitDml_statement = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#create_keyspace_stmt.
CQL3Listener.prototype.enterCreate_keyspace_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#create_keyspace_stmt.
CQL3Listener.prototype.exitCreate_keyspace_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#alter_keyspace_stmt.
CQL3Listener.prototype.enterAlter_keyspace_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#alter_keyspace_stmt.
CQL3Listener.prototype.exitAlter_keyspace_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#drop_keyspace_stmt.
CQL3Listener.prototype.enterDrop_keyspace_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#drop_keyspace_stmt.
CQL3Listener.prototype.exitDrop_keyspace_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#use_stmt.
CQL3Listener.prototype.enterUse_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#use_stmt.
CQL3Listener.prototype.exitUse_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#create_table_stmt.
CQL3Listener.prototype.enterCreate_table_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#create_table_stmt.
CQL3Listener.prototype.exitCreate_table_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#alter_table_stmt.
CQL3Listener.prototype.enterAlter_table_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#alter_table_stmt.
CQL3Listener.prototype.exitAlter_table_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#alter_table_instruction.
CQL3Listener.prototype.enterAlter_table_instruction = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#alter_table_instruction.
CQL3Listener.prototype.exitAlter_table_instruction = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#drop_table_stmt.
CQL3Listener.prototype.enterDrop_table_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#drop_table_stmt.
CQL3Listener.prototype.exitDrop_table_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#truncate_table_stmt.
CQL3Listener.prototype.enterTruncate_table_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#truncate_table_stmt.
CQL3Listener.prototype.exitTruncate_table_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#create_index_stmt.
CQL3Listener.prototype.enterCreate_index_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#create_index_stmt.
CQL3Listener.prototype.exitCreate_index_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#drop_index_stmt.
CQL3Listener.prototype.enterDrop_index_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#drop_index_stmt.
CQL3Listener.prototype.exitDrop_index_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#insert_stmt.
CQL3Listener.prototype.enterInsert_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#insert_stmt.
CQL3Listener.prototype.exitInsert_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#column_names.
CQL3Listener.prototype.enterColumn_names = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#column_names.
CQL3Listener.prototype.exitColumn_names = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#column_values.
CQL3Listener.prototype.enterColumn_values = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#column_values.
CQL3Listener.prototype.exitColumn_values = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#upsert_options.
CQL3Listener.prototype.enterUpsert_options = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#upsert_options.
CQL3Listener.prototype.exitUpsert_options = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#upsert_option.
CQL3Listener.prototype.enterUpsert_option = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#upsert_option.
CQL3Listener.prototype.exitUpsert_option = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#index_name.
CQL3Listener.prototype.enterIndex_name = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#index_name.
CQL3Listener.prototype.exitIndex_name = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#index_class.
CQL3Listener.prototype.enterIndex_class = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#index_class.
CQL3Listener.prototype.exitIndex_class = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#index_options.
CQL3Listener.prototype.enterIndex_options = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#index_options.
CQL3Listener.prototype.exitIndex_options = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#update_stmt.
CQL3Listener.prototype.enterUpdate_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#update_stmt.
CQL3Listener.prototype.exitUpdate_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#update_assignments.
CQL3Listener.prototype.enterUpdate_assignments = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#update_assignments.
CQL3Listener.prototype.exitUpdate_assignments = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#update_assignment.
CQL3Listener.prototype.enterUpdate_assignment = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#update_assignment.
CQL3Listener.prototype.exitUpdate_assignment = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#update_conditions.
CQL3Listener.prototype.enterUpdate_conditions = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#update_conditions.
CQL3Listener.prototype.exitUpdate_conditions = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#update_condition.
CQL3Listener.prototype.enterUpdate_condition = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#update_condition.
CQL3Listener.prototype.exitUpdate_condition = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#where_clause.
CQL3Listener.prototype.enterWhere_clause = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#where_clause.
CQL3Listener.prototype.exitWhere_clause = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#relation.
CQL3Listener.prototype.enterRelation = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#relation.
CQL3Listener.prototype.exitRelation = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#delete_stmt.
CQL3Listener.prototype.enterDelete_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#delete_stmt.
CQL3Listener.prototype.exitDelete_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#delete_conditions.
CQL3Listener.prototype.enterDelete_conditions = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#delete_conditions.
CQL3Listener.prototype.exitDelete_conditions = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#delete_condition.
CQL3Listener.prototype.enterDelete_condition = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#delete_condition.
CQL3Listener.prototype.exitDelete_condition = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#delete_selections.
CQL3Listener.prototype.enterDelete_selections = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#delete_selections.
CQL3Listener.prototype.exitDelete_selections = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#delete_selection.
CQL3Listener.prototype.enterDelete_selection = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#delete_selection.
CQL3Listener.prototype.exitDelete_selection = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#batch_stmt.
CQL3Listener.prototype.enterBatch_stmt = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#batch_stmt.
CQL3Listener.prototype.exitBatch_stmt = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#batch_options.
CQL3Listener.prototype.enterBatch_options = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#batch_options.
CQL3Listener.prototype.exitBatch_options = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#batch_option.
CQL3Listener.prototype.enterBatch_option = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#batch_option.
CQL3Listener.prototype.exitBatch_option = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#table_name.
CQL3Listener.prototype.enterTable_name = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#table_name.
CQL3Listener.prototype.exitTable_name = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#table_name_noks.
CQL3Listener.prototype.enterTable_name_noks = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#table_name_noks.
CQL3Listener.prototype.exitTable_name_noks = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#column_name.
CQL3Listener.prototype.enterColumn_name = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#column_name.
CQL3Listener.prototype.exitColumn_name = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#table_options.
CQL3Listener.prototype.enterTable_options = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#table_options.
CQL3Listener.prototype.exitTable_options = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#table_option.
CQL3Listener.prototype.enterTable_option = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#table_option.
CQL3Listener.prototype.exitTable_option = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#asc_or_desc.
CQL3Listener.prototype.enterAsc_or_desc = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#asc_or_desc.
CQL3Listener.prototype.exitAsc_or_desc = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#column_definitions.
CQL3Listener.prototype.enterColumn_definitions = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#column_definitions.
CQL3Listener.prototype.exitColumn_definitions = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#column_definition.
CQL3Listener.prototype.enterColumn_definition = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#column_definition.
CQL3Listener.prototype.exitColumn_definition = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#column_type.
CQL3Listener.prototype.enterColumn_type = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#column_type.
CQL3Listener.prototype.exitColumn_type = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#primary_key.
CQL3Listener.prototype.enterPrimary_key = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#primary_key.
CQL3Listener.prototype.exitPrimary_key = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#partition_key.
CQL3Listener.prototype.enterPartition_key = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#partition_key.
CQL3Listener.prototype.exitPartition_key = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#clustering_column.
CQL3Listener.prototype.enterClustering_column = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#clustering_column.
CQL3Listener.prototype.exitClustering_column = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#keyspace_name.
CQL3Listener.prototype.enterKeyspace_name = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#keyspace_name.
CQL3Listener.prototype.exitKeyspace_name = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#if_not_exists.
CQL3Listener.prototype.enterIf_not_exists = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#if_not_exists.
CQL3Listener.prototype.exitIf_not_exists = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#if_exists.
CQL3Listener.prototype.enterIf_exists = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#if_exists.
CQL3Listener.prototype.exitIf_exists = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#constant.
CQL3Listener.prototype.enterConstant = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#constant.
CQL3Listener.prototype.exitConstant = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#variable.
CQL3Listener.prototype.enterVariable = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#variable.
CQL3Listener.prototype.exitVariable = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#term.
CQL3Listener.prototype.enterTerm = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#term.
CQL3Listener.prototype.exitTerm = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#collection.
CQL3Listener.prototype.enterCollection = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#collection.
CQL3Listener.prototype.exitCollection = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#map.
CQL3Listener.prototype.enterMap = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#map.
CQL3Listener.prototype.exitMap = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#set.
CQL3Listener.prototype.enterSet = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#set.
CQL3Listener.prototype.exitSet = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#list.
CQL3Listener.prototype.enterList = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#list.
CQL3Listener.prototype.exitList = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#function.
CQL3Listener.prototype.enterFunction = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#function.
CQL3Listener.prototype.exitFunction = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#properties.
CQL3Listener.prototype.enterProperties = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#properties.
CQL3Listener.prototype.exitProperties = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#property.
CQL3Listener.prototype.enterProperty = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#property.
CQL3Listener.prototype.exitProperty = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#property_name.
CQL3Listener.prototype.enterProperty_name = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#property_name.
CQL3Listener.prototype.exitProperty_name = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#property_value.
CQL3Listener.prototype.enterProperty_value = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#property_value.
CQL3Listener.prototype.exitProperty_value = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#data_type.
CQL3Listener.prototype.enterData_type = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#data_type.
CQL3Listener.prototype.exitData_type = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#native_type.
CQL3Listener.prototype.enterNative_type = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#native_type.
CQL3Listener.prototype.exitNative_type = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#collection_type.
CQL3Listener.prototype.enterCollection_type = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#collection_type.
CQL3Listener.prototype.exitCollection_type = function(ctx) {
};


// Enter a parse tree produced by CQL3Parser#bool.
CQL3Listener.prototype.enterBool = function(ctx) {
};

// Exit a parse tree produced by CQL3Parser#bool.
CQL3Listener.prototype.exitBool = function(ctx) {
};



exports.CQL3Listener = CQL3Listener;