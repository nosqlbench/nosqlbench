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

import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HeadingScannerTest {
    Parser parser = Parser.builder().extensions(List.of(YamlFrontMatterExtension.create())).build();

    @Test
    public void testBasicHeadings() {
        Node node = parser
            .parse("# heading1\n\n## heading2\n\ntext\n\n# heading3")
            .getFirstChild();

        HeadingScanner scanner = new HeadingScanner(".");

        assertThat(scanner.update(node).toString()).isEqualTo("heading1"); // Paragraph
        node = node.getNext();
        assertThat(scanner.update(node).toString()).isEqualTo("heading1.heading2"); // Paragraph
        node=node.getNext();
        assertThat(scanner.update(node).toString()).isEqualTo("heading1.heading2"); // Paragraph
        node=node.getNext();
        assertThat(scanner.update(node).toString()).isEqualTo("heading3"); // Paragraph
        scanner.index();
        assertThat(scanner.toString()).isEqualTo("heading3 (01)"); // Paragraph
        node=node.getNext();
        assertThat(node).isNull();
    }

}
