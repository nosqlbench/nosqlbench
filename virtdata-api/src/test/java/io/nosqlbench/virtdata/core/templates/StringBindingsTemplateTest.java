package io.nosqlbench.virtdata.core.templates;

/*
 * Copyright (c) 2022 nosqlbench
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
