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

import java.util.Iterator;
import java.util.LinkedList;

public class NBComponentTraversal {

    public static Iterator<NBComponent> traverseDepth(NBComponent component) {
        return new iterDepthFirst(component);
    }

    public static Iterator<NBComponent> traverseBreadth(NBComponent component) {
        return new IterBreadthFirst(component);
    }

    private static final class iterDepthFirst implements Iterator<NBComponent> {

        private final LinkedList<NBComponent> traversal = new LinkedList<>();
        public iterDepthFirst(NBComponent comp) {
            traversal.add(comp);
        }

        @Override
        public boolean hasNext() {
            return (!traversal.isEmpty());
        }

        @Override
        public NBComponent next() {
            NBComponent next = traversal.remove();
            for (NBComponent child : next.getChildren().reversed()) {
                traversal.addFirst(child);
            }
            return next;
        }
    }

    private static final class IterBreadthFirst implements Iterator<NBComponent> {
        private final LinkedList<NBComponent> traversal = new LinkedList<>();

        public IterBreadthFirst(NBComponent component) {
            traversal.addFirst(component);
        }

        @Override
        public boolean hasNext() {
            return (!traversal.isEmpty());
        }

        @Override
        public NBComponent next() {
            NBComponent next = traversal.removeFirst();
            traversal.addAll(next.getChildren());
            return next;
        }
    }

}
