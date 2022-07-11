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

package io.nosqlbench.converters.cql.cqlast;

import io.nosqlbench.converters.cql.generated.CqlParser;
import io.nosqlbench.converters.cql.generated.CqlParserBaseListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

public class CqlModelBuilder extends CqlParserBaseListener {
    CqlModel model = new CqlModel();

    @Override
    public void enterCreateKeyspace(CqlParser.CreateKeyspaceContext ctx) {
        model.newKeyspace();
    }

    @Override
    public void exitCreateKeyspace(CqlParser.CreateKeyspaceContext ctx) {
        model.saveKeyspace(
            ctx.keyspace().getText(),
            textOf(ctx)
        );
    }

    @Override
    public void enterCreateTable(CqlParser.CreateTableContext ctx) {
        model.newTable();
    }

    @Override
    public void exitCreateTable(CqlParser.CreateTableContext ctx) {
        model.saveTable(
            ctx.keyspace().OBJECT_NAME().getText(),
            ctx.table().OBJECT_NAME().getText(),
            textOf(ctx)
        );
    }

    private String textOf(ParserRuleContext ctx) {
        int startIndex = ctx.start.getStartIndex();
        int stopIndex = ctx.stop.getStopIndex();
        Interval interval = Interval.of(startIndex, stopIndex);
        String text = ctx.start.getInputStream().getText(interval);
        return text;
    }

    @Override
    public void enterColumnDefinition(CqlParser.ColumnDefinitionContext ctx) {
    }

    @Override
    public void exitColumnDefinition(CqlParser.ColumnDefinitionContext ctx) {
        model.saveColumnDefinition(ctx.dataType().getText(),ctx.column().getText());
    }

    @Override
    public String toString() {
        return model.toString();
    }


    public CqlModel getModel() {
        return model;
    }
}
