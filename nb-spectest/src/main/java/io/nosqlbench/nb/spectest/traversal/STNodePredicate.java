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

import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * <P>Construct a sequence of {com.vladsch.flexmark.util.ast.Node} matchers using
 * one of a few common predicate forms, inferred from the types of the predicate
 * formats provided.</P>
 *
 * <P>The available forms are:
 * <OL>
 * <LI>A fully constructed {@link Predicate} of {@link Node}. When provided,
 * this predicate is automatically checked against dummy data to ensure
 * the generic signature is valid.</LI>
 * <LI>A {@link Class} of any type of {@link Node}, which asserts that the
 * node's class is equal to that specified. (No type hierarchy checks are allowed.)</LI>
 * <LI>A fully compiled {@link Pattern}, which checks the textual content of
 * the node for a match.</LI>
 * <LI>A string, which is automatically promoted to a pattern as above, and checked
 * in the same way. By default, patterns provided this way are automatically
 * prefixed and suffixed with '.*' unless a regex character is found there. Also,
 * all matches are created with Pattern.MULTILINE and Pattern.DOTALL.
 * </LI>
 * </OL>
 * </P>
 */
public class STNodePredicate implements Predicate<Node>, Supplier<Node> {
    private final Predicate<Node> predicate;
    private Node found = null;

    public STNodePredicate(Object o) {
        this.predicate = resolvePredicate(o);
    }

    private Predicate<Node> resolvePredicate(Object object) {
        if (object instanceof Predicate predicate) {
            Paragraph paragraph = new Paragraph(BasedSequence.of("type checking"));
            // assert no runtime type casting issues
            predicate.test(paragraph);
            return predicate;
        } else if (object instanceof Class c) {
            return new STNodeClassPredicate(c);
        } else if (object instanceof Pattern p) {
            return new STNodePatternPredicate(p);
        } else if (object instanceof CharSequence cs) {
            return new STNodePatternPredicate(cs.toString());
        } else {
            throw new RuntimeException("no Node predicate for type " + object.getClass().getSimpleName());
        }
    }

    @Override
    public boolean test(Node node) {
        this.found = null;
        boolean isFound = predicate.test(node);
        if (isFound) {
            this.found = node;
        }
        return isFound;
    }

    @Override
    public Node get() {
        return this.found;
    }

    @Override
    public String toString() {
        return this.predicate.toString();
    }
}
