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

package io.nosqlbench.virtdata.predicates.types;

import io.nosqlbench.virtdata.predicates.ast.PredicateExpr;

/**
 * PredicateAdapters know how to read the predicate abstract syntax
 * representation (in the form of a {@link PredicateExpr} and all it entails)
 * and render a protocol-specific form. (adapter or system vernacular).
 * The initial version presumed that a string form suffice for most cases,
 * although this interface should be generified or specialized when needed to
 * support other native in-memory representations, such as those which are
 * based on native driver APIs and their direct types.
 */
public interface PredicateAdapter {
    String getPredicate(PredicateExpr model);
}
