package io.nosqlbench.virtdata.core;

import io.nosqlbench.virtdata.core.bindings.ResolverDiagnostics;
import io.nosqlbench.virtdata.core.bindings.VirtDataComposer;
import org.junit.Test;

public class VirtDataComposerTest {

    @Test
    public void testResolveFunctionFlow() {
        VirtDataComposer composer = new VirtDataComposer();
        ResolverDiagnostics dashrepeats = composer.resolveDiagnosticFunctionFlow("TestableMapper('--', TestingRepeater(2));");
    }

    @Test
    public void testResolveDiagnosticFunctionFlow() {
    }

    @Test
    public void testResolveFunctionFlow1() {
    }
}
