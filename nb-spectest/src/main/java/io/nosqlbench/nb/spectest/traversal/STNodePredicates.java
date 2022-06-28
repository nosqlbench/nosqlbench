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
import io.nosqlbench.nb.spectest.core.STDebug;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <P>{@link STNodePredicates} is a convenient wrapper around {@link STNodePredicate}
 * evaluation so that creating a {@link Node} scanner is a one-liner.
 * Any of the supported types for {@link STNodePredicate} are valid, in addition
 * to numbers which reference previous positions in the argument list (zero-indexed.)</P>
 *
 * <p>This allows for existing node matchers to be re-used by reference. For Example,
 * a specification like <pre>{@code new NodePredicates("__\\w__",0,0)}</pre> represents
 * a regular expression which would match "__a__" or "__b__" and then two more subsequent
 * matches against similar content, for a total of 3 consecutive matching nodes.</p>
 */
public class STNodePredicates implements Function<Node, Optional<List<Node>>>, STDebug {
    private final List<Predicate<Node>> predicates = new ArrayList<>();
    private final List<Node> found = new ArrayList<>();
    private boolean debug;

    public STNodePredicates(Object... predicateSpecs) {
        for (int i = 0; i < predicateSpecs.length; i++) {
            if (predicateSpecs[i] instanceof STNodePredicates pspecs) {
                predicates.addAll(pspecs.predicates);
            } else if (predicateSpecs[i] instanceof STArgumentRef number) {
                if (i > number.argidx()) {
                    predicates.add(predicates.get(number.argidx()));
                } else {
                    throw new InvalidParameterException("predicate reference at " + i + " references invalid position at " + number.argidx());
                }
            } else {
                predicates.add(new STNodePredicate(predicateSpecs[i]));
            }
        }
    }

    @Override
    public Optional<List<Node>> apply(Node node) {
        if (test(node)) {
            return Optional.of(List.copyOf(found));
        } else {
            return Optional.empty();
        }
    }

    private boolean test(Node node) {
        found.clear();

        for (Predicate<Node> predicate : predicates) {
            if (node == null) {
                return false;
            }
            if (!predicate.test(node)) {
                return false;
            }
            found.add(node);

            node = node.getNext();
        }
        return true;
    }

    @Override
    public String toString() {
        return this.predicates.stream().map(Object::toString).collect(Collectors.joining(","))+")";
    }

    @Override
    public void applyDebugging(boolean enabled) {
        this.debug = enabled;
    }
}
