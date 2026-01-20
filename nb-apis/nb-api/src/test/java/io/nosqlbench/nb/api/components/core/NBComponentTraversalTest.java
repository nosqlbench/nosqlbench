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

import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBComponentTraversal;
import io.nosqlbench.nb.api.config.standard.TestComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.ArrayList;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
public class NBComponentTraversalTest {

    /**
     * <pre>{@code
     *  equivalent to
     *      private final static NBComponent a = new TestComponent("name_a","a")
     *         .attach(new TestComponent("name_1","1"))
     *         .attach(new TestComponent("name_2","2")
     *             .attach(new TestComponent("name_X","X"))
     *             .attach(new TestComponent("name_Y","Y")));
     * }</pre>
     */
    private final static TestComponent a = new TestComponent("name_a","a");
    private final static TestComponent sub1 = new TestComponent(a, "name_1", "1");
    private final static TestComponent sub2 = new TestComponent(a, "name_2", "2");
    private final static TestComponent dotX = new TestComponent(sub1, "name_X", "X");
    private final static TestComponent dotY = new TestComponent(sub1, "name_Y", "Y");


    @Test
    public void testDepthFirstTraversal() {
        Iterator<NBComponent> byDepth = NBComponentTraversal.traverseDepth(a);
        ArrayList<NBComponent> taller = new ArrayList<>();
        byDepth.forEachRemaining(taller::add);
        assertThat(taller).containsExactly(a,sub1,dotX,dotY,sub2);
    }
    @Test
    public void testBreadthFirstTraversal() {
        Iterator<NBComponent> byBreadth = NBComponentTraversal.traverseBreadth(a);
        ArrayList<NBComponent> wider = new ArrayList<>();
        byBreadth.forEachRemaining(wider::add);
        assertThat(wider).containsExactly(a,sub1,sub2,dotX,dotY);
    }

    @Test
    public void testDepthFirstVisitor() {
        NBComponentTraversal.visitDepthFirst(a, new NBComponentTraversal.Visitor() {
            @Override
            public void visit(NBComponent component, int depth) {
                System.out.println(">".repeat(depth)+":"+component.description());
            }
        });
    }
}
