package io.nosqlbench.virtdata.lang.parser;

import io.nosqlbench.virtdata.lang.ast.VirtDataAST;
import io.nosqlbench.virtdata.lang.ast.VirtDataFlow;
import io.nosqlbench.virtdata.lang.generated.VirtDataLexer;
import io.nosqlbench.virtdata.lang.generated.VirtDataParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;


public class VirtDataDSL {
    private final static Logger logger  = LogManager.getLogger(VirtDataDSL.class);

    public static VirtDataDSL.ParseResult parse(String input) {

        try {
            CodePointCharStream cstream = CharStreams.fromString(input);
            VirtDataLexer lexer = new VirtDataLexer(cstream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            VirtDataParser parser = new VirtDataParser(tokens);
            VirtDataBuilder astListener = new VirtDataBuilder();
            parser.addParseListener(astListener);

            VirtDataParser.VirtdataFlowContext virtdataFlowContext = parser.virtdataFlow();
            logger.trace("parse tree: " + virtdataFlowContext.toStringTree(parser));

            if (astListener.hasErrors()) {
                System.out.println(astListener.getErrorNodes());
            }

            VirtDataAST ast = astListener.getModel();
            List<VirtDataFlow> flows = ast.getFlows();
            if (flows.size() > 1) {
                throw new RuntimeException("Only one flow expected here.");
            }

            if (astListener.hasErrors()) {
                throw new RuntimeException("Error parsing input '" + input + "'");
            }

            return new ParseResult(flows.get(0));

        } catch (Exception e) {
            logger.warn("Error while parsing flow:" + e.getMessage());
            return new ParseResult(e);
        }
    }

    public static class ParseResult {
        public Throwable throwable;
        public VirtDataFlow flow;
        public ParseResult(VirtDataFlow flow) {
            this.flow = flow;
        }
        public ParseResult(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}
