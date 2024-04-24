/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.cqlgen.model;


import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import io.nosqlbench.cqlgen.generated.CqlParser;
import io.nosqlbench.cqlgen.generated.CqlParserBaseListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.IntStream;

public class CqlModelBuilder extends CqlParserBaseListener {
    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/buidler");

    private final CGErrorListener errorListener;
    private final CqlModel model;
    private long counted;
    private int colindex;
    private CqlKeyspaceDef keyspace;
    private CqlType usertype;
    private CqlTable table;

    public CqlModelBuilder(CGErrorListener errorListener) {
        this.errorListener = errorListener;
        this.model = new CqlModel(errorListener);
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        if ((counted++ & 0b11111111111111) == 0b10000000000000) {
            logger.trace(() -> "parsed " + counted + " elements...");
        }
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        System.out.println("error parsing: " + node.toString());
        ParseTree parent = node.getParent();
        String errorNodeType = parent.getClass().getSimpleName();

        logger.info(() -> "PARSE ERROR: " + errorNodeType + "\n" + node.getSourceInterval());

        super.visitErrorNode(node);
    }

    @Override
    public void enterCreateKeyspace(CqlParser.CreateKeyspaceContext ctx) {
        this.keyspace = new CqlKeyspaceDef();
    }

    @Override
    public void exitCreateKeyspace(CqlParser.CreateKeyspaceContext ctx) {
        saveKeyspace(
            ctx.keyspace().getText()
        );
    }

    public void saveKeyspace(String keyspaceName) {
        this.keyspace.setKeyspaceName(keyspaceName);
        keyspace.validate();
        keyspace.setDefined();
        model.addKeyspace(keyspace);
        this.keyspace = null;
    }


    @Override
    public void exitReplicationList(CqlParser.ReplicationListContext ctx) {
        String repldata = textOf(ctx);
        keyspace.setReplicationData(repldata);
    }

    @Override
    public void enterCreateTable(CqlParser.CreateTableContext ctx) {
        this.table = new CqlTable();
    }

    @Override
    public void exitPrimaryKeyColumn(CqlParser.PrimaryKeyColumnContext ctx) {
        super.exitPrimaryKeyColumn(ctx);
    }

    @Override
    public void exitPrimaryKeyDefinition(CqlParser.PrimaryKeyDefinitionContext ctx) {
        // PRIMARY KEY (PK)
        if (ctx.singlePrimaryKey() != null) {
            addPartitionKey(ctx.singlePrimaryKey().column().getText());
        } else
            // PRIMARY KEY (PK, ck, ...)
            if (ctx.compositeKey() != null) {
                if (ctx.compositeKey().partitionKeyList() != null) {
                    for (CqlParser.PartitionKeyContext pkctx : ctx.compositeKey().partitionKeyList().partitionKey()) {
                        addPartitionKey(pkctx.column().getText());
                    }
                }
                // PRIMARY KEY ((pk,...),(CK,...))
                if (ctx.compositeKey().clusteringKeyList() != null) {
                    for (CqlParser.ClusteringKeyContext ccol : ctx.compositeKey().clusteringKeyList().clusteringKey()) {
                        addClusteringColumn(ccol.column().getText());
                    }
                }
            } else
                // PRIMARY KEY ((PK), CK, ...)
                if (ctx.compoundKey() != null) {
                addPartitionKey(ctx.compoundKey().partitionKey().getText());
                for (CqlParser.ClusteringKeyContext ccol : ctx.compoundKey().clusteringKeyList().clusteringKey()) {
                    addClusteringColumn(ccol.column().getText());
                }
            }
    }

    @Override
    public void enterCreateType(CqlParser.CreateTypeContext ctx) {
        this.usertype = new CqlType();
    }

    @Override
    public void exitCreateType(CqlParser.CreateTypeContext ctx) {
        String keyspace = ctx.keyspace().getText();
        String name = ctx.type_().getText();
        usertype.setName(name);
        usertype.setDefined();
        model.addType(keyspace, usertype);
        usertype.validate();
        usertype = null;
    }

