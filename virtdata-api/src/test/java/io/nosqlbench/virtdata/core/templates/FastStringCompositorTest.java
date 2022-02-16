package io.nosqlbench.virtdata.core.templates;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class FastStringCompositorTest {

    @Test
    @Disabled // Needs to have annotation processor run in test scope first
    public void testFastStringCompositor() {
        String rawTpl = "template {b1}, {{TestValue(5)}}";
        Map<String, String> bindings = Map.of("b1", "TestIdentity()");
        ParsedTemplate ptpl = new ParsedTemplate(rawTpl, bindings);
        StringCompositor fsc = new StringCompositor(ptpl,Map.of());
        System.out.println(fsc);
    }

}
