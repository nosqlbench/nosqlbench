/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapters.api.activityconfig.specifications;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import io.nosqlbench.nb.spectest.core.SpecTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static io.nosqlbench.nb.spectest.traversal.STPredicateVerbs.*;

public class UniformWorkloadSpecificationTest {

    private final static Logger logger = LogManager.getLogger(UniformWorkloadSpecificationTest.class);

    private final static Object[] mdPredicate = new Object[] {
        depth(deepany("yaml:"), ".*"), depth(FencedCodeBlock.class, ".*"),
        depth(deepany("json:"), ".*"), ref(1),
        depth(deepany("ops:"), ".*"), ref(1)

    };
    @Test
    public void testTemplatedWorkloads() {
        SpecTest specTester = SpecTest.builder()
            .path("target/classes/workload_definition/")
            .matchNodes(mdPredicate)
            .validators(new YamlSpecValidator())
            .build();
        specTester.run();
    }

//    @Test
//    public void testWithStructuredPredicate() {
//        SpecTest specTester = SpecTest.builder()
//            .path("target/classes/workload_definition/op_template_payloads.md")
//            .matchNodes(mdPredicate)
//            .validators(new YamlSpecValidator())
////            .debug()
//            .build();
//        specTester.run();
//    }
//


}
