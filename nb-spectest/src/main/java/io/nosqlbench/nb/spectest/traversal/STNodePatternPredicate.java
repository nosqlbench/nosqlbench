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
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class STNodePatternPredicate implements Predicate<Node> {
    private final Pattern pattern;

    public STNodePatternPredicate(Pattern pattern) {
        this.pattern = pattern;
    }

    public STNodePatternPredicate(String pattern) {
        String newPattern = (pattern.matches("^[a-zA-Z0-9]") ? ".*" : "") + pattern + (pattern.matches("[a-zA-Z0-9]$") ? ".*" : "");
        Pattern compiled = Pattern.compile(newPattern, Pattern.MULTILINE | Pattern.DOTALL);
        this.pattern = compiled;
    }

    @Override
    public boolean test(Node node) {
        if (node == null) {
            return false;
        }
        BasedSequence chars = node.getChars();
        Matcher matcher = pattern.matcher(chars);
        boolean matches = matcher.matches();
        return matches;
    }

    @Override
    public String toString() {
        return "PATTERN(" + this.pattern.pattern() + ")";
    }
}
