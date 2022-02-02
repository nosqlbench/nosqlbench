package com.datastax.ebdrivers.cql.statements;

import io.nosqlbench.activitytype.cql.statements.core.CQLStatementDefParser;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CQLCQLStatementDefParserTest {

    // TODO: Implment support for default values in yaml

    @Test
    public void testBasicParsing() {
        HashMap<String, String> bindings = new HashMap<String, String>() {{
            put("not", "even");
        }};
        CQLStatementDefParser sdp = new CQLStatementDefParser("test-name","This is ?not an error.");
        CQLStatementDefParser.ParseResult r = sdp.getParseResult(bindings.keySet());
        assertThat(r.hasError()).isFalse();
        assertThat(r.getStatement()).isEqualTo("This is ? an error.");
        assertThat(r.getMissingAnchors().size()).isEqualTo(0);
        assertThat(r.getMissingGenerators().size()).isEqualTo(0);
    }

    @Test
    public void testParsingDiagnostics() {

        HashMap<String, String> bindings = new HashMap<String, String>() {{
            put("BINDABLE", "two");
            put("EXTRABINDING", "5");
        }};
        CQLStatementDefParser sdp = new CQLStatementDefParser("test-name","This is a test of ?BINDABLE interpolation and ?MISSINGBINDING.");
        List<String> bindableNames = sdp.getBindableNames();
        CQLStatementDefParser.ParseResult result = sdp.getParseResult(bindings.keySet());
        assertThat(result.hasError()).isTrue();
        assertThat(result.getStatement()).isEqualTo("This is a test of ? interpolation and ?.");
        assertThat(result.getMissingAnchors().size()).isEqualTo(1);
        assertThat(result.getMissingGenerators().size()).isEqualTo(1);
        assertThat(result.getMissingAnchors()).contains("EXTRABINDING");
        assertThat(result.getMissingGenerators()).contains("MISSINGBINDING");

    }

    @Test
    public void testParsingPatterns() {
        HashMap<String, String> bindings = new HashMap<String, String>() {{
            put("B-1", "one");
            put("B_-1.2", "two");
        }};
        CQLStatementDefParser sdp = new CQLStatementDefParser("test-name","This is a test of ?B-1 and {B_-1.2}");
        List<String> bindableNames = sdp.getBindableNames();
        assertThat(bindableNames).containsExactly("B-1","B_-1.2");
        CQLStatementDefParser.ParseResult parseResult = sdp.getParseResult(bindings.keySet());
        assertThat(parseResult.hasError()).isFalse();
        assertThat(parseResult.getStatement()).isEqualTo("This is a test of ? and ?");
    }

}
