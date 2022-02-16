package io.nosqlbench.virtdata.core.templates;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StringCompositorTest {

    @Test
    public void testShouldMatchSpanOnly() {
        ParsedTemplate pt = new ParsedTemplate("A\\{ {one}two", Map.of());
        assertThat(pt.getSpans()).containsExactly("A\\{ ", "one", "two");
    }

    @Test
    public void testShouldNotMatchEscaped() {
        ParsedTemplate pt = new ParsedTemplate("A\\{{B}C",Map.of());
        assertThat(pt.getSpans()).containsExactly("A\\{","B","C");
    }

//    @Test
//    public void testShoulsIgnoreExplicitExcapes() {
//        StringCompositor c = new StringCompositor("A");
//        String[] spans = c.parseTemplate("A\\{B}C");
//        assertThat(spans).containsExactly("A\\{B}C");
//    }
}
