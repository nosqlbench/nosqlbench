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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class STAndPredicate implements Predicate<Node> {
    private final List<STNodePredicate> predicates = new ArrayList<>();

    public STAndPredicate(Object... specs) {
        for (Object predicate : specs) {
            predicates.add(new STNodePredicate(predicate));
        }
    }

    @Override
    public boolean test(Node node) {
        for (int i = 0; i < predicates.size(); i++) {
            STNodePredicate predicate = predicates.get(i);
            if (!predicate.test(node)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "AND(" + predicates.stream().map(Object::toString).collect(Collectors.joining(","))+")";
    }
}
