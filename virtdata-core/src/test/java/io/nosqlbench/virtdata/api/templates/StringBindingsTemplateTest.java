package io.nosqlbench.virtdata.api.templates;

import io.nosqlbench.virtdata.api.BindingsTemplate;
import org.junit.Test;

public class StringBindingsTemplateTest {

    // , expectedExceptionsMessageRegExp = ".*not provided in the bindings: \\[two, three\\]")
    @Test(expected = RuntimeException.class)
    public void testUnqualifiedBindings() {
        BindingsTemplate bt1 = new BindingsTemplate();
        bt1.addFieldBinding("one", "Identity()");
        String template="{one} {two} {three}\n";
        StringBindingsTemplate sbt = new StringBindingsTemplate(template,bt1);
        StringBindings resolved = sbt.resolve();
    }

}