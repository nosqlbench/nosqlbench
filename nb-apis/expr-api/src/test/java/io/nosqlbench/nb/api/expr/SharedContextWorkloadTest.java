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


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests verifying that all Groovy expressions in a workload file share the same variable context.
 * This ensures that variables, functions, and state set in one expression are accessible in
 * subsequent expressions within the same workload processing session.
 */
@Tag("unit")
class SharedContextWorkloadTest {

    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

    @Test
    void shouldShareBasicVariableContextAcrossExpressions() throws IOException {
        String workload = loadWorkloadResource("workloads/shared_context_basic.yaml");
        String processed = processor.process(workload, URI.create("test://basic"), Map.of());

        assertNotNull(processed);

        // Verify counter starts at 0 and is incremented across expressions
        assertTrue(processed.contains("setup: Initialized"), "Setup should succeed");
        assertTrue(processed.contains("step1: 1"), "First increment should result in 1");
        assertTrue(processed.contains("step2: 11"), "Second increment should result in 11 (1+10)");
        assertTrue(processed.contains("step3: 22"), "Third increment should result in 22 (11*2)");
        assertTrue(processed.contains("final: Counter reached: 22"), "Final expression should access counter value");
    }

    @Test
    void shouldShareCollectionContextAcrossExpressions() throws IOException {
        String workload = loadWorkloadResource("workloads/shared_context_collections.yaml");
        String processed = processor.process(workload, URI.create("test://collections"), Map.of());

        assertNotNull(processed);

        // Verify list operations share context
        assertTrue(processed.contains("init: List initialized"), "List init should succeed");
        assertTrue(processed.contains("count: 3"), "List should contain 3 items");
        assertTrue(processed.contains("list: apple, banana, cherry"), "List should contain all added items");

        // Verify map operations share context
        assertTrue(processed.contains("setup_map: Map initialized"), "Map init should succeed");
        assertTrue(processed.contains("total: 274"), "Map total should be 95+87+92=274");
        assertTrue(processed.contains("average: 91"), "Average should be 274/3=91.33... (rounded to 91)");
    }

    @Test
    void shouldShareFunctionDefinitionsAcrossExpressions() throws IOException {
        String workload = loadWorkloadResource("workloads/shared_context_functions.yaml");
        String processed = processor.process(workload, URI.create("test://functions"), Map.of());

        assertNotNull(processed);

        // Verify function definitions are shared
        assertTrue(processed.contains("define: Function defined"), "Function definition should succeed");
        assertTrue(processed.contains("fib5: 5"), "Fibonacci(5) should equal 5");
        assertTrue(processed.contains("fib10: 55"), "Fibonacci(10) should equal 55");

        // Verify setup and function definition
        assertTrue(processed.contains("setup: Multiplier set"), "Multiplier setup should succeed");
        assertTrue(processed.contains("define2: Scale function defined"), "Scale function definition should succeed");

        // Verify functions can access shared variables
        assertTrue(processed.contains("scaled: 21"), "scale(7) with multiplier=3 should equal 21");
        assertTrue(processed.contains("change: Multiplier changed"), "Multiplier change should succeed");
        assertTrue(processed.contains("rescaled: 35"), "scale(7) with multiplier=5 should equal 35");
    }

    @Test
    void shouldShareComplexStateAcrossExpressions() throws IOException {
        String workload = loadWorkloadResource("workloads/shared_context_complex.yaml");
        String processed = processor.process(workload, URI.create("test://complex"), Map.of());

        assertNotNull(processed);

        // Verify initialization
        assertTrue(processed.contains("init_config: Config initialized"), "Config init should succeed");
        assertTrue(processed.contains("init_stats: Stats initialized"), "Stats init should succeed");
        assertTrue(processed.contains("init_data: Cache initialized"), "Cache init should succeed");

        // Verify mode is accessible
        assertTrue(processed.contains("check_mode: Running in test mode"), "Config should be shared");

        // Verify processing results
        assertTrue(processed.contains("process1: Processed request 1"), "First process should succeed");
        assertTrue(processed.contains("process2: Processed request 2"), "Second process should succeed");
        assertTrue(processed.contains("process3: Failed request 3"), "Third process should fail");

        // Verify stats aggregation
        assertTrue(processed.contains("summary: Processed 3 requests, 66"), "Summary should show 66% success rate");
        assertTrue(processed.contains("1 errors"), "Summary should show 1 error");

        // Verify cache operations
        assertTrue(processed.contains("cache_size: Cache contains 2 entries"), "Cache should have 2 entries");
        assertTrue(processed.contains("req1") && processed.contains("req2"), "Cache should contain both keys");
    }

    @Test
    void shouldShareContextInLoopLikeOperations() throws IOException {
        String workload = loadWorkloadResource("workloads/shared_context_loops.yaml");
        String processed = processor.process(workload, URI.create("test://loops"), Map.of());

        assertNotNull(processed);

        // Verify initialization
        assertTrue(processed.contains("init: Initialized"), "Init should succeed");

        // Verify accumulator is shared across iterations
        assertTrue(processed.contains("total: 15"), "Sum should be 1+2+3+4+5=15");
        assertTrue(processed.contains("count: 5"), "Count should be 5");
        assertTrue(processed.contains("list: 1-2-3-4-5"), "List should contain all numbers");
        assertTrue(processed.contains("average: 3"), "Average should be 15/5=3");
    }

