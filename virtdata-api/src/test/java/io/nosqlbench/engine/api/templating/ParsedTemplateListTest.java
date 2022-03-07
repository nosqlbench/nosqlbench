package io.nosqlbench.engine.api.templating;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedTemplateListTest {

    @Test
    public void testTemplateListLiterals() {
        ParsedTemplateList ptl = new ParsedTemplateList(List.of("a","b"), Map.of(),List.of());
        List<?> made = ptl.apply(2L);
        assertThat(made).isEqualTo(List.of("a","b"));
    }

//    @Test
//    public void testTemplateListReferences() {
//        ParsedTemplateList ptl = new ParsedTemplateList(List.of("{a}","{b}"), Map.of("a","TestingRepeater(2)","b","TestingRepeater(4)"),List.of());
//        List<?> made = ptl.apply(2L);
//        assertThat(made).isEqualTo(List.of("a","b"));
//    }

}
