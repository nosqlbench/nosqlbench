/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.components.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class NBComponentTraversal {

    public static Iterator<NBComponent> traverseDepth(NBComponent component) {
        return new iterDepthFirst(component);
    }

    public static void visitDepthFirst(NBComponent component, Visitor visitor) {
        visitDepthFirst(component,visitor,0);
    }

    private static void visitDepthFirst(NBComponent component, Visitor visitor, int depth) {
        visitor.visit(component,depth);
        List<NBComponent> children = component.getChildren();
        for (NBComponent child : children) {
            visitDepthFirst(child,visitor,depth+1);
        }
    }


    /**
     * Visits each component. If the component does NOT match the predicate, then NON-matching visitor
     * method applies. Otherwise the MATCHing visitor method applies and the search stops at that node,
     * continuing again for every sibling.
     * @param component The component to test and visit
     * @param visitor The methods for non-matching and matching nodes
     * @param predicate A test to determine whether to apply the matching predicate and stop searching deeper
     */
    public static void visitDepthFirstLimited(NBComponent component, FilterVisitor visitor, Predicate<NBComponent> predicate) {
        visitDepthFirstLimited(component, visitor, 0, predicate);
    }

    private static void visitDepthFirstLimited(NBComponent component, FilterVisitor visitor, int depth, Predicate<NBComponent> predicate) {
        if (predicate.test(component)) {
            visitor.visitMatching(component,depth);
            return;
        } else {
            visitor.visitNonMatching(component,depth);
            List<NBComponent> children = component.getChildren();

            for (NBComponent child : children) {
                visitDepthFirstLimited(child,visitor,depth+1,predicate);
            }
        }

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

    public interface Visitor {
        void visit(NBComponent component, int depth);
    }
    public interface FilterVisitor {
        void visitMatching(NBComponent component, int depth);
        void visitNonMatching(NBComponent component, int depth);
    }
}
