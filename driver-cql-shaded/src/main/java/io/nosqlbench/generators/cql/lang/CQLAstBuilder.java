package io.nosqlbench.generators.cql.lang;

import io.nosqlbench.generators.cql.generated.CqlParser;
import io.nosqlbench.generators.cql.generated.CqlParserBaseListener;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class CQLAstBuilder extends CqlParserBaseListener {

    @Override
    public void exitCreateTable(CqlParser.CreateTableContext ctx) {
        List<CqlParser.ColumnDefinitionContext> columnDefinitionContexts =
            ctx.columnDefinitionList().columnDefinition();
        for (CqlParser.ColumnDefinitionContext coldef : columnDefinitionContexts) {
            CqlParser.ColumnContext column = coldef.column();
            Token symbol = column.OBJECT_NAME().getSymbol();
            CqlParser.DataTypeContext datatype = coldef.dataType();
            CqlParser.DataTypeNameContext dtn = datatype.dataTypeName();
            CqlParser.DataTypeDefinitionContext dtd = datatype.dataTypeDefinition();
            if (dtd != null) {
                List<CqlParser.DataTypeNameContext> dataTypeNameContexts = dtd.dataTypeName();
                for (CqlParser.DataTypeNameContext dtnc : dataTypeNameContexts) {
                    System.out.println("here");
                }
            }
        }
    }
}
