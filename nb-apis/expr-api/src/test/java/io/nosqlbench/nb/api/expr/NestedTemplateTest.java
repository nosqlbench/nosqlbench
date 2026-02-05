/*
 * Copyright (c) 2025 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.api.expr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class NestedTemplateTest {

    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

    @Test
    void testNestedTemplateFunction() {
        // TEMPLATE(outer, TEMPLATE(inner, default))
        String input = "value: TEMPLATE(outer,TEMPLATE(inner,default-value))";
        String rewritten = TemplateRewriter.rewrite(input);
        
        // Let's see what it rewrites to currently
        System.out.println("Rewritten: " + rewritten);

        // If 'outer' is provided, it should use it
        assertEquals("value: outer-provided", processor.process(rewritten, URI.create("test://1"), Map.of("outer", "outer-provided")));

        // If 'outer' is not provided, but 'inner' is, it should use 'inner'
        assertEquals("value: inner-provided", processor.process(rewritten, URI.create("test://2"), Map.of("inner", "inner-provided")));

        // If neither is provided, it should use the innermost default
        assertEquals("value: default-value", processor.process(rewritten, URI.create("test://3"), Map.of()));
    }

    @Test
    void testNestedShellVarInTemplate() {
        // TEMPLATE(outer, ${inner:default})
        String input = "value: TEMPLATE(outer,${inner:default-value})";
        String rewritten = TemplateRewriter.rewrite(input);
        System.out.println("Rewritten (mixed1): " + rewritten);

        assertEquals("value: outer-provided", processor.process(rewritten, URI.create("test://4"), Map.of("outer", "outer-provided")));
        assertEquals("value: inner-provided", processor.process(rewritten, URI.create("test://5"), Map.of("inner", "inner-provided")));
        assertEquals("value: default-value", processor.process(rewritten, URI.create("test://6"), Map.of()));
    }

    @Test
    void testNestedTemplateInShellVar() {
        // ${outer:TEMPLATE(inner,default)}
        String input = "value: ${outer:TEMPLATE(inner,default-value)}";
        String rewritten = TemplateRewriter.rewrite(input);
        System.out.println("Rewritten (mixed2): " + rewritten);

        assertEquals("value: outer-provided", processor.process(rewritten, URI.create("test://7"), Map.of("outer", "outer-provided")));
        assertEquals("value: inner-provided", processor.process(rewritten, URI.create("test://8"), Map.of("inner", "inner-provided")));
        assertEquals("value: default-value", processor.process(rewritten, URI.create("test://9"), Map.of()));
    }

    @Test
    void testFullyNestedShellVar() {
        // ${outer:${inner:default}}
        String input = "value: ${outer:${inner:default-value}}";
        String rewritten = TemplateRewriter.rewrite(input);
        System.out.println("Rewritten (nested shell): " + rewritten);

        assertEquals("value: outer-provided", processor.process(rewritten, URI.create("test://10"), Map.of("outer", "outer-provided")));
        assertEquals("value: inner-provided", processor.process(rewritten, URI.create("test://11"), Map.of("inner", "inner-provided")));
        assertEquals("value: default-value", processor.process(rewritten, URI.create("test://12"), Map.of()));
    }
}
