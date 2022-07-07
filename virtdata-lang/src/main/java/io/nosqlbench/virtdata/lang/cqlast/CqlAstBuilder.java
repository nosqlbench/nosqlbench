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

package io.nosqlbench.virtdata.lang.cqlast;

import io.nosqlbench.virtdata.lang.generated.CqlParser;
import io.nosqlbench.virtdata.lang.generated.CqlParserBaseListener;

public class CqlAstBuilder extends CqlParserBaseListener {

    CqlKeyspace keyspace = new CqlKeyspace();

    @Override
    public void enterCreateTable(CqlParser.CreateTableContext ctx) {
        keyspace.addTable();
    }

    @Override
    public void exitCreateTable(CqlParser.CreateTableContext ctx) {
        keyspace.setTableName(ctx.table().OBJECT_NAME().getSymbol().getText());
    }

    @Override
    public void enterColumnDefinition(CqlParser.ColumnDefinitionContext ctx) {
        System.out.println("here");
    }

    @Override
    public void exitColumnDefinition(CqlParser.ColumnDefinitionContext ctx) {
        System.out.println("here");
        keyspace.addTableColumn(
            ctx.dataType().dataTypeName().getText(),
            ctx.column().OBJECT_NAME().getSymbol().getText()
        );
    }

    @Override
    public String toString() {
        return "CqlAstBuilder{" +
            "keyspace=" + keyspace +
            '}';
    }
}
