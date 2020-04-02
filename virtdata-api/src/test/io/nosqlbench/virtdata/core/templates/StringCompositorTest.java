package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.core.templates.StringCompositor;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringCompositorTest {

    @Test
    public void testShouldMatchSpanOnly() {
        StringCompositor c = new StringCompositor("A");
        String[] spans = c.parseTemplate("A\\{ {one}two");
        assertThat(spans).containsExactly("A\\{ ", "one", "two");

    }

    @Test
    public void testShouldNotMatchEscaped() {
        StringCompositor c = new StringCompositor("A");
        String[] spans = c.parseTemplate("A\\{{B}C");
        assertThat(spans).containsExactly("A\\{","B","C");
    }

//    @Test
//    public void testShoulsIgnoreExplicitExcapes() {
//        StringCompositor c = new StringCompositor("A");
//        String[] spans = c.parseTemplate("A\\{B}C");
//        assertThat(spans).containsExactly("A\\{B}C");
//    }

}
