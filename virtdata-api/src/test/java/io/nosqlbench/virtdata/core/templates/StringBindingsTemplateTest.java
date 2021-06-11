package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class StringBindingsTemplateTest {

    // , expectedExceptionsMessageRegExp = ".*not provided in the bindings: \\[two, three\\]")
    @Test
    public void testUnqualifiedBindings() {
        BindingsTemplate bt1 = new BindingsTemplate();
        bt1.addFieldBinding("one", "Identity()");
        String template="{one} {two} {three}\n";
        StringBindingsTemplate sbt = new StringBindingsTemplate(template,bt1);
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(sbt::resolve);
    }
}
