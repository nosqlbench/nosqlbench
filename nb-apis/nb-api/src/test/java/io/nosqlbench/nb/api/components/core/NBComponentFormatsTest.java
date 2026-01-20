/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.components.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBComponentFormats;
import io.nosqlbench.nb.api.config.standard.TestComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
@Tag("unit")
public class NBComponentFormatsTest {

    @Test
    public void testFormat() {
        NBComponent root = new TestComponent("rootk","rootv");
        TestComponent tc = new TestComponent(root, "atest", "view");

        StringBuilder sb = new StringBuilder();
        NBComponentFormats.PrintVisitor visitor = new NBComponentFormats.PrintVisitor(sb);
        visitor.visit(root,2);
        visitor.visit(tc,3);
        String diagview = sb.toString();
        diagview=diagview.replaceAll("#[0-9]+","#id");
        assertThat(diagview).isEqualTo("""
                002 TestComponent {rootk="rootv"}
                 >TestComponent #id
                  003 TestComponent {atest="view",rootk="rootv"}
                   >TestComponent #id
            """);


    }

}
