package io.nosqlbench.generators.cql.lang;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.generators.cql.generated.CqlParser;
import io.nosqlbench.generators.cql.generated.CqlParserBaseListener;

import java.util.ArrayList;
import java.util.List;

public class CQLAstBuilder extends CqlParserBaseListener {

    CqlWorkloadBuffer buf = new CqlWorkloadBuffer();

    @Override
    public void exitCreateTable(CqlParser.CreateTableContext ctx) {
        buf.newTable(ctx.keyspace().getText(), ctx.table().getText());
    }

    @Override
    public void exitColumnDefinition(CqlParser.ColumnDefinitionContext ctx) {
        List<String> typedef = new ArrayList<>();

        CqlParser.DataTypeContext dt = ctx.dataType();

        typedef.add(dt.dataTypeName().getText());
        CqlParser.DataTypeDefinitionContext dtd = dt.dataTypeDefinition();
        if (dtd != null) {
            typedef.add("<");
            dtd.dataTypeName().forEach(dtn -> {
                typedef.add(dtn.getText());
                typedef.add(",");
            });
        }
        typedef.remove(typedef.size() - 1);

        buf.newColumn(
            ctx.column().OBJECT_NAME().getText(),
            typedef.toArray(new String[0])
        );
    }

    @Override
    public void exitPrimaryKeyDefinition(CqlParser.PrimaryKeyDefinitionContext ctx) {
        super.exitPrimaryKeyDefinition(ctx);
    }

    @Override
    public void exitPrimaryKeyColumn(CqlParser.PrimaryKeyColumnContext ctx) {
    }

    @Override
    public void exitPartitionKey(CqlParser.PartitionKeyContext ctx) {
    }

    @Override
    public void exitClusteringKey(CqlParser.ClusteringKeyContext ctx) {
    }

    // This is the form of a primary key that is tacked onto the end of a column def
    @Override
    public void enterPrimaryKeyColumn(CqlParser.PrimaryKeyColumnContext ctx) {
    }

    // This is the form of a primary key that is added to the column def list as an element
    @Override
    public void enterPrimaryKeyElement(CqlParser.PrimaryKeyElementContext ctx) {
    }

//    @Override
//    public void exitCreateTable(CqlParser.CreateTableContext ctx) {
//        List<CqlParser.ColumnDefinitionContext> columnDefinitionContexts =
//            ctx.columnDefinitionList().columnDefinition();
//        for (CqlParser.ColumnDefinitionContext coldef : columnDefinitionContexts) {
//            CqlParser.ColumnContext column = coldef.column();
//            Token symbol = column.OBJECT_NAME().getSymbol();
//            CqlParser.DataTypeContext datatype = coldef.dataType();
//            CqlParser.DataTypeNameContext dtn = datatype.dataTypeName();
//            CqlParser.DataTypeDefinitionContext dtd = datatype.dataTypeDefinition();
//            if (dtd != null) {
//                List<CqlParser.DataTypeNameContext> dataTypeNameContexts = dtd.dataTypeName();
//                for (CqlParser.DataTypeNameContext dtnc : dataTypeNameContexts) {
//                    System.out.println("here");
//                }
//            }
//        }
//    }

}