    @Override
    public void exitTypeMemberColumnList(CqlParser.TypeMemberColumnListContext ctx) {
        List<CqlParser.ColumnContext> columns = ctx.column();
        List<CqlParser.DataTypeContext> dataTypes = ctx.dataType();
        for (int idx = 0; idx < columns.size(); idx++) {
            addTypeField(
                new CqlTypeColumn(columns.get(idx).getText(), dataTypes.get(idx).getText(), usertype)
            );
        }
    }

    @Override
    public void exitSinglePrimaryKey(CqlParser.SinglePrimaryKeyContext ctx) {
        super.exitSinglePrimaryKey(ctx);
    }

    @Override
    public void exitCreateTable(CqlParser.CreateTableContext ctx) {
        table.setName(ctx.table().getText());
        model.addTable(ctx.keyspace().getText(), table);
        table = null;
    }

    @Override
    public void exitOrderDirection(CqlParser.OrderDirectionContext ctx) {
    }

    @Override
    public void exitTableOptionItem(CqlParser.TableOptionItemContext ctx) {
        if (table!=null) {
            table.setCompactStorage(ctx.kwCompactStorage() != null);
        } else {
            logger.debug("table option item found with no table, this is likely for a materialized view");
        }
    }

    @Override
    public void exitDurableWrites(CqlParser.DurableWritesContext ctx) {
        keyspace.setDurableWrites(Boolean.parseBoolean(ctx.booleanLiteral().getText()));
    }

    @Override
    public void exitClusteringOrder(CqlParser.ClusteringOrderContext ctx) {

        List<String> columns = ctx.children.stream()
            .filter(c -> c instanceof CqlParser.ColumnContext)
            .map(c -> c.getText())
            .toList();

        List<String> orders = ctx.children.stream()
            .filter(c -> c instanceof CqlParser.OrderDirectionContext)
            .map(c -> c.getText())
            .toList();

        if (table!=null) {
            IntStream.range(0, columns.size())
                .forEach(i -> table.addTableClusteringOrder(columns.get(i), orders.get(i)));
        } else {
            logger.debug("clustering order found, but not active table. This is likely for a materialized view.");
        }
    }

    private String textOf(ParserRuleContext ctx) {
        int startIndex = ctx.start.getStartIndex();
        int stopIndex = ctx.stop.getStopIndex();
        Interval interval = Interval.of(startIndex, stopIndex);
        String text = ctx.start.getInputStream().getText(interval);
        return text;
    }

    @Override
    public void enterColumnDefinitionList(CqlParser.ColumnDefinitionListContext ctx) {
        this.colindex = 0;
    }

    @Override
    public void exitColumnDefinition(CqlParser.ColumnDefinitionContext ctx) {

        if (usertype != null) {
            CqlTypeColumn coldef = new CqlTypeColumn(
                ctx.column().getText(),
                ctx.dataType().getText(),
                usertype
            );
            usertype.addColumn(coldef);
        } else if (table != null) {
            CqlTableColumn coldef = new CqlTableColumn(
                ctx.column().getText(),
                ctx.dataType().getText(),
                table
            );
            table.addcolumnDef(coldef);
            if (ctx.primaryKeyColumn() != null) {
                table.addPartitionKey(ctx.column().getText());
            }
        }

    }

    @Override
    public String toString() {
        return model.toString();
    }


    public CqlModel getModel() {
        return model;
    }

    public List<String> getErrors() {
        return model.getErrors();
    }

    public void addPartitionKey(String partitionKey) {
        table.addPartitionKey(partitionKey);
    }

    public void addClusteringColumn(String ccolumn) {
        table.addClusteringColumn(ccolumn);
    }

    public void addTypeField(CqlTypeColumn coldef) {
        this.usertype.addColumn(coldef);
    }


}
