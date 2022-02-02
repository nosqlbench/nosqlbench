package io.nosqlbench.generators.cql.lang;

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
