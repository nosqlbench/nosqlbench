/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.predicates.adapter;

import io.nosqlbench.virtdata.predicates.ast.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExamplePredicateAdapterTest {

    public static PredicateExpr exampleExpr =
        new PredicateExprDefaultImpl()
            .term(
                new PredicateTerm(new PField("username"), POperator.eq,new PComparator("joe"))
            );
    @Test
    public void testBasicAdapterExample() {
        ExamplePredicateAdapter epa = new ExamplePredicateAdapter();
        String nativeForm = epa.getPredicate(exampleExpr);
        assertThat(nativeForm)
            .isEqualTo("username eq joe");
    }

}
