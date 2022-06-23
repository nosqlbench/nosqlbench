/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import io.nosqlbench.nb.spectest.core.SpecTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class UniformWorkloadSpecificationTest {
    private final static Logger logger = LogManager.getLogger(UniformWorkloadSpecificationTest.class);

    @Test
    public void testTemplatedWorkloads() {
        SpecTest specTester = SpecTest.builder()
            .path("target/classes/workload_definition/")
            .matchNodes(
                Pattern.compile("\\*(.*?)\\*\n?"), FencedCodeBlock.class, 0, 1, 0 ,1)
            .validators(new YamlSpecValidator())
            .build();
        specTester.run();
    }

}
