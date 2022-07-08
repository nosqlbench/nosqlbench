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

package io.nosqlbench.converters.cql.cql.parser;

import io.nosqlbench.converters.cql.cql.cqlast.CqlModel;
import io.nosqlbench.converters.cql.cql.cqlast.CqlModelBuilder;
import io.nosqlbench.converters.cql.generated.CqlLexer;
import io.nosqlbench.converters.cql.generated.CqlParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class CqlModelParser {
    private final static Logger logger  = LogManager.getLogger(CqlModelParser.class);

    public static CqlModel parse(Path path) {
        try {
            String ddl = Files.readString(path);
            return parse(ddl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CqlModel parse(String input) {

        try {
            CodePointCharStream cstream = CharStreams.fromString(input);

            CqlLexer lexer = new CqlLexer(cstream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CqlParser parser = new CqlParser(tokens);


            CqlModelBuilder astListener = new CqlModelBuilder();
            parser.addParseListener(astListener);

            CqlParser.RootContext keyspaceParser = parser.root();

            return astListener.getModel();

        } catch (Exception e) {
            logger.warn("Error while parsing flow:" + e.getMessage());
            throw e;
//            return new ParseResult(e);
        }
    }

}
