package io.nosqlbench.virtdata.core.templates;

import org.junit.Ignore;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedTemplateTest {

    private final Map<String, String> bindings = Map.of(
            "bindname1", "bindspec1",
            "bindname2", "bindspec2");
    private final String rawNothing = "This has no anchors";
    private final String oneCurly = "A {curly} brace.";
    private final String oneQuestion = " A ?question anchor.";
    private final String oneExtraneous = "An {this is an extraneous form} invalid anchor.";

    @Test
    public void testShouldMatchRawLiteral() {
        ParsedTemplate pt = new ParsedTemplate(rawNothing, bindings);
        assertThat(pt.getSpans()).containsExactly("This has no anchors");
        assertThat(pt.getSpecificBindings()).isEmpty();
        assertThat(pt.getExtraBindings()).hasSameElementsAs(bindings.keySet());
        assertThat(pt.getMissingBindings()).isEmpty();
    }

    @Test
    public void testShoudlMatchCurlyBraces() {
        ParsedTemplate pt = new ParsedTemplate(oneCurly, bindings);
        assertThat(pt.getSpans()).containsExactly("A ", "curly", " brace.");
        assertThat(pt.getSpecificBindings()).isEmpty();
        assertThat(pt.getMissingBindings()).contains("curly");
        assertThat(pt.getExtraBindings()).hasSameElementsAs(bindings.keySet());
    }

    @Ignore("currently not working correctly")
    @Test
    public void testShouldMatchQuestionMark() {
        ParsedTemplate pt = new ParsedTemplate(oneQuestion, bindings);
        assertThat(pt.getSpans()).containsExactly(" A ", "question", " anchor.");
        assertThat(pt.getSpecificBindings()).isEmpty();
        assertThat(pt.getMissingBindings()).containsExactly("question");
        assertThat(pt.getExtraBindings()).hasSameElementsAs(bindings.keySet());
    }

    @Test
    public void testShouldIgnoreExtraneousAnchors() {
        ParsedTemplate pt = new ParsedTemplate(oneExtraneous, bindings);
        assertThat(pt.getSpans()).containsExactly("An {this is an extraneous form} invalid anchor.");
        assertThat(pt.getSpecificBindings()).isEmpty();
        assertThat(pt.getMissingBindings()).isEmpty();
        assertThat(pt.getExtraBindings()).hasSameElementsAs(bindings.keySet());
    }

    @Test
    public void testShouldMatchLiteralVariableOnly() {
        String literalVariableOnly = "literal {bindname1}";
        ParsedTemplate pt = new ParsedTemplate(literalVariableOnly, bindings);
        assertThat(pt.getSpans()).containsExactly("literal ", "bindname1", "");
        assertThat(pt.getSpecificBindings()).containsOnlyKeys("bindname1");
        assertThat(pt.getMissingBindings()).isEmpty();
        assertThat(pt.getExtraBindings()).containsExactly("bindname2");
    }

    @Test
    public void testShouldMatchVariableLiteralOnly() {
        String variableLiteralOnly = "{bindname2} literal";
        ParsedTemplate pt = new ParsedTemplate(variableLiteralOnly, bindings);
        assertThat(pt.getSpans()).containsExactly("", "bindname2", " literal");
        assertThat(pt.getSpecificBindings()).containsOnlyKeys("bindname2");
        assertThat(pt.getMissingBindings()).isEmpty();
        assertThat(pt.getExtraBindings()).containsExactly("bindname1");
    }

    @Test
    public void testShouldMatchProvidedValidPattern() {
        String basic = "A [provided] pattern.";
        Pattern p = Pattern.compile("\\[(?<anchor>\\w[_a-zA-Z]+)]");
        ParsedTemplate pt = new ParsedTemplate(basic, bindings, p);
        assertThat(pt.getSpans()).containsExactly("A ", "provided", " pattern.");
        assertThat(pt.getSpecificBindings()).isEmpty();
        assertThat(pt.getMissingBindings()).containsExactly("provided");
        assertThat(pt.getExtraBindings()).containsAll(bindings.keySet());
    }

    //, expectedExceptionsMessageRegExp = ".*must contain a named group called anchor.*"
    @Test(expected= InvalidParameterException.class)
    public void testShouldErrorOnInvalidPattern() {
        String wontuse = "This won't get used.";
        Pattern p = Pattern.compile("\\[(\\w[_a-zA-Z]+)]");
        ParsedTemplate pt = new ParsedTemplate(wontuse, bindings, p);
    }

    @Test
    public void testPositionalExpansionShouldBeValid() {
        String multi = "A {bindname1} of {bindname2} sort.";
        ParsedTemplate pt = new ParsedTemplate(multi, bindings);
        assertThat(pt.getSpans()).containsExactly("A ", "bindname1", " of ", "bindname2", " sort.");
        assertThat(pt.getSpecificBindings()).containsOnlyKeys("bindname1", "bindname2");
        assertThat(pt.getMissingBindings()).isEmpty();
        assertThat(pt.getExtraBindings()).isEmpty();
        assertThat(pt.getPositionalStatement(s -> "##")).isEqualTo("A ## of ## sort.");
        assertThat(pt.getPositionalStatement(s -> "[[" + s + "]]")).isEqualTo("A [[bindname1]] of [[bindname2]] sort.");

        assertThat(pt.getBindPoints()).containsExactly(
                new BindPoint("bindname1", "bindspec1"),
                new BindPoint("bindname2", "bindspec2")
        );
    }
}
