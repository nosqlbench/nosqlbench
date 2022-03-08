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

package io.nosqlbench.virtdata.lang.parser;

import io.nosqlbench.virtdata.lang.ast.*;
import io.nosqlbench.virtdata.lang.generated.VirtDataLexer;
import io.nosqlbench.virtdata.lang.generated.VirtDataParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VirtdataBuilderTest {

    private static char[] readFile(String filename) {
        BufferedReader sr = new BufferedReader(
                new InputStreamReader(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)
                )
        );
        CharBuffer cb = CharBuffer.allocate(1000000);
        try {
            while (sr.read(cb) > 0) {
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cb.flip();
        char[] cbimage = new char[cb.limit()];
        cb.get(cbimage, 0, cb.limit());
        return cbimage;
    }

    @Test
    public void testFullSyntax() {
        char[] chars = readFile("test-syntax.virtdata");
        CodePointCharStream ais = CharStreams.fromString(new String(chars));
        String inputString = new String(chars);
        System.out.println("Parsing:\n" + inputString);
        VirtDataLexer lexer = new VirtDataLexer(ais);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VirtDataParser parser = new VirtDataParser(tokens);
        VirtDataBuilder astListener = new VirtDataBuilder();
        parser.addParseListener(astListener);

        VirtDataParser.VirtdataRecipeContext virtdataRecipeContext = parser.virtdataRecipe();
        System.out.println(virtdataRecipeContext.toStringTree(parser));

        if (astListener.hasErrors()) {
            System.out.println(astListener.getErrorNodes());
        }

        VirtDataAST ast = astListener.getModel();
        assertThat(ast.getFlows().size()).isEqualTo(3);

        List<VirtDataFlow> flows = ast.getFlows();
        VirtDataFlow flow0 = flows.get(0);

        assertThat(flow0.getExpressions().size()).isEqualTo(2);
        Expression expr00 = flow0.getExpressions().get(0);
        assertThat(expr00.getAssignment().getVariableName()).isEqualTo("joy");
        assertThat(expr00.getCall().getFunctionName()).isEqualTo("full");
        assertThat(expr00.getCall().getInputType()).isNull();
        assertThat(expr00.getCall().getOutputType()).isNull();

        Expression expr01 = flow0.getExpressions().get(1);
        assertThat(expr01.getAssignment().getVariableName()).isEqualTo("zero");
        assertThat(expr01.getCall().getFunctionName()).isEqualTo("one");
        assertThat(expr01.getCall().getArgs().size()).isEqualTo(4);

        ArgType arg0 = expr01.getCall().getArgs().get(0);
        assertThat(arg0).isInstanceOf(FunctionCall.class);
        FunctionCall fcArg = (FunctionCall) arg0;
        assertThat(fcArg.getArgs().size()).isEqualTo(1);

        ArgType arg1 = fcArg.getArgs().get(0);
        assertThat(arg1).isInstanceOf(RefArg.class);
        RefArg refArg = (RefArg) arg1;
        assertThat(refArg.getRefName()).isEqualTo("joy");

        VirtDataFlow flow1 = flows.get(1);
        assertThat(flow1.getExpressions().size()).isEqualTo(1);
        Expression expr10 = flow1.getExpressions().get(0);

        VirtDataFlow flow2 = flows.get(2);
        assertThat(flow2.getExpressions().size()).isEqualTo(1);
        Expression expr20 = flow2.getExpressions().get(0);

    }

    @Test
    public void testEscapedDoubleQuotedLiteralISEscaped() {
        VirtDataDSL.ParseResult r = VirtDataDSL.parse(
                "Template(\"A \\value\")"
        );
        assertThat(r.throwable).isNull();
        assertThat(r.flow).isNotNull();
        assertThat(r.flow.getExpressions()).hasSize(1);
        assertThat(r.flow.getExpressions().get(0)).isNotNull();
        Expression expr = r.flow.getExpressions().get(0);
        assertThat(expr.getCall().getArgs()).hasSize(1);
        ArgType argType = expr.getCall().getArgs().get(0);
        ArgType.TypeName typeName = ArgType.TypeName.valueOf(argType);
        assertThat(typeName).isEqualTo(ArgType.TypeName.StringArg);
        assertThat(argType.toString()).isEqualTo("'A value'");

    }

    @Test
    public void testEscapedSingleQuotedLiteralIsNotEscaped() {
        VirtDataDSL.ParseResult r = VirtDataDSL.parse(
                "Template('{\"q\":\"*:*\", \"fq\":\"point:\\\"IsWithin(BUFFER(POINT(40.71 74.3), 50.0))}');"
        );
        assertThat(r.throwable).isNull();
        assertThat(r.flow).isNotNull();
        assertThat(r.flow.getExpressions()).hasSize(1);
        assertThat(r.flow.getExpressions().get(0)).isNotNull();
        Expression expr = r.flow.getExpressions().get(0);
        assertThat(expr.getCall().getArgs()).hasSize(1);
        ArgType argType = expr.getCall().getArgs().get(0);
        ArgType.TypeName typeName = ArgType.TypeName.valueOf(argType);
        assertThat(typeName).isEqualTo(ArgType.TypeName.StringArg);
        assertThat(argType.toString()).isEqualTo("'{\"q\":\"*:*\", \"fq\":\"point:\\\"IsWithin(BUFFER(POINT(40.71 74.3), 50.0))}'");



    }

    @Test
    public void testLambdaChains() {
        Path path=null;
        try {

            URI uri = ClassLoader.getSystemResource("test-syntax-lambda.virtdata").toURI();
            path = Paths.get(uri);
            byte[] bytes = Files.readAllBytes(path);
            VirtDataDSL.ParseResult parseResult = VirtDataDSL.parse(new String(bytes));

            assertThat(parseResult.flow).isNotNull();

            List<Expression> expressions = parseResult.flow.getExpressions();
            assertThat(expressions).hasSize(4);

            Expression e0 = expressions.get(0);
            assertThat(e0.getCall().getFunctionName()).isEqualTo("Func2");

            Expression e1 = expressions.get(1);
            assertThat(e1.getCall().getFunctionName()).isEqualTo("Func3");

            Expression e2 = expressions.get(2);
            assertThat(e2.getCall().getFunctionName()).isEqualTo("func4");

            Expression e3 = expressions.get(3);
            assertThat(e3.getCall().getFunctionName()).isEqualTo("f5");


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
