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

package io.nosqlbench.virtdata.predicates.ast;

import java.util.ArrayList;
import java.util.List;

public class PredicateExprDefaultImpl implements PredicateExpr {

    public final List<PredicateTerm> terms = new ArrayList<>();
    public PConjunction conjunction = PConjunction.none;

    public PredicateExprDefaultImpl()  {
    }
    public PredicateExprDefaultImpl(List<PredicateTerm> terms) {
        this.terms.addAll(terms);
    }

    public PredicateExprDefaultImpl term(PredicateTerm term) {
        this.terms.add(term);
        return this;
    }

    @Override
    public PConjunction getConjunction() {
        return this.conjunction;
    }

    @Override
    public List<PredicateTerm> getTerms() {
        return terms;
    }
}
