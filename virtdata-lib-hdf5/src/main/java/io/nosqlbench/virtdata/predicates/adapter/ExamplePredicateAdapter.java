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

import io.nosqlbench.virtdata.predicates.ast.PredicateAndExpr;
import io.nosqlbench.virtdata.predicates.ast.PredicateExpr;
import io.nosqlbench.virtdata.predicates.ast.PredicateOrExpr;
import io.nosqlbench.virtdata.predicates.ast.PredicateTerm;
import io.nosqlbench.virtdata.predicates.types.PredicateAdapter;

import java.util.stream.Collectors;

/**
 * <P>This predicate adapter is not based on any real type of system.
 * It provides the ability to render a string-form predicate clause
 * for an imaginary system. This can be used for integrated testing
 * and validation of high level configurations in the
 * hands of users, like the diag adapter.</P>
 *
 * <P>The vernacular for this is simply JSON in the most obvious format,
 * using verbs and nouns which are familiar from classic SQL systems.</P>
 */
public class ExamplePredicateAdapter implements PredicateAdapter {

    @Override
    public String getPredicate(PredicateExpr model) {
        StringBuilder sb = new StringBuilder();
        String fragment = switch (model) {
            case PredicateTerm pt -> renderTerm(pt);
            case PredicateAndExpr pae -> renderTerm(pae);
            case PredicateOrExpr po -> renderTerm(po);
            default -> throw new IllegalStateException("Unexpected value: " + model);
        };
        sb.append(fragment);

        return sb.toString();
    }

    private String renderTerm(PredicateTerm pt) {
        String value = pt.field.name + " " + pt.operator.name() + " " + pt.comparator.value;
        return value;
    }

    private String renderTerm(PredicateAndExpr pae) {
        String value = pae.terms.stream().map(this::renderTerm).collect(Collectors.joining(" and "));
        return value;
    }

    private String renderTerm(PredicateOrExpr poe) {
        String value = poe.terms.stream().map(this::renderTerm).collect(Collectors.joining(" or "));
        return value;
    }
}
