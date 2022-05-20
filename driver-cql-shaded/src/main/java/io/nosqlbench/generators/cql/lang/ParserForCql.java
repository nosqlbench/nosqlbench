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


import io.nosqlbench.generators.cql.generated.CqlLexer;
import io.nosqlbench.generators.cql.generated.CqlParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;

public class ParserForCql {
    public static void parse(String input) {
        CharStream instream = CharStreams.fromString(input);

        Lexer lexer = new CqlLexer(instream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);

        CqlParser cqlParser = new CqlParser(commonTokenStream);
        cqlParser.addParseListener(new CQLAstBuilder());
        CqlParser.RootContext root = cqlParser.root();
    }

//    public static String fingerprint(String input) {
//
//    }


}
