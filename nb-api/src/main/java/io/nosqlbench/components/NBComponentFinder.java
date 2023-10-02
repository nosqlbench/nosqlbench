/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.components;

import io.nosqlbench.adapters.api.util.TagFilter;

import java.util.*;

public class NBComponentFinder {

    public static List<NBComponent> findComponents(String pattern, NBComponent startNode) {
        TagFilter filter = new TagFilter(pattern);
        List<NBComponent> found = new ArrayList<>();
        Iterator<NBComponent> nbComponentIterator = NBComponentTraversal.traverseDepth(startNode);
        nbComponentIterator.forEachRemaining(c -> {
            if (filter.matchesLabeled(c)) {
                found.add(c);
            }
        });
        return found;
    }

    public static NBComponent findOneComponent(String pattern, NBComponent startNode) {
        List<NBComponent> found = findComponents(pattern, startNode);
        if (found.size()!=1) {
            throw new RuntimeException("Expected exactly 1 componet, but found " + found.size()+": for '" + pattern + "'");
        }
        return found.get(0);
    }
}
