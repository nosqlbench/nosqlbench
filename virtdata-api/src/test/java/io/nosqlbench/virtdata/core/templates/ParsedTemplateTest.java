package io.nosqlbench.virtdata.core.templates;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedTemplateTest {

    private final Map<String, String> bindings = Map.of(
            "bindname1", "bindspec1",
            "bindname2", "bindspec2");

    @Test
    public void testShouldMatchRawLiteral() {
        String rawNothing = "This has no anchors";
        ParsedTemplate pt = new ParsedTemplate(rawNothing, bindings);
        assertThat(pt.getSpans()).containsExactly("This has no anchors");
        assertThat(pt.getBindPoints()).isEmpty();
        assertThat(pt.getMissing()).isEmpty();
    }

    @Test
    public void testShouldIgnoreExtraneousAnchors() {
        String oneExtraneous = "An {this is an extraneous form} invalid anchor.";
        ParsedTemplate pt = new ParsedTemplate(oneExtraneous, bindings);
        assertThat(pt.getSpans()).containsExactly("An {this is an extraneous form} invalid anchor.");
        assertThat(pt.getBindPoints()).isEmpty();
        assertThat(pt.getMissing()).isEmpty();
    }

    @Test
    public void testShouldAllowArbitraryNonGreedyInExtendedBindPoint() {
        String oneExtendedBindPoint = "An {{this is an extended form}} {{and another}} invalid anchor.";
        ParsedTemplate pt = new ParsedTemplate(oneExtendedBindPoint, bindings);
        assertThat(pt.getSpans()).containsExactly("An ","this is an extended form"," ","and another"," invalid anchor.");
        assertThat(pt.getAnchors()).containsExactly("this is an extended form","and another");
    }

    @Test
    public void testShouldMatchLiteralVariableOnly() {
        String literalVariableOnly = "literal {bindname1}";
        ParsedTemplate pt = new ParsedTemplate(literalVariableOnly, bindings);
        assertThat(pt.getSpans()).containsExactly("literal ", "bindname1", "");
        assertThat(pt.getAnchors()).containsOnly("bindname1");
        assertThat(pt.getMissing()).isEmpty();
    }

    @Test
    public void testShouldMatchVariableLiteralOnly() {
        String variableLiteralOnly = "{bindname2} literal";
        ParsedTemplate pt = new ParsedTemplate(variableLiteralOnly, bindings);
        assertThat(pt.getSpans()).containsExactly("", "bindname2", " literal");
        assertThat(pt.getAnchors()).containsOnly("bindname2");
        assertThat(pt.getMissing()).isEmpty();
    }

    @Test
    public void testPositionalExpansionShouldBeValid() {
        String multi = "A {bindname1} of {bindname2} sort.";
        ParsedTemplate pt = new ParsedTemplate(multi, bindings);
        assertThat(pt.getSpans()).containsExactly("A ", "bindname1", " of ", "bindname2", " sort.");
        assertThat(pt.getAnchors()).containsOnly("bindname1", "bindname2");
        assertThat(pt.getMissing()).isEmpty();
        assertThat(pt.getPositionalStatement(s -> "##")).isEqualTo("A ## of ## sort.");
        assertThat(pt.getPositionalStatement(s -> "[[" + s + "]]")).isEqualTo("A [[bindname1]] of [[bindname2]] sort.");

        assertThat(pt.getBindPoints()).containsExactly(
                new BindPoint("bindname1", "bindspec1"),
                new BindPoint("bindname2", "bindspec2")
        );
    }

    @Test
    public void shouldMatchBasicCapturePoint() {
        ParsedTemplate pt = new ParsedTemplate(
            "select [u],[v as v1] from users where userid={userid}", Map.of("userid", "NumberNameToString()")
        );
        assertThat(pt.getAnchors()).containsExactly("userid");
        assertThat(pt.getType()).isEqualTo(ParsedTemplate.Type.concat);
        assertThat(pt.getCaptures()).containsExactly(CapturePoint.of("u"),CapturePoint.of("v","v1"));

    }

}
