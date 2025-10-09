package io.nosqlbench.nb.api.expr;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroovyExpressionProcessorTest {

    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

    @Test
    void shouldSubstituteSimpleExpression() {
        String source = "value: {{= 1 + 2 }}";
        String rendered = processor.process(source, null, Map.of());
        assertThat(rendered).isEqualTo("value: 3");
    }

    @Test
    void shouldAccessParameterFunctions() {
        String source = "param: {{= param('threshold') }}";
        String rendered = processor.process(source, URI.create("file:///test.yaml"), Map.of("threshold", 99));
        assertThat(rendered).isEqualTo("param: 99");
    }

    @Test
    void shouldSupportDefaultParameterFunction() {
        String source = "value: {{= paramOr('missing', 'fallback') }}";
        String rendered = processor.process(source, null, Map.of());
        assertThat(rendered).isEqualTo("value: fallback");
    }

    @Test
    void shouldMemoizeAndDerefVariable() {
        String source = String.join("\n",
            "first: {{memo = 1 + 2}}",
            "second: {{@memo}}"
        );
        String rendered = processor.process(source, null, Map.of());
        assertThat(rendered).isEqualTo("first: 3\nsecond: 3");
    }

    @Test
    void shouldEnforceStrictSetOnce() {
        String source = String.join("\n",
            "value: {{token === 'abc'}}",
            "again: {{token === 'def'}}"
        );
        assertThatThrownBy(() -> processor.process(source, null, Map.of()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("token");
    }

    @Test
    void shouldIgnoreIfAlreadySetForSilentAssignment() {
        String source = String.join("\n",
            "value: {{token == 'abc'}}",
            "repeat: {{token == 'def'}}",
            "check: {{@token!}}"
        );
        String rendered = processor.process(source, null, Map.of());
        assertThat(rendered).isEqualTo("value: abc\nrepeat: abc\ncheck: abc");
    }

    @Test
    void shouldAllowOptionalDereference() {
        String source = "value: {{@missing?}}";
        String rendered = processor.process(source, null, Map.of());
        assertThat(rendered).isEqualTo("value: ");
    }

    @Test
    void shouldRejectMissingDereferenceByDefault() {
        String source = "value: {{@missing}}";
        assertThatThrownBy(() -> processor.process(source, null, Map.of()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("missing");
    }

    @Test
    void shouldRejectNullDereferenceWhenRequired() {
        String source = String.join("\n",
            "init: {{nullable = null}}",
            "fail: {{@nullable!}}"
        );
        assertThatThrownBy(() -> processor.process(source, null, Map.of()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("nullable");
    }
}