    @Test
    void shouldMaintainVariableStateWithSetOnceOperator() throws IOException {
        String template = """
            first: {{alpha == 'initial'}}
            read1: {{@alpha}}
            second: {{alpha == 'ignored'}}
            read2: {{@alpha}}
            """;

        String processed = processor.process(template, URI.create("test://setonce"), Map.of());

        // Verify set-once operator maintains original value
        assertTrue(processed.contains("first: initial"), "First assignment should set value");
        assertTrue(processed.contains("read1: initial"), "First read should get initial value");
        assertTrue(processed.contains("second: initial"), "Second assignment should be ignored");
        assertTrue(processed.contains("read2: initial"), "Second read should still get initial value");
    }

    @Test
    void shouldMaintainVariableStateWithOverwriteOperator() throws IOException {
        String template = """
            first: {{beta = 'version1'}}
            read1: {{@beta}}
            second: {{beta = 'version2'}}
            read2: {{@beta}}
            """;

        String processed = processor.process(template, URI.create("test://overwrite"), Map.of());

        // Verify overwrite operator updates value
        assertTrue(processed.contains("first: version1"), "First assignment should set value");
        assertTrue(processed.contains("read1: version1"), "First read should get version1");
        assertTrue(processed.contains("second: version2"), "Second assignment should overwrite");
        assertTrue(processed.contains("read2: version2"), "Second read should get version2");
    }

    @Test
    void shouldShareVariablesAcrossMultilineExpressionBlocks() throws IOException {
        String template = """
            setup: {{=
                state = [:]
                state.counter = 0
                state.items = []
                return "Initialized"
            }}

            update1: {{=
                state.counter++
                state.items << 'A'
                return "Updated"
            }}

            update2: {{=
                state.counter++
                state.items << 'B'
                return "Updated"
            }}

            result: {{= "Counter: ${state.counter}, Items: ${state.items.join(',')}"}}
            """;

        String processed = processor.process(template, URI.create("test://multiline"), Map.of());

        // Verify state is shared across multiline blocks
        assertTrue(processed.contains("setup: Initialized"), "Setup should succeed");
        assertTrue(processed.contains("update1: Updated"), "First update should succeed");
        assertTrue(processed.contains("update2: Updated"), "Second update should succeed");
        assertTrue(processed.contains("result: Counter: 2, Items: A,B"), "Result should show accumulated state");
    }

    @Test
    void shouldAllowAccessToVariablesSetInGroovyBlocks() throws IOException {
        String template = """
            define: {{=
                myVar = 'fromBlock'
                myList = [1, 2, 3]
                return "Defined"
            }}
            access1: {{= myVar}}
            access2: {{= myList.sum()}}
            modify: {{=
                myVar = 'modified'
                return myVar
            }}
            verify: {{= myVar}}
            """;

        String processed = processor.process(template, URI.create("test://access"), Map.of());

        // Verify variables set in Groovy blocks are accessible in other Groovy expressions
        assertTrue(processed.contains("define: Defined"), "Definition should succeed");
        assertTrue(processed.contains("access1: fromBlock"), "Should access variable from block");
        assertTrue(processed.contains("access2: 6"), "Should access list from block");
        assertTrue(processed.contains("modify: modified"), "Should be able to modify variable");
        assertTrue(processed.contains("verify: modified"), "Should read modified value");
    }

    @Test
    void shouldMaintainSeparateContextsForDifferentProcessCalls() {
        String template = """
            set: {{myVar = 'first'}}
            read: {{@myVar}}
            """;

        // Process the same template twice with different contexts
        String processed1 = processor.process(template, URI.create("test://context1"), Map.of());
        String processed2 = processor.process(template, URI.create("test://context2"), Map.of());

        // Both should succeed independently
        assertTrue(processed1.contains("set: first"));
        assertTrue(processed1.contains("read: first"));
        assertTrue(processed2.contains("set: first"));
        assertTrue(processed2.contains("read: first"));

        // Verify they don't interfere with each other
        assertFalse(processed1.isEmpty());
        assertFalse(processed2.isEmpty());
    }

    @Test
    void shouldShareBindingBetweenExpressionAndLvarReference() throws IOException {
        String template = """
            set: {{myValue = 42}}
            direct: {{= __expr_lvar_myValue}}
            reference: {{@myValue}}
            increment: {{myValue = __expr_lvar_myValue + 10}}
            verify_lvar: {{@myValue}}
            """;

        String processed = processor.process(template, URI.create("test://binding"), Map.of());

        // Verify lvar assignment stores values accessible via @ reference and via __expr_lvar_ prefix
        assertTrue(processed.contains("set: 42"), "Assignment should work");
        assertTrue(processed.contains("direct: 42"), "Direct access via binding prefix should work");
        assertTrue(processed.contains("reference: 42"), "Reference should work");
        assertTrue(processed.contains("increment: 52"), "Increment should work");
        assertTrue(processed.contains("verify_lvar: 52"), "Verify should see updated value");
    }

    private String loadWorkloadResource(String resourcePath) throws IOException {
        Path path = Path.of("src/test/resources", resourcePath);
        assertTrue(Files.exists(path), "Resource file should exist: " + path);
        return Files.readString(path);
    }
}
