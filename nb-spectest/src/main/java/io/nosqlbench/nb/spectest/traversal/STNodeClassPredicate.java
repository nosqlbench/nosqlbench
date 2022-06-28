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

package io.nosqlbench.nb.spectest.traversal;

import com.vladsch.flexmark.util.ast.Node;

import java.util.function.Predicate;

final class STNodeClassPredicate implements Predicate<Node> {
    private final Class<? extends Node> matchingClass;

    public STNodeClassPredicate(Class<? extends Node> matchingClass) {
        this.matchingClass = matchingClass;
    }

    @Override
    public boolean test(Node node) {
        Class<? extends Node> classToMatch = node.getClass();
        boolean matches = matchingClass.equals(classToMatch);
        return matches;
    }

    @Override
    public String toString() {
        return "CLASS(" + matchingClass.getSimpleName() + ")";
    }
}
