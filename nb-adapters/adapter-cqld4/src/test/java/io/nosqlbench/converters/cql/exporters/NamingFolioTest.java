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

package io.nosqlbench.converters.cql.exporters;

import io.nosqlbench.cqlgen.binders.NamingFolio;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NamingFolioTest {

    @Test
    public void testBindingFolio() {
        NamingFolio folio = new NamingFolio("[OPTYPE-][COLUMN-][TYPEDEF-][TABLE!]-[KEYSPACE]");
        folio.addFieldRef(Map.of("column","c1","typedef","t1","table","tb1","keyspace","ks1"));
        assertThat(folio.getNames()).containsExactly("c1-t1-tb1-ks1");
        folio.addFieldRef("c2","t2","tb2","ks2");
        assertThat(folio.getNames()).containsExactly(
            "c1-t1-tb1-ks1","c2-t2-tb2-ks2"
        );
    }

}
