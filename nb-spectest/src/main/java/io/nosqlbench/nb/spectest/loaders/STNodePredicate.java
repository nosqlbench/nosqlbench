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

package io.nosqlbench.nb.spectest.loaders;

import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
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
            Paragraph paragraph = new Paragraph(BasedSequence.of("test paragraph"));
            predicate.test(paragraph);
            return predicate;
        } else if (object instanceof Class c) {
            return new ClassPredicate(c);
        } else if (object instanceof Pattern p) {
            return new PatternPredicate(p);
        } else if (object instanceof CharSequence cs) {
            return new PatternPredicate(cs.toString());
        } else {
            throw new RuntimeException("no Node predicate for type " + object.getClass().getSimpleName());
        }
    }

    private final static class ClassPredicate implements Predicate<Node> {
        private final Class<? extends Node> matchingClass;

        public ClassPredicate(Class<? extends Node> matchingClass) {
            this.matchingClass = matchingClass;
        }

        @Override
        public boolean test(Node node) {
            Class<? extends Node> classToMatch = node.getClass();
            boolean matches = matchingClass.equals(classToMatch);
            return matches;
        }
    }

    private final static class PatternPredicate implements Predicate<Node> {
        private final Pattern pattern;

        private PatternPredicate(Pattern pattern) {
            this.pattern = pattern;
        }

        private PatternPredicate(String pattern) {
            String newPattern = (pattern.matches("^[a-zA-Z0-9]") ? ".*" : "") + pattern + (pattern.matches("[a-zA-Z0-9]$") ? ".*" : "");
            Pattern compiled = Pattern.compile(newPattern, Pattern.MULTILINE | Pattern.DOTALL);
            this.pattern = compiled;
        }

        @Override
        public boolean test(Node node) {
            BasedSequence chars = node.getChars();
            Matcher matcher = pattern.matcher(chars);
            boolean matches = matcher.matches();
            return matches;
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
}
