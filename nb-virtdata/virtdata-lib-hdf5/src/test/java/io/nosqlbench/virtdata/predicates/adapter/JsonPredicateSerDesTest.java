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
import io.nosqlbench.virtdata.predicates.ast.PConjunction;
import io.nosqlbench.virtdata.predicates.ast.POperator;
import io.nosqlbench.virtdata.predicates.ast.PredicateExpr;
import io.nosqlbench.virtdata.predicates.ast.PredicateTerm;
import io.nosqlbench.virtdata.predicates.types.PredicateSerDes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public static final String test3 = """
    {
        "conjunction": "or",
        "terms": [
            {
                "field": {"name": "highprice"},
                "operator": "gt",
                "comparator": {"value": 1000}
            },
            {
                "field": {"name": "lowprice"},
                "operator": "lt",
                "comparator": {"value": 1}
            }
        ]
    }
    """;

    private static final PredicateSerDes instance = new JsonPredicateSerDes();

    @Test
    public void testUnserialize() {
        PredicateExpr result1 = instance.unserialize(test1);
        assertEquals(PConjunction.none, result1.getConjunction());
        List<PredicateTerm> terms = result1.getTerms();
        assertEquals(1, terms.size());
        assertEquals("firstname", terms.getFirst().field.name);
        assertEquals("Mark", terms.getFirst().comparator.value);
        assertEquals(POperator.eq, terms.getFirst().operator);

        PredicateExpr result2 = instance.unserialize(test2);
        assertEquals(PConjunction.and, result2.getConjunction());
        List<PredicateTerm> terms2 = result2.getTerms();
        assertEquals(2, terms2.size());
        assertEquals("firstname", terms2.getFirst().field.name);
        assertEquals("Mark", terms2.getFirst().comparator.value);
        assertEquals(POperator.eq, terms2.getFirst().operator);
        assertEquals("lastname", terms2.get(1).field.name);
        assertEquals("Wolters", terms2.get(1).comparator.value);
        assertEquals(POperator.eq, terms2.get(1).operator);


        PredicateExpr result3 = instance.unserialize(test3);
        assertEquals(PConjunction.or, result3.getConjunction());
        List<PredicateTerm> terms3 = result3.getTerms();
        assertEquals(2, terms3.size());
        assertEquals("highprice", terms3.getFirst().field.name);
        assertEquals(1000.0, terms3.getFirst().comparator.value);
        assertEquals(POperator.gt, terms3.getFirst().operator);
        assertEquals("lowprice", terms3.get(1).field.name);
        assertEquals(1.0, terms3.get(1).comparator.value);
        assertEquals(POperator.lt, terms3.get(1).operator);

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
