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

package io.nosqlbench.nb.api.config;

import io.nosqlbench.nb.api.config.params.Synonyms;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SynonymsTest {

    @Test
    public void testCanonicalizeText() {
        String before = "test yaml ayamlfoo type btype typea targetrate";
        StringBuilder sb = new StringBuilder();
        String result = Synonyms.canonicalize(before, Synonyms.PARAM_SYNONYMS,
            (s, s2) -> sb.append("replaced ").append(s).append(" with ").append(s2).append("\n"));
        assertThat(result).isEqualTo("test workload ayamlfoo driver btype typea rate");
        assertThat(sb.toString()).isEqualTo("replaced type with driver\n" +
                "replaced targetrate with rate\n" +
                "replaced yaml with workload\n");
    }

}
