package io.nosqlbench.nb.api.expr;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.api.config.params.Element;
import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec;
import io.nosqlbench.nb.api.expr.ExprFunctionParamsAware;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroovyExpressionProcessorTest {

    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

    @Test
    void shouldExecuteArbitraryGroovyScript() {
        String template = "value={{=\n" +
            "    def data = []\n" +
            "    for (int i = 1; i <= 3; i++) {\n" +
            "        data << i * i\n" +
            "    }\n" +
            "    return data.join('-')\n" +
            "}}";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("value=1-4-9", rendered);
    }

    @Test
    void shouldHandleAssignmentOperatorsAndLvarReferences() {
        String template = """
{{alpha = 'one'}}
alpha={{@alpha}}
{{alpha = 'two'}}
alphaAgain={{@alpha}}
{{beta == 'first'}}
betaOnce={{@beta}}
{{beta == 'ignored'}}
betaStill={{@beta}}
{{gamma === 'strict'}}
gammaStrict={{@gamma}}
gammaStrictBang={{@gamma!}}
optional={{@missing?}}
end
""";

        String rendered = processor.process(template, URI.create("nb://test"), Map.of());

        String expected = """
one
alpha=one
two
alphaAgain=two
first
betaOnce=first
first
betaStill=first
strict
gammaStrict=strict
gammaStrictBang=strict
optional=
end
""";

        assertEquals(expected, rendered);
    }

    @Test
    void strictAssignmentShouldRejectSecondDefinition() {
        String template = """
{{gamma === 'strict'}}
{{gamma === 'again'}}
""";

        RuntimeException ex = assertThrows(RuntimeException.class, () -> processor.process(template, null, Map.of()));

        assertTrue(ex.getMessage().contains("already set"));
    }

    @Test
    void bangModifierShouldRejectNullValues() {
        String template = """
{{nullable = null}}
{{@nullable!}}
""";

        RuntimeException ex = assertThrows(RuntimeException.class, () -> processor.process(template, null, Map.of()));

        assertTrue(ex.getMessage().contains("was null"));
    }

    @Test
    void shouldProvideParamsToAwareProviders() {
        RecordingProvider provider = new RecordingProvider();
        GroovyExpressionProcessor customProcessor = new GroovyExpressionProcessor(
            List.of(provider),
            new CompilerConfiguration()
        );

        String rendered = customProcessor.process("value={{= readFoo()}}", null, Map.of("foo", "bar"));

        assertEquals("value=bar", rendered);
        assertNotNull(provider.captured);
        assertEquals("bar", provider.captured.getOr("foo", ""));
    }

    private static class RecordingProvider implements ExprFunctionProvider, ExprFunctionParamsAware {

        Element captured;

        @Override
        public void setParams(Element params) {
            this.captured = params;
        }

        @ExprFunctionSpec(
            name = "readFoo",
            synopsis = "readFoo()",
            description = "Return the 'foo' parameter"
        )
        private Object readFoo() {
            return captured.getOr("foo", "");
        }
    }
}
