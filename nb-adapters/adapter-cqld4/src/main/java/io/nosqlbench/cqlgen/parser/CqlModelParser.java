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

package io.nosqlbench.cqlgen.parser;

import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import io.nosqlbench.cqlgen.generated.CqlLexer;
import io.nosqlbench.cqlgen.generated.CqlParser;
import io.nosqlbench.cqlgen.model.CGErrorListener;
import io.nosqlbench.cqlgen.model.CqlModel;
import io.nosqlbench.cqlgen.model.CqlModelBuilder;
import io.nosqlbench.cqlgen.model.CqlType;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class CqlModelParser {
    private final static Logger logger  = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/parser");


    public static CqlModel parse(Path path) {
        try {
            String ddl = Files.readString(path);
            logger.info(() -> "read " + ddl.length() + " character DDL file, parsing");
            CqlModel parsed = parse(ddl, null);
            logger.info(() -> "parsed cql model: " + parsed.getSummaryLine());
            return parsed;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CqlType parseCqlType(String input) {
        CqlModel parsed = parse(input, null);
        List<CqlType> types = parsed.getTypeDefs();
        if (types.size()!=1) {
            throw new RuntimeException("error parsing typedef");
        }
        return types.get(0);
    }

    public static CqlModel parse(String input, Path origin) {
        try {
            CodePointCharStream cstream = CharStreams.fromString(input);
            CGErrorListener errorListener = new CGErrorListener(origin);

            CqlLexer lexer = new CqlLexer(cstream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CqlParser parser = new CqlParser(tokens);

            CqlModelBuilder cqlModelBuilder = new CqlModelBuilder(errorListener);
            parser.addParseListener(cqlModelBuilder);
            parser.addErrorListener(errorListener);

            parser.root();

            CqlModel model = cqlModelBuilder.getModel();
            if (model.getErrors().size()>0) {
                System.out.println(model.getErrors());
                throw new RuntimeException("Unable to render model for unparsable input with errors:\n" + model.getErrors());
            } else {
                return model;
            }

        } catch (Exception e) {
            logger.warn(() -> "Error while parsing flow:" + e.getMessage());
            throw e;
//            return new ParseResult(e);
        }
    }

}
