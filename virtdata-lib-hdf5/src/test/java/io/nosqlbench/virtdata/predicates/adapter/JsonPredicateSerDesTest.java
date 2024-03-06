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
 *
 */

package io.nosqlbench.virtdata.predicates.adapter;

import io.nosqlbench.virtdata.predicates.JsonPredicateSerDes;
import io.nosqlbench.virtdata.predicates.ast.PredicateExpr;
import io.nosqlbench.virtdata.predicates.types.PredicateSerDes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsonPredicateSerDesTest {

    public static final String test1 = """
    {
        "conjunction": "none",
        "terms": [
            {
                "field": {"name": "firstname"},
                "operator": "eq",
                "comparator": {"value": "Mark"}
            }
        ]
    }
    """;
    public static final String test2 = """
    {
        "conjunction": "and",
        "terms": [
            {
                "field": {"name": "firstname"},
                "operator": "eq",
                "comparator": {"value": "Mark"}
            },
            {
                "field": {"name": "lastname"},
                "operator": "eq",
                "comparator": {"value": "Wolters"}
            }
        ]
    }
    """;

    private static final PredicateSerDes instance = new JsonPredicateSerDes();

    @Test
    public void testUnserialize() {
        PredicateExpr result1 = instance.unserialize(test1);
        assertNotNull(result1);

        PredicateExpr result2 = instance.unserialize(test2);
        assertNotNull(result2);

    }

    @Test
    public void testSerialize() {
        // PredicateExpr predicateExpr = null;
        // PredicateSerDes instance = new JsonPredicateSerDes();
        // String expResult = "";
        // String result = instance.serialize(predicateExpr);
        // assertEquals(expResult, result);
    }
}
