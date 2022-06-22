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

package io.nosqlbench.nb.spectest;

import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import io.nosqlbench.nb.spectest.core.STNodeAssembly;
import io.nosqlbench.nb.spectest.loaders.STDefaultNodeLoader;
import io.nosqlbench.nb.spectest.loaders.STNodePredicates;
import io.nosqlbench.nb.spectest.types.STNodeLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class STNodePredicatesTest {

    @Test
    public void testBackReferences() {
        STNodePredicates predicateShouldMatch1 = new STNodePredicates(".*__\\w__.*", 0, 0);
        STNodePredicates predicateShouldMatch2 = new STNodePredicates("__\\w__", 0, 0);
        STNodePredicates predicateShouldNotMatch3 = new STNodePredicates("^__\\w__", 0, 0);
        STNodePredicates predicateShouldNotMatch4 = new STNodePredicates("^__\\w__$", 0, 0);
        String testMarkdown = """
            paragraph contents with __a__.

            paragraph contents with __b__.

            paragraph contents with __c__.
            """;


        Parser parser = Parser.builder().extensions(List.of(YamlFrontMatterExtension.create())).build();
        Document document = parser.parse(testMarkdown);
//        STDefaultLoader scanner = new STDefaultLoader(predicates);
        STNodeLoader scanner = new STDefaultNodeLoader(predicateShouldMatch1);

        List<STNodeAssembly> assemblies = scanner.apply(null, document);
        assertThat(assemblies).hasSizeGreaterThan(0);
        System.out.print(assemblies);
    }

}
