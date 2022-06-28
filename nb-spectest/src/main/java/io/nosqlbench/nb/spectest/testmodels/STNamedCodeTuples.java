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

package io.nosqlbench.nb.spectest.testmodels;

import io.nosqlbench.nb.spectest.core.STNameAndCodeTuple;
import io.nosqlbench.nb.spectest.core.STNode;

import java.util.List;

/**
 * This is a view over an {@link io.nosqlbench.nb.spectest.core.STNodeAssembly},
 * which is merely backed by a {@link List} of {@link STNode}s. This view, however,
 * imposes a structure over the nodes which is name, data, name, data, ... and so on.
 * This is a convenient way to consume and validate Node data which follows the form:
 * <pre>{@code
 * *name1:*
 * ```yaml
 * document-property: version1
 * ```
 * *name2:*
 * ```yaml
 * document-property: version2
 * ```
 * *name3:*
 * ```yaml
 * document-property: version3
 * ```
 * }</pre>
 *
 * In this example, there are six consecutive nodes which contain 3 names and three fenced code sections.
 */
public class STNamedCodeTuples {

    private final List<STNameAndCodeTuple> tuples;

    public STNamedCodeTuples(List<STNameAndCodeTuple> tuples) {
        this.tuples = tuples;
    }


    /**
     * <p>For the STNamedCodeTuples view of a testable set of nodes, it is useful
     * to see what the name structure looks like in order to conditionally
     * match different types of testable sequences based on the asserted names
     * in the documentation. This will return the named, concatenated with "->",
     * and sanitized with all non-alphanumeric and &gt; and $lt;, space, underscore
     * and dash removed.</p>
     *
     * <p>For the example structure at the class level, this would look like:
     * <pre>{@code name1->name2->name3}</pre></p>
     * @return A signature of the node sequences based on the name elements
     */
    public String getTypeSignature() {
        StringBuilder sb = new StringBuilder();
        for (STNameAndCodeTuple tuple : tuples) {
            sb.append(tuple.getName()).append("->");
        }
        sb.setLength(sb.length()-"->".length());
        return sb.toString().replaceAll("[^-a-zA-Z0-9<> _]", "");
    }

    public STNameAndCodeTuple get(int i) {
        return tuples.get(i);
    }
}
